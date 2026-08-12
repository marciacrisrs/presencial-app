package com.presencial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.presencial.app.data.local.entity.AbsenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AbsenceDao {
    @Query("SELECT * FROM absences ORDER BY startDateEpochDay DESC")
    fun getAllAbsences(): Flow<List<AbsenceEntity>>

    @Query("DELETE FROM absences")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(absences: List<AbsenceEntity>)

    @Query("SELECT * FROM absences WHERE (startDateEpochDay <= :end AND endDateEpochDay >= :start)")
    fun getAbsencesInRange(start: Long, end: Long): Flow<List<AbsenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsence(absence: AbsenceEntity)

    @Delete
    suspend fun deleteAbsence(absence: AbsenceEntity)

    @Query("DELETE FROM absences WHERE id = :id")
    suspend fun deleteById(id: Long)
}
