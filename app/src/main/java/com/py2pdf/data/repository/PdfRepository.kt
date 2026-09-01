package com.py2pdf.data.repository

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.IOException

/**
 * Repository for PDF operations.
 *
 * Responsibilities:
 * - Load and render PDF pages using PdfRenderer
 * - Manage file descriptors and PdfRenderer lifecycle
 * - Handle resource cleanup and memory management
 * - Provide error handling for file operations
 *
 * Memory Management:
 * - PdfRenderer is properly closed in finally blocks
 * - ParcelFileDescriptor is closed after use
 * - Bitmaps are created and returned for UI layer to manage
 */
class PdfRepository {
    private var currentPdfFile: File? = null
    private var pdfRenderer: PdfRenderer? = null

    /**
     * Load PDF pages and convert them to Bitmaps.
     * Properly manages PdfRenderer lifecycle and closes file descriptors.
     *
     * @return List of Bitmap objects, one per PDF page
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if PDF is invalid
     */
    @Throws(IOException::class)
    fun loadPdfPages(): List<Bitmap> {
        val pdfFile = currentPdfFile
            ?: throw IOException("PDF file not set")
        
        if (!pdfFile.exists()) {
            throw IOException("PDF file does not exist: ${pdfFile.absolutePath}")
        }

        if (!pdfFile.isFile) {
            throw IOException("Path is not a file: ${pdfFile.absolutePath}")
        }

        if (!pdfFile.canRead()) {
            throw IOException("PDF file is not readable: ${pdfFile.absolutePath}")
        }

        var fileDescriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        val bitmaps = mutableListOf<Bitmap>()

        try {
            // Open file descriptor with read-only mode
            fileDescriptor = ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )

            // Create PdfRenderer
            renderer = PdfRenderer(fileDescriptor)
            val pageCount = renderer.pageCount

            if (pageCount <= 0) {
                throw IOException("PDF has no pages")
            }

            // Render each page to a bitmap
            for (pageIndex in 0 until pageCount) {
                var page: PdfRenderer.Page? = null
                try {
                    page = renderer.openPage(pageIndex)
                    
                    // Create bitmap with PDF page dimensions
                    val bitmap = Bitmap.createBitmap(
                        page.width,
                        page.height,
                        Bitmap.Config.ARGB_8888
                    )
                    
                    // Render page to bitmap
                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    
                    bitmaps.add(bitmap)
                } finally {
                    // Ensure page is closed
                    page?.close()
                }
            }
        } catch (e: IOException) {
            // Clean up bitmaps on error
            bitmaps.forEach { if (!it.isRecycled) it.recycle() }
            throw e
        } finally {
            // Ensure resources are properly closed
            renderer?.close()
            fileDescriptor?.close()
        }

        return bitmaps
    }

    /**
     * Set the PDF file to load.
     * Closes any existing renderer.
     */
    fun setPdfFile(file: File) {
        // Close existing resources
        cleanup()
        currentPdfFile = file
    }

    /**
     * Get the current PDF file.
     */
    fun getPdfFile(): File? = currentPdfFile

    /**
     * Clean up resources.
     * Called when PDF is no longer needed or ViewModel is cleared.
     */
    fun cleanup() {
        pdfRenderer?.close()
        pdfRenderer = null
        currentPdfFile = null
    }
}
