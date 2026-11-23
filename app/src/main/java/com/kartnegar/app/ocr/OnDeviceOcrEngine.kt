package com.kartnegar.app.ocr

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import com.kartnegar.app.domain.DetectedItem
import com.kartnegar.app.domain.FinancialExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class OcrScanResult(
    val items: List<DetectedItem>,
    val rawText: String,
    val elapsedMs: Long,
    val darkModeInverted: Boolean
)

class OnDeviceOcrEngine(private val context: Context) {

    private val dataRoot: File
        get() = File(context.filesDir, "tesseract")

    private val tessDataDir: File
        get() = File(dataRoot, "tessdata")

    @Synchronized
    private fun ensureTessDataCopied() {
        if (!tessDataDir.exists()) {
            tessDataDir.mkdirs()
        }
        val models = listOf("eng.traineddata", "fas.traineddata")
        for (model in models) {
            val outFile = File(tessDataDir, model)
            if (!outFile.exists() || outFile.length() == 0L) {
                context.assets.open("tessdata/$model").use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    suspend fun scanBitmap(sourceBitmap: Bitmap): OcrScanResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        ensureTessDataCopied()

        val prepared = ImagePreprocessor.prepareForOcr(sourceBitmap)
        val passOutputs = mutableListOf<String>()

        try {
            // Pass 1: Fast English/Latin LSTM pass (sharp on 0-9 and IR)
            val engText = runTesseractPass(prepared.bitmap, "eng")
            if (engText.isNotBlank()) passOutputs.add(engText)

            // Pass 2: Persian + English LSTM pass (reads Persian digits ۰-۹ & labels like کد ملی / شبا)
            val fasEngText = runTesseractPass(prepared.bitmap, "fas+eng")
            if (fasEngText.isNotBlank()) passOutputs.add(fasEngText)

            var items = FinancialExtractor.extractFromMultiplePasses(passOutputs)

            // Pass 3: Fallback high-contrast pass for tough embossed cards or low-contrast photos
            if (items.isEmpty()) {
                val highContrast = ImagePreprocessor.createHighContrastPass(prepared.bitmap)
                try {
                    val hcText = runTesseractPass(highContrast, "eng")
                    if (hcText.isNotBlank()) {
                        passOutputs.add(hcText)
                        items = FinancialExtractor.extractFromMultiplePasses(passOutputs)
                    }
                } finally {
                    highContrast.recycle()
                }
            }

            val combinedRawText = passOutputs
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .joinToString("\n\n---\n\n")

            OcrScanResult(
                items = items,
                rawText = combinedRawText,
                elapsedMs = System.currentTimeMillis() - startTime,
                darkModeInverted = prepared.darkModeInverted
            )
        } finally {
            prepared.bitmap.recycle()
        }
    }

    private fun runTesseractPass(bitmap: Bitmap, language: String): String {
        val tess = TessBaseAPI()
        return try {
            val initialized = tess.init(dataRoot.absolutePath, language, TessBaseAPI.OEM_LSTM_ONLY)
            if (!initialized) return ""
            tess.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
            tess.setImage(bitmap)
            tess.utF8Text ?: ""
        } catch (_: Throwable) {
            ""
        } finally {
            try {
                tess.recycle()
            } catch (_: Throwable) {
                // Ignore native cleanup errors
            }
        }
    }
}
