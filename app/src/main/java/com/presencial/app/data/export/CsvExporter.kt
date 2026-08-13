package com.presencial.app.data.export

import com.presencial.app.domain.model.AttendanceReport
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor() {

    fun export(report: AttendanceReport, outputStream: OutputStream): Result<Unit> = runCatching {
        val locale = Locale.getDefault()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
        val exportDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
        outputStream.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            writer.write(UTF8_BOM)
            writer.appendLine(COLUMN_HEADERS.joinToString(COLUMN_SEPARATOR))
            report.rows.forEach { row ->
                writer.appendLine(
                    listOf(
                        dateFormatter.format(row.date),
                        row.dayOfWeekLabel,
                        row.statusLabel,
                        booleanLabel(row.isHoliday),
                        booleanLabel(row.isWorkday)
                    ).joinToString(COLUMN_SEPARATOR) { escapeCsvField(it) }
                )
            }
            writer.appendLine()
            appendFooter(writer, report.footer, exportDateFormatter)
        }
    }

    private fun appendFooter(
        writer: java.io.BufferedWriter,
        footer: com.presencial.app.domain.model.AttendanceReportFooter,
        exportDateFormatter: DateTimeFormatter
    ) {
        writer.appendLine("Dias úteis${COLUMN_SEPARATOR}${footer.workdays}")
        writer.appendLine(
            "Meta configurada${COLUMN_SEPARATOR}${footer.requiredDays} dias " +
                "(${footer.requiredPercentage}%)"
        )
        writer.appendLine("Dias presenciais${COLUMN_SEPARATOR}${footer.completedDays}")
        writer.appendLine("Percentual atingido${COLUMN_SEPARATOR}${"%.1f".format(footer.achievedPercentage)}%")
        writer.appendLine("Data da exportação${COLUMN_SEPARATOR}${exportDateFormatter.format(footer.exportedAt)}")
    }

    private fun booleanLabel(value: Boolean): String = if (value) "Sim" else "Não"

    private fun escapeCsvField(value: String): String {
        if (value.contains(COLUMN_SEPARATOR) || value.contains('"') || value.contains('\n')) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    companion object {
        private const val UTF8_BOM = "\uFEFF"
        private const val COLUMN_SEPARATOR = ";"
        private val COLUMN_HEADERS = listOf(
            "Data",
            "Dia da semana",
            "Status",
            "Feriado",
            "Dia útil"
        )
    }
}
