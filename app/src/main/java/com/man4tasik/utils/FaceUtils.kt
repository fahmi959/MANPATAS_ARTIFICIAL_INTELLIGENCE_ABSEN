package com.man4tasik.utils



import kotlin.math.sqrt

object FaceUtils {

    /**
     * Hitung cosine similarity antara dua vektor embedding wajah
     * @param v1 FloatArray/ List<Float> embedding wajah terdeteksi
     * @param v2 FloatArray/ List<Float> embedding wajah dari database
     * @return skor similarity (semakin dekat 1, semakin mirip)
     */
    fun cosineSimilarity(v1: List<Float>, v2: List<Float>): Float {
        if (v1.size != v2.size) return 0f

        var dot = 0f
        var normV1 = 0f
        var normV2 = 0f

        for (i in v1.indices) {
            dot += v1[i] * v2[i]
            normV1 += v1[i] * v1[i]
            normV2 += v2[i] * v2[i]
        }

        val denominator = sqrt(normV1) * sqrt(normV2)
        return if (denominator == 0f) 0f else dot / denominator
    }
}