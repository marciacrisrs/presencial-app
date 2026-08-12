package com.presencial.app.domain.repository

import com.presencial.app.domain.model.WorkAddress
import kotlinx.coroutines.flow.Flow

interface WorkAddressRepository {
    fun getAllAddresses(): Flow<List<WorkAddress>>
    suspend fun getActiveAddresses(): List<WorkAddress>
    suspend fun insertAddress(address: WorkAddress)
    suspend fun updateAddress(address: WorkAddress)
    suspend fun deleteAddress(address: WorkAddress)
    suspend fun getAddressById(id: Long): WorkAddress?
    suspend fun getAllAddressesSnapshot(): List<WorkAddress>
}
