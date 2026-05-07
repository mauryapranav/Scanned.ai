package com.example.skannedai.viewmodel

import com.google.android.gms.tasks.Tasks
import android.graphics.Bitmap
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.skannedai.data.local.ScanDatabase
import com.example.skannedai.data.local.ScanResult
import com.example.skannedai.data.remote.RetrofitInstance
import com.example.skannedai.data.remote.ScanRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers

sealed class ScanUiState {
    object Idle : ScanUiState()
    object ExtractingText : ScanUiState()
    object Processing : ScanUiState()
    data class Success(val result: String) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ScanDatabase.getDatabase(application)
    private val dao = db.scanDao()

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _extractedText = MutableStateFlow("")
    val extractedText: StateFlow<String> = _extractedText.asStateFlow()

    val allScans = dao.getAllScans()

    fun extractTextFromUri(uri: Uri) {
        _uiState.value = ScanUiState.ExtractingText
        val context = getApplication<Application>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Decode full-res bitmap
                val fullBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, uri)
                    ) { decoder, _, _ -> decoder.isMutableRequired = true }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val allText = StringBuilder()

                // 2. Split into tiles: top-half, bottom-half, with overlap
                val tiles = getTiles(fullBitmap)

                // 3. OCR each tile
                for (tile in tiles) {
                    val image = InputImage.fromBitmap(tile, 0)
                    try {
                        val result = Tasks.await(recognizer.process(image))
                        if (result.text.isNotBlank()) {
                            allText.appendLine(result.text)
                        }
                    } catch (e: Exception) {
                        // skip failed tile, continue with others
                    }
                }

                val finalText = allText.toString().trim()
                _extractedText.value = if (finalText.isNotBlank()) finalText else "No text found"
                _uiState.value = ScanUiState.Idle

            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error("Failed to load image: ${e.message}")
            }
        }
    }

    private fun getTiles(bitmap: Bitmap): List<Bitmap> {
        val width = bitmap.width
        val height = bitmap.height
        val overlap = height / 10  // 10% overlap between tiles

        // For a typical question paper: split into 3 horizontal strips
        val tileCount = 3
        val tileHeight = height / tileCount

        return (0 until tileCount).map { i ->
            val top = (i * tileHeight - if (i > 0) overlap else 0).coerceAtLeast(0)
            val bottom = ((i + 1) * tileHeight + if (i < tileCount - 1) overlap else 0).coerceAtMost(height)
            Bitmap.createBitmap(bitmap, 0, top, width, bottom - top)
        }
    }

    fun processText(mode: String, question: String = "") {
        val text = _extractedText.value
        if (text.isBlank()) {
            _uiState.value = ScanUiState.Error("No text extracted yet")
            return
        }

        _uiState.value = ScanUiState.Processing
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.process(
                    ScanRequest(text = text, mode = mode, question = question)
                )
                _uiState.value = ScanUiState.Success(response.result)

                dao.insert(
                    ScanResult(
                        originalText = text,
                        mode = mode,
                        result = response.result
                    )
                )
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error("Failed: ${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = ScanUiState.Idle
        _extractedText.value = ""
    }
}