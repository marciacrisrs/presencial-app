package com.presencial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.presencial.app.data.local.entity.AbsenceEntity
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import com.presencial.app.data.local.entity.WorkAddressEntity

@Dao
abstract class BackupDao {

    @Query("DELETE FROM check_ins")
    protected abstract suspend fun deleteAllCheckIns()

    @Query("DELETE FROM monthly_summaries")
    protected abstract suspend fun deleteAllMonthlySummaries()

    @Query("DELETE FROM work_addresses")
    protected abstract suspend fun deleteAllWorkAddresses()

    @Query("DELETE FROM absences")
    protected abstract suspend fun deleteAllAbsences()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertCheckIns(entities: List<CheckInEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertMonthlySummaries(entities: List<MonthlySummaryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertWorkAddresses(entities: List<WorkAddressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAbsences(entities: List<AbsenceEntity>)

    @Transaction
    open suspend fun restoreAll(
        checkIns: List<CheckInEntity>,
        summaries: List<MonthlySummaryEntity>,
        workAddresses: List<WorkAddressEntity>,
        absences: List<AbsenceEntity>
    ) {
        deleteAllCheckIns()
        deleteAllMonthlySummaries()
        deleteAllWorkAddresses()
        deleteAllAbsences()
        insertCheckIns(checkIns)
        insertMonthlySummaries(summaries)
        insertWorkAddresses(workAddresses)
        insertAbsences(absences)
    }
}
