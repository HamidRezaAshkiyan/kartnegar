package com.kartnegar.app.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlin.math.max
import kotlin.math.min

data class PreprocessedImage(
    val bitmap: Bitmap,
    val darkModeInverted: Boolean
)

object ImagePreprocessor {

    private const val MAX_OCR_DIMENSION = 1800
    private const val MIN_OCR_WIDTH = 900

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        val origW = boundsOptions.outWidth
        val origH = boundsOptions.outHeight
        if (origW <= 0 || origH <= 0) return null

        var sampleSize = 1
        while ((origW / sampleSize) > MAX_OCR_DIMENSION * 2 || (origH / sampleSize) > MAX_OCR_DIMENSION * 2) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decoded = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        val rotationDegrees = readExifRotation(context, uri)
        val oriented = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            if (rotated !== decoded) decoded.recycle()
            rotated
        } else {
            decoded
        }

        return scaleToOptimalSize(oriented)
    }

    private fun readExifRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                when (ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun scaleToOptimalSize(source: Bitmap): Bitmap {
        val maxDim = max(source.width, source.height)
        val scale = when {
            maxDim > MAX_OCR_DIMENSION -> MAX_OCR_DIMENSION.toFloat() / maxDim.toFloat()
            source.width < MIN_OCR_WIDTH && maxDim < MAX_OCR_DIMENSION -> {
                min(2.0f, MIN_OCR_WIDTH.toFloat() / source.width.toFloat())
            }
            else -> 1.0f
        }
        if (scale == 1.0f) return source

        val newW = (source.width * scale).toInt().coerceAtLeast(1)
        val newH = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, newW, newH, true)
        if (scaled !== source) source.recycle()
        return scaled
    }

    /**
     * Converts to grayscale, detects dark-mode screenshots (low average luminance)
     * and automatically inverts them so Tesseract sees dark text on a light background,
     * then applies moderate contrast enhancement.
     */
    fun prepareForOcr(source: Bitmap): PreprocessedImage {
        val avgLuma = estimateAverageLuminance(source)
        val isDarkMode = avgLuma < 105f

        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val matrix = ColorMatrix()
        matrix.setSaturation(0f) // grayscale

        if (isDarkMode) {
            val invertMatrix = ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(invertMatrix)
        }

        // Gentle contrast boost (1.25x)
        val contrast = 1.25f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(contrastMatrix)

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return PreprocessedImage(bitmap = result, darkModeInverted = isDarkMode)
    }

    /**
     * Stronger contrast pass for embossed physical cards or low-contrast photos.
     */
    fun createHighContrastPass(preprocessed: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(preprocessed.width, preprocessed.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val contrast = 2.0f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(contrastMatrix)
        canvas.drawBitmap(preprocessed, 0f, 0f, paint)
        return result
    }

    private fun estimateAverageLuminance(bitmap: Bitmap): Float {
        val stepX = (bitmap.width / 40).coerceAtLeast(1)
        val stepY = (bitmap.height / 40).coerceAtLeast(1)
        var sum = 0.0
        var count = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val luma = 0.299 * Color.red(pixel) +
                    0.587 * Color.green(pixel) +
                    0.114 * Color.blue(pixel)
                sum += luma
                count++
                x += stepX
            }
            y += stepY
        }
        return if (count == 0) 128f else (sum / count).toFloat()
    }
}
