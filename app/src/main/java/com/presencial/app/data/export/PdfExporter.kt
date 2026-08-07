package com.presencial.app.data.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.presencial.app.domain.model.MonthlySummary
import java.io.OutputStream
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfExporter @Inject constructor() {

    fun exportStatistics(
        outputStream: OutputStream,
        summaries: List<MonthlySummary>,
        averageAchieved: Float,
        totalPresencial: Int,
        totalHomeOffice: Int
    ): Result<Unit> = runCatching {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            textSize = TITLE_TEXT_SIZE
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply { textSize = BODY_TEXT_SIZE }

        var y = INITIAL_Y
        canvas.drawText("Presencial — Relatório de Estatísticas", MARGIN_X, y, titlePaint)
        y += LINE_SPACING_LARGE
        canvas.drawText("Média anual: ${"%.1f".format(averageAchieved)}%", MARGIN_X, y, bodyPaint)
        y += LINE_SPACING_SMALL
        canvas.drawText("Total presencial: $totalPresencial dias", MARGIN_X, y, bodyPaint)
        y += LINE_SPACING_SMALL
        canvas.drawText("Total home office: $totalHomeOffice dias", MARGIN_X, y, bodyPaint)
        y += LINE_SPACING_LARGE

        summaries.forEach { summary ->
            val monthName = summary.yearMonth.month.getDisplayName(
                TextStyle.FULL, 
                Locale.forLanguageTag("pt-BR")
            )
            val text = "$monthName/${summary.yearMonth.year}: " +
                    "${summary.completedDays}/${summary.requiredDays} dias " +
                    "(${"%.0f".format(summary.achievedPercentage)}%)"
            
            canvas.drawText(text, MARGIN_X, y, bodyPaint)
            y += LINE_SPACING_SMALL
            if (y > PAGE_BREAK_Y) y = INITIAL_Y
        }

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    fun exportMonthlySummary(
        outputStream: OutputStream,
        summary: MonthlySummary
    ): Result<Unit> = runCatching {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply { textSize = TITLE_TEXT_SIZE_SMALL; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = BODY_TEXT_SIZE }

        val monthName = summary.yearMonth.month.getDisplayName(
            TextStyle.FULL, 
            Locale.forLanguageTag("pt-BR")
        )
        var y = INITIAL_Y
        canvas.drawText("Resumo — $monthName ${summary.yearMonth.year}", MARGIN_X, y, titlePaint)
        y += LINE_SPACING_MEDIUM
        canvas.drawText("Dias úteis: ${summary.workdays}", MARGIN_X, y, bodyPaint)
        y += LINE_SPACING_SMALL
        canvas.drawText(
            "Meta: ${summary.requiredDays} dias (${summary.requiredPercentage}%)",
            MARGIN_X, y, bodyPaint
        )
        y += LINE_SPACING_SMALL
        canvas.drawText("Cumpridos: ${summary.completedDays}", MARGIN_X, y, bodyPaint)
        y += LINE_SPACING_SMALL
        canvas.drawText("Home office: ${summary.homeOfficeDays}", MARGIN_X, y, bodyPaint)
        y += LINE_SPACING_SMALL
        canvas.drawText(
            "Percentual atingido: ${"%.1f".format(summary.achievedPercentage)}%",
            MARGIN_X, y, bodyPaint
        )

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val TITLE_TEXT_SIZE = 24f
        private const val TITLE_TEXT_SIZE_SMALL = 22f
        private const val BODY_TEXT_SIZE = 14f
        private const val MARGIN_X = 40f
        private const val INITIAL_Y = 60f
        private const val LINE_SPACING_SMALL = 22f
        private const val LINE_SPACING_MEDIUM = 36f
        private const val LINE_SPACING_LARGE = 40f
        private const val PAGE_BREAK_Y = 780f
    }
}
