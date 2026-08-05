package com.presencial.app.domain.repository

import com.presencial.app.domain.model.Absence
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AbsenceRepository {
    fun getAllAbsences(): Flow<List<Absence>>
    fun getAbsencesInRange(start: LocalDate, end: LocalDate): Flow<List<Absence>>
    suspend fun insertAbsence(absence: Absence)
    suspend fun deleteAbsence(absence: Absence)
    suspend fun deleteById(id: Long)
}
