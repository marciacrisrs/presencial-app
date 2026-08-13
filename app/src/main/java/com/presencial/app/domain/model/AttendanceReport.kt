package com.presencial.app.domain.model

import java.time.LocalDate
import java.time.YearMonth

data class AttendanceReportRow(
    val date: LocalDate,
    val dayOfWeekLabel: String,
    val statusLabel: String,
    val isHoliday: Boolean,
    val isWorkday: Boolean,
    val holidayName: String?
)

data class AttendanceReportFooter(
    val workdays: Int,
    val requiredDays: Int,
    val completedDays: Int,
    val requiredPercentage: Int,
    val achievedPercentage: Float,
    val exportedAt: LocalDate
)

data class AttendanceReport(
    val yearMonth: YearMonth,
    val rows: List<AttendanceReportRow>,
    val footer: AttendanceReportFooter
)
