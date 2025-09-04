package com.man4tasik

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.man4tasik.databinding.ActivityRegisterStudentBinding
import com.man4tasik.model.Student
import com.man4tasik.utils.FaceEmbeddingExtractor
import com.man4tasik.utils.FirebaseUtils
import com.man4tasik.utils.flipHorizontally
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RegisterStudentActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterStudentBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var latestEmbedding: List<Float>? = null
    private var isFaceDetected = false

    private val handler = Handler(Looper.getMainLooper())
    private val resetRunnable = Runnable {
        latestEmbedding = null
        isFaceDetected = false
        runOnUiThread {
            b.tvFaceStatus.text = "Wajah kadaluarsa, silakan hadapkan lagi ke kamera"
            b.tvFaceStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            b.tvFaceStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        }
        Toast.makeText(this, "Wajah kadaluarsa, silakan hadapkan lagi ke kamera", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterStudentBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Set status awal wajah
        b.tvFaceStatus.text = "Status wajah: Belum terdeteksi"
        b.tvFaceStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        b.tvFaceStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        b.btnSwitchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                CameraSelector.DEFAULT_BACK_CAMERA
            else
                CameraSelector.DEFAULT_FRONT_CAMERA

            startCamera()
        }

        val kelasList = listOf(
            "X-A", "X-B", "X-C", "X-D", "X-E", "X-F",
            "XI-A", "XI-B", "XI-C", "XI-D", "XI-E", "XI-F",
            "XII-A", "XII-B", "XII-C", "XII-D", "XII-E", "XII-F"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            kelasList
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        b.spinnerClass.adapter = adapter

        b.btnRegister.setOnClickListener {
            val name = b.edtName.text.toString()
            val kelas = b.spinnerClass.selectedItem.toString()

            if (name.isEmpty() || kelas.isEmpty()) {
                Toast.makeText(this, "Isi nama & kelas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isFaceDetected || latestEmbedding.isNullOrEmpty()) {
                Toast.makeText(this, "Wajah tidak terdeteksi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cleanedName = name.trim().lowercase().replace(" ", "_")
            val student = Student(
                id = cleanedName,
                name = name,
                kelas = kelas,
                faceEmbedding = latestEmbedding ?: emptyList()
            )

            latestEmbedding = null
            isFaceDetected = false
            handler.removeCallbacks(resetRunnable)

            // Reset UI status
            runOnUiThread {
                b.tvFaceStatus.text = "Status wajah: Belum terdeteksi"
                b.tvFaceStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                b.tvFaceStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
            }

            FirebaseUtils.addStudent(student) { success ->
                if (success) {
                    val mediaPlayer = MediaPlayer.create(this, R.raw.success_sound)
                    mediaPlayer.setOnCompletionListener { it.release() }
                    mediaPlayer.start()

                    Toast.makeText(this, "Siswa bernama"+ "$name"+ "terdaftar" , Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal daftar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(b.viewFinder.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, FaceAnalyzer())
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            var bitmap = imageProxy.toBitmap()
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                bitmap = bitmap.flipHorizontally()
            }

            val extractor = FaceEmbeddingExtractor(this@RegisterStudentActivity)
            val faces = extractor.detectFaces(bitmap)

            if (faces != null && faces.isNotEmpty()) {
                val embedding = extractor.getEmbedding(bitmap).toList()
                latestEmbedding = embedding
                isFaceDetected = true

                runOnUiThread {
                    b.tvFaceStatus.text = "Wajah terdeteksi! Siap daftar"
                    b.tvFaceStatus.setTextColor(ContextCompat.getColor(this@RegisterStudentActivity, android.R.color.holo_green_dark))
                    b.tvFaceStatus.setBackgroundColor(ContextCompat.getColor(this@RegisterStudentActivity, android.R.color.holo_green_light))
                }

                handler.removeCallbacks(resetRunnable)
                handler.postDelayed(resetRunnable, 2000)  // 2 detik
            }

            imageProxy.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        handler.removeCallbacks(resetRunnable)
    }
}
