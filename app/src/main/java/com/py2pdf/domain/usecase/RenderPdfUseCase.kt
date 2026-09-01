package com.py2pdf.domain.usecase

import android.graphics.Bitmap
import com.py2pdf.data.repository.PdfRepository
import java.io.File
import java.io.IOException

class RenderPdfUseCase(
    private val pdfRepository: PdfRepository
) {
    /**
     * Execute the PDF rendering use case.
     * @param pdfFile The PDF file to render
     * @return List of Bitmap objects representing each page
     * @throws IOException if file operations fail
     */
    @Throws(IOException::class)
    suspend operator fun invoke(pdfFile: File): List<Bitmap> {
        if (!pdfFile.exists()) {
            throw IOException("PDF file does not exist: ${pdfFile.absolutePath}")
        }
        
        if (!pdfFile.canRead()) {
            throw IOException("PDF file is not readable: ${pdfFile.absolutePath}")
        }

        pdfRepository.setPdfFile(pdfFile)
        return pdfRepository.loadPdfPages()
    }
}
