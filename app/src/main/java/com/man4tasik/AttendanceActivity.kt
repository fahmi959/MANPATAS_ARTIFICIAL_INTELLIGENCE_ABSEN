package com.man4tasik

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.toObject
import com.man4tasik.databinding.ActivityAttendanceBinding
import com.man4tasik.model.Student
import com.man4tasik.utils.FaceEmbeddingExtractor
import com.man4tasik.utils.FaceUtils
import com.man4tasik.utils.FirebaseUtils
import com.man4tasik.utils.flipHorizontally
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AttendanceActivity : AppCompatActivity() {

    private lateinit var b: ActivityAttendanceBinding
    private lateinit var cameraExecutor: ExecutorService
    private val students = mutableListOf<Student>()
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(b.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        mediaPlayer = MediaPlayer.create(this, R.raw.absen_success)

        loadStudents()
        startCamera()

        b.btnSwitchCamera.setOnClickListener {
            toggleCamera()
        }
    }

    private fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        startCamera()
    }

    private fun loadStudents() {
        FirebaseUtils.firestore.collection("students").get()
            .addOnSuccessListener { snapshot ->
                students.clear()
                for (doc in snapshot) {
                    val student = doc.toObject<Student>()
                    students.add(student)
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(b.viewFinder.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, FaceAnalyzer())
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, analyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            var bitmap = imageProxy.toBitmap()

            // Flip horizontal jika kamera depan karena biasanya mirror image
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                bitmap = bitmap.flipHorizontally()
            }

            val extractor = FaceEmbeddingExtractor(this@AttendanceActivity)
            val detectedEmbedding = extractor.getEmbedding(bitmap).toList()

            var bestStudent: Student? = null
            var bestScore = 0f
            val threshold = 0.7f

            for (student in students) {
                if (student.faceEmbedding.isNotEmpty()) {
                    val score = FaceUtils.cosineSimilarity(detectedEmbedding, student.faceEmbedding)
                    if (score > bestScore) {
                        bestScore = score
                        bestStudent = student
                    }
                }
            }

            if (bestStudent != null && bestScore > threshold) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                FirebaseUtils.markAttendanceIfNotExists(
                    studentId = bestStudent.id,
                    name = bestStudent.name,
                    kelas = bestStudent.kelas,
                    date = date
                ) {
                    runOnUiThread {
                        showStatus("${bestStudent.name} hadir")
                        playSound()
                    }
                }
            }

            imageProxy.close()
        }
    }

    private fun showStatus(message: String) {
        b.tvStatus.text = message
        b.tvStatus.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                b.tvStatus.animate().alpha(0f).setDuration(1500).start()
            }.start()
    }

    private fun playSound() {
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
