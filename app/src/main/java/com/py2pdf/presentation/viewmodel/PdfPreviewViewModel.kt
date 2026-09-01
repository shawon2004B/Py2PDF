package com.py2pdf.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.py2pdf.data.repository.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * ViewModel for PDF Preview Screen.
 *
 * Responsibilities:
 * - Manages UI state (Loading, Success, Error, Idle)
 * - Handles PDF loading with proper error handling
 * - Manages file sharing with FileProvider and URI permissions
 * - Executes all IO operations on Dispatchers.IO
 * - Properly scoped coroutines with viewModelScope
 */
class PdfPreviewViewModel(
    private val pdfRepository: PdfRepository = PdfRepository()
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val pages: List<Bitmap>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    /**
     * Load PDF pages from storage.
     * Executes on IO dispatcher and switches to Main for state updates.
     * Handles IOException and general exceptions with user-friendly messages.
     */
    fun loadPdfPages() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val pages = withContext(Dispatchers.IO) {
                    pdfRepository.loadPdfPages()
                }
                _uiState.value = UiState.Success(pages)
            } catch (e: IOException) {
                val errorMessage = when {
                    e.message?.contains("not found") == true -> "PDF file not found"
                    e.message?.contains("not readable") == true -> "PDF file is not readable"
                    else -> "Failed to load PDF: ${e.message}"
                }
                _uiState.value = UiState.Error(errorMessage)
            } catch (e: OutOfMemoryError) {
                _uiState.value = UiState.Error("PDF is too large to render (out of memory)")
            } catch (e: SecurityException) {
                _uiState.value = UiState.Error("Permission denied: Cannot access PDF file")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    /**
     * Share PDF using FileProvider.
     * 
     * Security Implementation:
     * - Uses FileProvider to generate content:// URI (not file://)
     * - Adds FLAG_GRANT_READ_URI_PERMISSION for temporary access
     * - Creates ACTION_SEND intent with PDF mime type
     * - Catches IllegalArgumentException for file provider issues
     */
    fun sharePdf(context: Context) {
        viewModelScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    pdfRepository.getPdfFile()
                }

                if (pdfFile == null) {
                    _uiState.value = UiState.Error("PDF file not set")
                    return@launch
                }

                if (!pdfFile.exists()) {
                    _uiState.value = UiState.Error("PDF file not found")
                    return@launch
                }

                if (!pdfFile.canRead()) {
                    _uiState.value = UiState.Error("Cannot read PDF file")
                    return@launch
                }

                // Use FileProvider to get a content:// URI for secure sharing
                val uri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        pdfFile
                    )
                } catch (e: IllegalArgumentException) {
                    _uiState.value = UiState.Error("File provider configuration error: ${e.message}")
                    return@launch
                }

                // Create share intent with proper flags
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "application/pdf"
                    // Grant read permission for the receiving app
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Launch system chooser
                context.startActivity(
                    Intent.createChooser(shareIntent, "Share PDF")
                )
            } catch (e: SecurityException) {
                _uiState.value = UiState.Error("Security error while sharing: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to share PDF: ${e.message}")
            }
        }
    }

    /**
     * Clean up resources when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        pdfRepository.cleanup()
    }
}
