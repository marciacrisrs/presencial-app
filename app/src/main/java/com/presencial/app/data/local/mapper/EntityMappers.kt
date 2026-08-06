package com.presencial.app.data.local.mapper

import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import com.presencial.app.data.local.entity.AbsenceEntity
import com.presencial.app.data.local.entity.WorkAddressEntity
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.model.MonthlySummary
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.domain.model.WorkAddress
import java.time.LocalDate
import java.time.YearMonth

fun CheckInEntity.toDomain(): CheckIn = CheckIn(
    date = LocalDate.ofEpochDay(dateEpochDay),
    status = DayStatus.valueOf(status),
    updatedAt = updatedAt,
    source = source
)

fun CheckIn.toEntity(): CheckInEntity = CheckInEntity(
    dateEpochDay = date.toEpochDay(),
    status = status.name,
    updatedAt = updatedAt,
    source = source
)

fun MonthlySummaryEntity.toDomain(): MonthlySummary = MonthlySummary(
    yearMonth = YearMonth.parse(yearMonthKey),
    workdays = workdays,
    requiredDays = requiredDays,
    completedDays = completedDays,
    homeOfficeDays = homeOfficeDays,
    requiredPercentage = requiredPercentage,
    achievedPercentage = achievedPercentage
)

fun MonthlySummary.toEntity(): MonthlySummaryEntity = MonthlySummaryEntity(
    yearMonthKey = yearMonth.toString(),
    workdays = workdays,
    requiredDays = requiredDays,
    completedDays = completedDays,
    homeOfficeDays = homeOfficeDays,
    requiredPercentage = requiredPercentage,
    achievedPercentage = achievedPercentage
)

fun AbsenceEntity.toDomain() = Absence(
    id = id,
    type = AbsenceType.valueOf(type),
    startDate = LocalDate.ofEpochDay(startDateEpochDay),
    endDate = LocalDate.ofEpochDay(endDateEpochDay),
    isFullDay = isFullDay,
    hours = hours,
    notes = notes,
    isCounted = isCounted
)

fun Absence.toEntity() = AbsenceEntity(
    id = id,
    type = type.name,
    startDateEpochDay = startDate.toEpochDay(),
    endDateEpochDay = endDate.toEpochDay(),
    isFullDay = isFullDay,
    hours = hours,
    notes = notes,
    isCounted = isCounted
)

fun WorkAddressEntity.toDomain() = WorkAddress(
    id = id,
    name = name,
    addressText = addressText,
    latitude = latitude,
    longitude = longitude,
    radius = radius,
    isActive = isActive
)

fun WorkAddress.toEntity() = WorkAddressEntity(
    id = id,
    name = name,
    addressText = addressText,
    latitude = latitude,
    longitude = longitude,
    radius = radius,
    isActive = isActive
)

