package com.py2pdf.data.repository

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.IOException

class PdfRepository {
    private var currentPdfFile: File? = null
    private var pdfRenderer: PdfRenderer? = null

    /**
     * Load PDF pages and convert them to Bitmaps.
     * Properly manages PdfRenderer lifecycle and closes file descriptors.
     */
    @Throws(IOException::class)
    fun loadPdfPages(): List<Bitmap> {
        val pdfFile = currentPdfFile ?: throw IOException("PDF file not set")
        
        if (!pdfFile.exists() || !pdfFile.isFile) {
            throw IOException("PDF file does not exist or is not a file")
        }

        var fileDescriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        val bitmaps = mutableListOf<Bitmap>()

        try {
            fileDescriptor = ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            renderer = PdfRenderer(fileDescriptor)
            val pageCount = renderer.pageCount

            // Render each page to a bitmap
            for (pageIndex in 0 until pageCount) {
                val page = renderer.openPage(pageIndex)
                val bitmap = Bitmap.createBitmap(
                    page.width,
                    page.height,
                    Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
        } finally {
            // Ensure resources are properly closed
            renderer?.close()
            fileDescriptor?.close()
        }

        return bitmaps
    }

    /**
     * Set the PDF file to load.
     */
    fun setPdfFile(file: File) {
        // Close existing renderer if any
        pdfRenderer?.close()
        pdfRenderer = null
        currentPdfFile = file
    }

    /**
     * Get the current PDF file.
     */
    fun getPdfFile(): File? = currentPdfFile

    /**
     * Clean up resources.
     */
    fun cleanup() {
        pdfRenderer?.close()
        pdfRenderer = null
        currentPdfFile = null
    }
}
