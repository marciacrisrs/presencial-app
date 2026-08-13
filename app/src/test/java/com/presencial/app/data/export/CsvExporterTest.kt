package com.presencial.app.data.export

import com.presencial.app.domain.model.AttendanceReport
import com.presencial.app.domain.model.AttendanceReportFooter
import com.presencial.app.domain.model.AttendanceReportRow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.YearMonth

class CsvExporterTest {

    private val exporter = CsvExporter()

    @Test
    fun `export writes header rows and footer with semicolon separator`() {
        val report = sampleReport()
        val output = ByteArrayOutputStream()

        val result = exporter.export(report, output)

        assertTrue(result.isSuccess)
        val csv = output.toString(Charsets.UTF_8)
        assertTrue(csv.startsWith("\uFEFFData;Dia da semana;Status;Feriado;Dia útil"))
        assertTrue(csv.contains("03/08/2026;domingo;Presencial;Não;Sim"))
        assertTrue(csv.contains("Dias úteis;21"))
        assertTrue(csv.contains("Percentual atingido;71,4%") || csv.contains("Percentual atingido;71.4%"))
    }

    @Test
    fun `export escapes fields containing separator`() {
        val report = sampleReport().copy(
            rows = listOf(
                AttendanceReportRow(
                    date = LocalDate.of(2026, 8, 4),
                    dayOfWeekLabel = "segunda-feira",
                    statusLabel = "Feriado (Dia; Especial)",
                    isHoliday = true,
                    isWorkday = false,
                    holidayName = "Dia; Especial"
                )
            )
        )
        val output = ByteArrayOutputStream()

        val result = exporter.export(report, output)

        assertTrue(result.isSuccess)
        assertTrue(output.toString(Charsets.UTF_8).contains("\"Feriado (Dia; Especial)\""))
    }

    private fun sampleReport() = AttendanceReport(
        yearMonth = YearMonth.of(2026, 8),
        rows = listOf(
            AttendanceReportRow(
                date = LocalDate.of(2026, 8, 3),
                dayOfWeekLabel = "domingo",
                statusLabel = "Presencial",
                isHoliday = false,
                isWorkday = true,
                holidayName = null
            )
        ),
        footer = AttendanceReportFooter(
            workdays = 21,
            requiredDays = 7,
            completedDays = 5,
            requiredPercentage = 40,
            achievedPercentage = 71.4f,
            exportedAt = LocalDate.of(2026, 8, 12)
        )
    )
}
