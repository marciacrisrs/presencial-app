package com.presencial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySummaryDao {
    @Query("SELECT * FROM monthly_summaries ORDER BY yearMonthKey DESC")
    fun observeAll(): Flow<List<MonthlySummaryEntity>>

    @Query("SELECT * FROM monthly_summaries WHERE yearMonthKey = :key LIMIT 1")
    suspend fun getByKey(key: String): MonthlySummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MonthlySummaryEntity)

    @Query("DELETE FROM monthly_summaries")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MonthlySummaryEntity>)
}
