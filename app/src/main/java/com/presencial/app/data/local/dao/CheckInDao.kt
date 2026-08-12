package com.presencial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.presencial.app.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins WHERE dateEpochDay >= :start AND dateEpochDay <= :end ORDER BY dateEpochDay")
    fun observeBetween(start: Long, end: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE dateEpochDay = :epochDay LIMIT 1")
    suspend fun getByDate(epochDay: Long): CheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CheckInEntity)

    @Query("DELETE FROM check_ins WHERE dateEpochDay = :epochDay")
    suspend fun deleteByDate(epochDay: Long)

    @Query("SELECT * FROM check_ins WHERE dateEpochDay >= :start AND dateEpochDay <= :end")
    suspend fun getBetween(start: Long, end: Long): List<CheckInEntity>

    @Query("DELETE FROM check_ins")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CheckInEntity>)

    @Query(
        """
        SELECT COUNT(*) FROM check_ins
        WHERE dateEpochDay >= :start AND dateEpochDay <= :end
        AND (source = :autoSource OR source = :legacySource)
        """
    )
    suspend fun countAutoGeofenceBetween(
        start: Long,
        end: Long,
        autoSource: String,
        legacySource: String
    ): Int
}
