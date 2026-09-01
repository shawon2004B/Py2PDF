package com.py2pdf.presentation

import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.py2pdf.R
import com.py2pdf.presentation.viewmodel.PdfPreviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main PDF Preview Screen composable.
 * Handles rendering PDF pages and sharing functionality.
 *
 * State Management:
 * - Uses ViewModel for UI state management
 * - Non-blocking IO operations on Dispatchers.IO
 * - Proper coroutine scoping with viewModelScope
 */
@Composable
fun PdfPreviewScreen(
    viewModel: PdfPreviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val uiState by viewModel.uiState.collectAsState()

    // Load PDF pages when screen is composed
    LaunchedEffect(Unit) {
        viewModel.loadPdfPages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Preview") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch(Dispatchers.Main) {
                                viewModel.sharePdf(context)
                            }
                        },
                        enabled = uiState !is PdfPreviewViewModel.UiState.Error
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_pdf)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (val state = uiState) {
                is PdfPreviewViewModel.UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PdfPreviewViewModel.UiState.Success -> {
                    PdfPagesColumn(
                        pages = state.pages,
                        scrollState = scrollState
                    )
                }
                is PdfPreviewViewModel.UiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is PdfPreviewViewModel.UiState.Idle -> {
                    EmptyPdfPlaceholder()
                }
            }
        }
    }
}

/**
 * Displays PDF pages in a vertically scrollable column.
 * Each page is rendered as a Bitmap and displayed in a Card.
 *
 * Memory Management:
 * - Bitmaps are managed by the ViewModel and properly recycled
 * - UI composition doesn't create additional bitmap copies
 */
@Composable
fun PdfPagesColumn(
    pages: List<android.graphics.Bitmap>,
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    if (pages.isEmpty()) {
        EmptyPdfPlaceholder()
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        pages.forEachIndexed { index, bitmap ->
            if (!bitmap.isRecycled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF Page ${index + 1}",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Page ${index + 1}",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(8.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays error message with icon.
 * Error handling for file access, permission, and rendering errors.
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Placeholder UI for when no PDF is loaded.
 */
@Composable
fun EmptyPdfPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No PDF loaded",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select a PDF to preview",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
