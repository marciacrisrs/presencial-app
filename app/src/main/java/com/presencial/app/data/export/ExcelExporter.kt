package com.presencial.app.data.export

import com.presencial.app.domain.model.AttendanceReport
import org.dhatim.fastexcel.Workbook
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExporter @Inject constructor() {

    fun export(report: AttendanceReport, outputStream: OutputStream): Result<Unit> = runCatching {
        val locale = Locale.getDefault()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
        val exportDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)

        Workbook(outputStream, APP_NAME, APP_VERSION).use { workbook ->
            val sheet = workbook.newWorksheet(SHEET_NAME)
            COLUMN_HEADERS.forEachIndexed { column, header ->
                sheet.value(0, column, header)
                sheet.style(0, column).bold().set()
            }

            report.rows.forEachIndexed { index, row ->
                val rowIndex = index + DATA_ROW_OFFSET
                sheet.value(rowIndex, COL_DATE, dateFormatter.format(row.date))
                sheet.value(rowIndex, COL_DAY_OF_WEEK, row.dayOfWeekLabel)
                sheet.value(rowIndex, COL_STATUS, row.statusLabel)
                sheet.value(rowIndex, COL_HOLIDAY, booleanLabel(row.isHoliday))
                sheet.value(rowIndex, COL_WORKDAY, booleanLabel(row.isWorkday))
            }

            val footerStart = report.rows.size + FOOTER_ROW_OFFSET
            val footer = report.footer
            writeFooterRow(sheet, footerStart + FOOTER_WORKDAYS, "Dias úteis", footer.workdays.toDouble())
            writeFooterRow(
                sheet,
                footerStart + FOOTER_REQUIRED,
                "Meta configurada",
                "${footer.requiredDays} dias (${footer.requiredPercentage}%)"
            )
            writeFooterRow(
                sheet,
                footerStart + FOOTER_COMPLETED,
                "Dias presenciais",
                footer.completedDays.toDouble()
            )
            writeFooterRow(
                sheet,
                footerStart + FOOTER_ACHIEVED,
                "Percentual atingido",
                "${"%.1f".format(footer.achievedPercentage)}%"
            )
            writeFooterRow(
                sheet,
                footerStart + FOOTER_EXPORT_DATE,
                "Data da exportação",
                exportDateFormatter.format(footer.exportedAt)
            )
            sheet.finish()
        }
    }

    private fun writeFooterRow(
        sheet: org.dhatim.fastexcel.Worksheet,
        rowIndex: Int,
        label: String,
        value: String
    ) {
        sheet.value(rowIndex, COL_LABEL, label)
        sheet.value(rowIndex, COL_VALUE, value)
    }

    private fun writeFooterRow(
        sheet: org.dhatim.fastexcel.Worksheet,
        rowIndex: Int,
        label: String,
        value: Number
    ) {
        sheet.value(rowIndex, COL_LABEL, label)
        sheet.value(rowIndex, COL_VALUE, value)
    }

    private fun booleanLabel(value: Boolean): String = if (value) "Sim" else "Não"

    companion object {
        private const val APP_NAME = "Presencial"
        private const val APP_VERSION = "1.0"
        private const val SHEET_NAME = "Comparecimento"
        private const val DATA_ROW_OFFSET = 1
        private const val FOOTER_ROW_OFFSET = 2
        private const val FOOTER_WORKDAYS = 0
        private const val FOOTER_REQUIRED = 1
        private const val FOOTER_COMPLETED = 2
        private const val FOOTER_ACHIEVED = 3
        private const val FOOTER_EXPORT_DATE = 4
        private const val COL_DATE = 0
        private const val COL_DAY_OF_WEEK = 1
        private const val COL_STATUS = 2
        private const val COL_HOLIDAY = 3
        private const val COL_WORKDAY = 4
        private const val COL_LABEL = 0
        private const val COL_VALUE = 1
        private val COLUMN_HEADERS = listOf(
            "Data",
            "Dia da semana",
            "Status",
            "Feriado",
            "Dia útil"
        )
    }
}
