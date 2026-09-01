package com.py2pdf.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
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

    fun loadPdfPages() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val pages = withContext(Dispatchers.IO) {
                    pdfRepository.loadPdfPages()
                }
                _uiState.value = UiState.Success(pages)
            } catch (e: IOException) {
                _uiState.value = UiState.Error("Failed to load PDF: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun sharePdf(context: Context) {
        viewModelScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    pdfRepository.getPdfFile()
                }

                if (pdfFile == null || !pdfFile.exists()) {
                    _uiState.value = UiState.Error("PDF file not found")
                    return@launch
                }

                // Use FileProvider to get a content:// URI
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                // Create share intent
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "application/pdf"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Launch chooser
                context.startActivity(
                    Intent.createChooser(shareIntent, "Share PDF")
                )
            } catch (e: IllegalArgumentException) {
                _uiState.value = UiState.Error("File provider error: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to share PDF: ${e.message}")
            }
        }
    }
}
