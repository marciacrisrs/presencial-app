package com.presencial.app.data.export

import com.presencial.app.domain.model.AttendanceReport
import com.presencial.app.domain.model.AttendanceReportFooter
import com.presencial.app.domain.model.AttendanceReportRow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.YearMonth

class ExcelExporterTest {

    private val exporter = ExcelExporter()

    @Test
    fun `export creates xlsx bytes`() {
        val report = AttendanceReport(
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
        val output = ByteArrayOutputStream()

        val result = exporter.export(report, output)

        assertTrue(result.isSuccess)
        val bytes = output.toByteArray()
        assertTrue(bytes.size > 100)
        assertTrue(bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte())
    }
}
