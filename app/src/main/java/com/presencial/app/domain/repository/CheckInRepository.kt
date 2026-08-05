package com.presencial.app.domain.repository

import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface CheckInRepository {
    fun observeCheckInsForMonth(yearMonth: YearMonth): Flow<List<CheckIn>>
    fun observeAllCheckIns(): Flow<List<CheckIn>>
    suspend fun getCheckIn(date: LocalDate): CheckIn?
    suspend fun saveCheckIn(date: LocalDate, status: DayStatus)
    suspend fun deleteCheckIn(date: LocalDate)
    suspend fun getCheckInsBetween(start: LocalDate, end: LocalDate): List<CheckIn>
}
