package com.presencial.app.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.presencial.app.domain.model.MonthlySummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun exportStatistics(
        outputStream: OutputStream,
        summaries: List<MonthlySummary>,
        averageAchieved: Float,
        totalPresencial: Int,
        totalHomeOffice: Int
    ): Result<Unit> = runCatching {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply { textSize = 14f }

        var y = 60f
        canvas.drawText("Presencial — Relatório de Estatísticas", 40f, y, titlePaint)
        y += 40f
        canvas.drawText("Média anual: ${"%.1f".format(averageAchieved)}%", 40f, y, bodyPaint)
        y += 24f
        canvas.drawText("Total presencial: $totalPresencial dias", 40f, y, bodyPaint)
        y += 24f
        canvas.drawText("Total home office: $totalHomeOffice dias", 40f, y, bodyPaint)
        y += 40f

        summaries.forEach { summary ->
            val monthName = summary.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
            canvas.drawText(
                "$monthName/${summary.yearMonth.year}: ${summary.completedDays}/${summary.requiredDays} dias (${"%.0f".format(summary.achievedPercentage)}%)",
                40f, y, bodyPaint
            )
            y += 22f
            if (y > 780f) y = 60f
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply { textSize = 22f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 14f }

        val monthName = summary.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
        var y = 60f
        canvas.drawText("Resumo — $monthName ${summary.yearMonth.year}", 40f, y, titlePaint)
        y += 36f
        canvas.drawText("Dias úteis: ${summary.workdays}", 40f, y, bodyPaint)
        y += 24f
        canvas.drawText("Meta: ${summary.requiredDays} dias (${summary.requiredPercentage}%)", 40f, y, bodyPaint)
        y += 24f
        canvas.drawText("Cumpridos: ${summary.completedDays}", 40f, y, bodyPaint)
        y += 24f
        canvas.drawText("Home office: ${summary.homeOfficeDays}", 40f, y, bodyPaint)
        y += 24f
        canvas.drawText("Percentual atingido: ${"%.1f".format(summary.achievedPercentage)}%", 40f, y, bodyPaint)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
