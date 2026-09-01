package com.py2pdf.domain.usecase

import android.graphics.Bitmap
import com.py2pdf.data.repository.PdfRepository
import java.io.File
import java.io.IOException

/**
 * Use case for rendering PDF files.
 *
 * Responsibilities:
 * - Validate PDF file before rendering
 * - Execute PDF rendering through repository
 * - Handle and transform exceptions for domain layer
 */
class RenderPdfUseCase(
    private val pdfRepository: PdfRepository
) {
    /**
     * Execute the PDF rendering use case.
     *
     * @param pdfFile The PDF file to render
     * @return List of Bitmap objects representing each page
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if file is invalid
     */
    @Throws(IOException::class)
    suspend operator fun invoke(pdfFile: File): List<Bitmap> {
        // Validate file exists
        if (!pdfFile.exists()) {
            throw IOException("PDF file does not exist: ${pdfFile.absolutePath}")
        }
        
        // Validate file is readable
        if (!pdfFile.canRead()) {
            throw IOException("PDF file is not readable: ${pdfFile.absolutePath}")
        }

        // Validate file has PDF extension or content
        if (!pdfFile.name.endsWith(".pdf", ignoreCase = true)) {
            throw IOException("File does not appear to be a PDF: ${pdfFile.name}")
        }

        // Set file and load pages
        pdfRepository.setPdfFile(pdfFile)
        return pdfRepository.loadPdfPages()
    }
}
