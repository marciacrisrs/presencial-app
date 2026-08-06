package com.presencial.app.data.repository

import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.data.local.entity.AbsenceEntity
import com.presencial.app.data.local.mapper.toDomain
import com.presencial.app.data.local.mapper.toEntity
import com.presencial.app.domain.model.Absence
import com.presencial.app.domain.model.AbsenceType
import com.presencial.app.domain.repository.AbsenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class AbsenceRepositoryImpl @Inject constructor(
    private val absenceDao: AbsenceDao
) : AbsenceRepository {

    override fun getAllAbsences(): Flow<List<Absence>> =
        absenceDao.getAllAbsences().map { entities -> entities.map { it.toDomain() } }

    override fun getAbsencesInRange(start: LocalDate, end: LocalDate): Flow<List<Absence>> =
        absenceDao.getAbsencesInRange(start.toEpochDay(), end.toEpochDay())
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertAbsence(absence: Absence) {
        absenceDao.insertAbsence(absence.toEntity())
    }

    override suspend fun deleteAbsence(absence: Absence) {
        absenceDao.deleteAbsence(absence.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        absenceDao.deleteById(id)
    }
}
