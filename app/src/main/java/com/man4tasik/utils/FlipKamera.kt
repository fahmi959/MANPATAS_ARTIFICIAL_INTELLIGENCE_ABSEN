package com.man4tasik.utils



import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.flipHorizontally(): Bitmap {
    val matrix = Matrix().apply {
        preScale(-1f, 1f)
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
