package com.presencial.app.util

import com.presencial.app.data.local.entity.AbsenceEntity
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import com.presencial.app.data.local.entity.WorkAddressEntity
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DashboardData
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.usecase.StatisticsData
import java.time.LocalDate
import java.time.YearMonth

object TestDataFactory {

    fun createWorkAddress(
        id: Long = 1L,
        name: String = "Escritório",
        addressText: String = "Rua Teste, 123",
        latitude: Double = -23.5505,
        longitude: Double = -46.6333,
        radius: Float = 50f,
        isActive: Boolean = true,
        stateCode: String? = "SP",
        cityName: String? = "São Paulo"
    ) = WorkAddress(
        id, name, addressText, latitude, longitude, radius, isActive, stateCode, cityName
    )

    fun createWorkAddressEntity(
        id: Long = 1L,
        name: String = "Escritório",
        addressText: String = "Rua Teste, 123",
        latitude: Double = -23.5505,
        longitude: Double = -46.6333,
        radius: Float = 50f,
        isActive: Boolean = true,
        stateCode: String? = "SP",
        cityName: String? = "São Paulo"
    ) = WorkAddressEntity(
        id, name, addressText, latitude, longitude, radius, isActive, stateCode, cityName
    )

    fun createCheckIn(
        date: LocalDate = LocalDate.of(2026, 8, 6),
        status: DayStatus = DayStatus.PRESENCIAL,
        updatedAt: Long = 1722940800000L,
        source: String = "MANUAL"
    ) = CheckIn(date, status, updatedAt, source)

    fun createCheckInEntity(
        dateEpochDay: Long = LocalDate.of(2026, 8, 6).toEpochDay(),
        status: String = "PRESENCIAL",
        updatedAt: Long = 1722940800000L,
        source: String = "MANUAL"
    ) = CheckInEntity(dateEpochDay, status, updatedAt, source)

    fun createAbsence(
        id: Long = 1L,
        type: AbsenceType = AbsenceType.VACATION,
        startDate: LocalDate = LocalDate.of(2026, 8, 1),
        endDate: LocalDate = LocalDate.of(2026, 8, 5),
        isFullDay: Boolean = true,
        hours: Float = 8f,
        notes: String? = "Férias de agosto",
        isCounted: Boolean = false
    ) = Absence(id, type, startDate, endDate, isFullDay, hours, notes, isCounted)

    fun createAbsenceEntity(
        id: Long = 1L,
        type: String = "VACATION",
        startDateEpochDay: Long = LocalDate.of(2026, 8, 1).toEpochDay(),
        endDateEpochDay: Long = LocalDate.of(2026, 8, 5).toEpochDay(),
        isFullDay: Boolean = true,
        hours: Float = 8f,
        notes: String? = "Férias de agosto",
        isCounted: Boolean = false
    ) = AbsenceEntity(id, type, startDateEpochDay, endDateEpochDay, isFullDay, hours, notes, isCounted)

    fun createMonthlySummary(
        yearMonth: YearMonth = YearMonth.of(2026, 8),
        workdays: Int = 22,
        requiredDays: Int = 9,
        completedDays: Int = 5,
        homeOfficeDays: Int = 3,
        requiredPercentage: Int = 40,
        achievedPercentage: Float = 55.5f
    ) = MonthlySummary(
        yearMonth,
        workdays,
        requiredDays,
        completedDays,
        homeOfficeDays,
        requiredPercentage,
        achievedPercentage
    )

    fun createMonthlySummaryEntity(
        yearMonthKey: String = "2026-08",
        workdays: Int = 22,
        requiredDays: Int = 9,
        completedDays: Int = 5,
        homeOfficeDays: Int = 3,
        requiredPercentage: Int = 40,
        achievedPercentage: Float = 55.5f
    ) = MonthlySummaryEntity(
        yearMonthKey,
        workdays,
        requiredDays,
        completedDays,
        homeOfficeDays,
        requiredPercentage,
        achievedPercentage
    )

    fun createDashboardData(
        yearMonth: YearMonth = YearMonth.of(2026, 8),
        totalDays: Int = 31,
        workdays: Int = 22,
        requiredDays: Int = 9,
        completedDays: Int = 5,
        remainingDays: Int = 4,
        homeOfficeDays: Int = 3,
        achievedPercentage: Float = 55.5f,
        requiredPercentage: Int = 40,
        progressFraction: Float = 0.5f,
        smartMessage: String = "Boa!",
        countSaturdays: Boolean = false,
        todayIsPresencial: Boolean = true,
        todayIsWorkday: Boolean = true,
        yesterdayIsPending: Boolean = false,
        streak: Int = 3
    ) = DashboardData(
        yearMonth,
        totalDays,
        workdays,
        requiredDays,
        completedDays,
        remainingDays,
        homeOfficeDays,
        achievedPercentage,
        requiredPercentage,
        progressFraction,
        smartMessage,
        countSaturdays,
        todayIsPresencial,
        todayIsWorkday,
        yesterdayIsPending,
        streak
    )

    fun createStatisticsData(
        monthlySummaries: List<MonthlySummary> = emptyList(),
        averageAchieved: Float = 50f,
        totalPresencial: Int = 10,
        totalHomeOffice: Int = 10,
        longestStreak: Int = 5,
        currentStreak: Int = 2
    ) = StatisticsData(
        monthlySummaries,
        averageAchieved,
        totalPresencial,
        totalHomeOffice,
        longestStreak,
        currentStreak
    )
}
