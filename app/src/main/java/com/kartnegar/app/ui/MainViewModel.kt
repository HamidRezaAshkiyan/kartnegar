package com.kartnegar.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kartnegar.app.KartNegarApp
import com.kartnegar.app.data.ScannedItemEntity
import com.kartnegar.app.domain.DetectedItem
import com.kartnegar.app.domain.FinancialExtractor
import com.kartnegar.app.domain.FinancialValidators
import com.kartnegar.app.domain.ManualValidationResult
import com.kartnegar.app.ocr.ImagePreprocessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScanUiState(
    val isScanning: Boolean = false,
    val previewBitmap: Bitmap? = null,
    val detectedItems: List<DetectedItem> = emptyList(),
    val rawOcrText: String = "",
    val elapsedMs: Long? = null,
    val darkModeInverted: Boolean = false,
    val errorMessage: String? = null,
    val hasScannedOnce: Boolean = false,
    val statusBanner: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as KartNegarApp
    private val repository = app.repository
    private val ocrEngine = app.ocrEngine

    private val _scanState = MutableStateFlow(ScanUiState())
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    val savedItems: StateFlow<List<ScannedItemEntity>> = repository.savedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _manualInput = MutableStateFlow("")
    val manualInput: StateFlow<String> = _manualInput.asStateFlow()

    private val _manualResult = MutableStateFlow(FinancialValidators.validateManualInput(""))
    val manualResult: StateFlow<ManualValidationResult> = _manualResult.asStateFlow()

    fun scanImageUri(uri: Uri) {
        viewModelScope.launch {
            _scanState.update {
                it.copy(
                    isScanning = true,
                    errorMessage = null,
                    statusBanner = null
                )
            }
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    ImagePreprocessor.loadBitmapFromUri(app, uri)
                }
                if (bitmap == null) {
                    _scanState.update {
                        it.copy(
                            isScanning = false,
                            hasScannedOnce = true,
                            errorMessage = "Could not decode the selected image."
                        )
                    }
                    return@launch
                }

                // Keep a lightweight preview copy and recycle previous preview
                val oldPreview = _scanState.value.previewBitmap
                _scanState.update { it.copy(previewBitmap = bitmap) }
                if (oldPreview != null && oldPreview !== bitmap && !oldPreview.isRecycled) {
                    oldPreview.recycle()
                }

                val result = ocrEngine.scanBitmap(bitmap)
                _scanState.update {
                    it.copy(
                        isScanning = false,
                        detectedItems = result.items,
                        rawOcrText = result.rawText,
                        elapsedMs = result.elapsedMs,
                        darkModeInverted = result.darkModeInverted,
                        hasScannedOnce = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _scanState.update {
                    it.copy(
                        isScanning = false,
                        hasScannedOnce = true,
                        errorMessage = "Scan failed: ${e.localizedMessage ?: "unknown error"}"
                    )
                }
            }
        }
    }

    fun scanRawText(text: String) {
        if (text.isBlank()) return
        val start = System.currentTimeMillis()
        val items = FinancialExtractor.extract(text)
        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        _scanState.update {
            it.copy(
                isScanning = false,
                detectedItems = items,
                rawOcrText = text.trim(),
                elapsedMs = elapsed,
                darkModeInverted = false,
                hasScannedOnce = true,
                errorMessage = null
            )
        }
    }

    fun clearScanResult() {
        val oldPreview = _scanState.value.previewBitmap
        _scanState.value = ScanUiState()
        if (oldPreview != null && !oldPreview.isRecycled) {
            oldPreview.recycle()
        }
    }

    fun saveItem(item: DetectedItem, label: String = "") {
        viewModelScope.launch {
            repository.saveDetectedItem(item, label)
            _scanState.update { it.copy(statusBanner = "Saved to local vault") }
        }
    }

    fun updateSavedLabel(id: Long, label: String) {
        viewModelScope.launch {
            repository.updateLabel(id, label)
        }
    }

    fun deleteSavedItem(item: ScannedItemEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun clearAllSavedItems() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun updateManualInput(input: String) {
        _manualInput.value = input
        _manualResult.value = FinancialValidators.validateManualInput(input)
    }

    fun consumeStatusBanner() {
        _scanState.update { it.copy(statusBanner = null) }
    }
}
