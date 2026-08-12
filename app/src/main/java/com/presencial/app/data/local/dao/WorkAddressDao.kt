package com.presencial.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.presencial.app.data.local.entity.WorkAddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkAddressDao {
    @Query("SELECT * FROM work_addresses")
    fun getAllAddresses(): Flow<List<WorkAddressEntity>>

    @Query("SELECT * FROM work_addresses WHERE isActive = 1")
    suspend fun getActiveAddresses(): List<WorkAddressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: WorkAddressEntity)

    @Update
    suspend fun updateAddress(address: WorkAddressEntity)

    @Delete
    suspend fun deleteAddress(address: WorkAddressEntity)

    @Query("SELECT * FROM work_addresses WHERE id = :id")
    suspend fun getAddressById(id: Long): WorkAddressEntity?

    @Query("SELECT * FROM work_addresses")
    suspend fun getAllAddressesSync(): List<WorkAddressEntity>

    @Query("DELETE FROM work_addresses")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(addresses: List<WorkAddressEntity>)
}
