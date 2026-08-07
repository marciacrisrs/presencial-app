package com.presencial.app.data.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.presencial.app.util.TestDataFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.YearMonth

@Suppress("WildcardImport")
class PdfExporterTest {

    private lateinit var pdfExporter: PdfExporter

    @BeforeEach
    fun setup() {
        pdfExporter = PdfExporter()
        
        // Mock Android graphics classes
        mockkConstructor(PdfDocument::class)
        mockkConstructor(PdfDocument.PageInfo.Builder::class)
        mockkConstructor(Paint::class)
        
        // Ensure constructor doesn't throw
        every { anyConstructed<PdfDocument.PageInfo.Builder>().create() } returns mockk(relaxed = true)
        every { anyConstructed<PdfDocument>().startPage(any()) } returns mockk(relaxed = true)
        every { anyConstructed<PdfDocument>().finishPage(any()) } returns Unit
        every { anyConstructed<PdfDocument>().writeTo(any()) } returns Unit
        every { anyConstructed<PdfDocument>().close() } returns Unit
        every { anyConstructed<Paint>().setTextSize(any()) } returns Unit
        every { anyConstructed<Paint>().setFakeBoldText(any()) } returns Unit
    }

    @Test
    fun `when exportMonthlySummary, then generate PDF document`() {
        // Arrange
        val summary = TestDataFactory.createMonthlySummary(yearMonth = YearMonth.of(2026, 8))
        val outputStream = ByteArrayOutputStream()
        
        val page: PdfDocument.Page = mockk(relaxed = true)
        val canvas: Canvas = mockk(relaxed = true)
        every { page.canvas } returns canvas
        every { anyConstructed<PdfDocument>().startPage(any()) } returns page
        
        // Act
        val result = pdfExporter.exportMonthlySummary(outputStream, summary)
        
        // Assert
        result.onFailure { throw it }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when exportStatistics, then generate PDF document`() {
        // Arrange
        val summaries = listOf(TestDataFactory.createMonthlySummary())
        val outputStream = ByteArrayOutputStream()
        
        val page: PdfDocument.Page = mockk(relaxed = true)
        val canvas: Canvas = mockk(relaxed = true)
        every { page.canvas } returns canvas
        every { anyConstructed<PdfDocument>().startPage(any()) } returns page
        
        // Act
        val result = pdfExporter.exportStatistics(outputStream, summaries, 50f, 10, 5)
        
        // Assert
        result.onFailure { throw it }
        assertTrue(result.isSuccess)
    }
}
