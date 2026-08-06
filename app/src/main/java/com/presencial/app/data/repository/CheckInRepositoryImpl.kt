package com.presencial.app.data.repository

import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.mapper.toDomain
import com.presencial.app.data.local.mapper.toEntity
import com.presencial.app.domain.model.CheckIn
import com.presencial.app.domain.model.DayStatus
import com.presencial.app.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val checkInDao: CheckInDao
) : CheckInRepository {

    override fun observeCheckInsForMonth(yearMonth: YearMonth): Flow<List<CheckIn>> {
        val start = yearMonth.atDay(1).toEpochDay()
        val end = yearMonth.atEndOfMonth().toEpochDay()
        return checkInDao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllCheckIns(): Flow<List<CheckIn>> =
        checkInDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getCheckIn(date: LocalDate): CheckIn? =
        checkInDao.getByDate(date.toEpochDay())?.toDomain()

    override suspend fun saveCheckIn(date: LocalDate, status: DayStatus, source: String) {
        checkInDao.upsert(
            CheckIn(date = date, status = status, source = source).toEntity()
        )
    }

    override suspend fun deleteCheckIn(date: LocalDate) {
        checkInDao.deleteByDate(date.toEpochDay())
    }

    override suspend fun getCheckInsBetween(start: LocalDate, end: LocalDate): List<CheckIn> =
        checkInDao.getBetween(start.toEpochDay(), end.toEpochDay()).map { it.toDomain() }
}
