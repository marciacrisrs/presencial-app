package com.presencial.app.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.presencial.app.util.TestDataFactory
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.YearMonth

class PdfExporterTest {

    private val context: Context = mockk()
    private lateinit var pdfExporter: PdfExporter

    @BeforeEach
    fun setup() {
        pdfExporter = PdfExporter(context)
        
        // Mock Android graphics classes
        mockkConstructor(PdfDocument::class)
        mockkConstructor(PdfDocument.PageInfo.Builder::class)
        mockkConstructor(Paint::class)
    }

    @Test
    fun `when exportMonthlySummary, then generate PDF document`() {
        // Arrange
        val summary = TestDataFactory.createMonthlySummary(yearMonth = YearMonth.of(2026, 8))
        val outputStream = ByteArrayOutputStream()
        
        val pageInfo: PdfDocument.PageInfo = mockk(relaxed = true)
        val page: PdfDocument.Page = mockk(relaxed = true)
        val canvas: Canvas = mockk(relaxed = true)
        
        every { anyConstructed<PdfDocument>().startPage(any()) } returns page
        every { page.canvas } returns canvas
        every { anyConstructed<PdfDocument.PageInfo.Builder>().create() } returns pageInfo
        
        // Act
        val result = pdfExporter.exportMonthlySummary(outputStream, summary)
        
        // Assert
        assertTrue(result.isSuccess)
        verify { anyConstructed<PdfDocument>().startPage(any()) }
        verify { anyConstructed<PdfDocument>().finishPage(any()) }
        verify { anyConstructed<PdfDocument>().writeTo(any()) }
        verify { anyConstructed<PdfDocument>().close() }
    }

    @Test
    fun `when exportStatistics, then generate PDF document`() {
        // Arrange
        val summaries = listOf(TestDataFactory.createMonthlySummary())
        val outputStream = ByteArrayOutputStream()
        
        val page: PdfDocument.Page = mockk(relaxed = true)
        val canvas: Canvas = mockk(relaxed = true)
        
        every { anyConstructed<PdfDocument>().startPage(any()) } returns page
        every { page.canvas } returns canvas
        
        // Act
        val result = pdfExporter.exportStatistics(outputStream, summaries, 50f, 10, 5)
        
        // Assert
        assertTrue(result.isSuccess)
        verify { anyConstructed<PdfDocument>().writeTo(any()) }
    }
}
