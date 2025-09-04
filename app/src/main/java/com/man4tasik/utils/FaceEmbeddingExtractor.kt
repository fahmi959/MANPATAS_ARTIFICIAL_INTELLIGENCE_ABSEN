package com.man4tasik.utils

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FaceEmbeddingExtractor(context: Context) {
    private var interpreter: Interpreter

    init {
        val assetManager = context.assets
        val model = assetManager.open("mobile_face_net.tflite").readBytes()
        val buffer = ByteBuffer.allocateDirect(model.size)
        buffer.order(ByteOrder.nativeOrder())
        buffer.put(model)
        interpreter = Interpreter(buffer)
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {
        // 1. Resize ke 112x112 sesuai MobileFaceNet
        val inputSize = 112
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        // 2. Preprocess ke Float32 normalized [-1,1]
        val byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 128f - 1f
            val g = ((pixel shr 8) and 0xFF) / 128f - 1f
            val b = (pixel and 0xFF) / 128f - 1f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        // 3. Output shape (1,128)
        val output = TensorBuffer.createFixedSize(intArrayOf(1, 128), org.tensorflow.lite.DataType.FLOAT32)
        interpreter.run(byteBuffer, output.buffer.rewind())

        return output.floatArray
    }


    fun detectFaces(bitmap: Bitmap): MutableList<com.google.mlkit.vision.face.Face>? {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val faceDetector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )

        val result = Tasks.await(faceDetector.process(inputImage)) // pakai await supaya sync
        return result // List<Face>
    }

}