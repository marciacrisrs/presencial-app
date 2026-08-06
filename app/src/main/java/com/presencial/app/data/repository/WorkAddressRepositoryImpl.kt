package com.presencial.app.data.repository

import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.data.local.entity.WorkAddressEntity
import com.presencial.app.domain.model.WorkAddress
import com.presencial.app.domain.repository.WorkAddressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkAddressRepositoryImpl @Inject constructor(
    private val workAddressDao: WorkAddressDao
) : WorkAddressRepository {

    override fun getAllAddresses(): Flow<List<WorkAddress>> =
        workAddressDao.getAllAddresses().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getActiveAddresses(): List<WorkAddress> =
        workAddressDao.getActiveAddresses().map { it.toDomain() }

    override suspend fun insertAddress(address: WorkAddress) {
        workAddressDao.insertAddress(address.toEntity())
    }

    override suspend fun updateAddress(address: WorkAddress) {
        workAddressDao.updateAddress(address.toEntity())
    }

    override suspend fun deleteAddress(address: WorkAddress) {
        workAddressDao.deleteAddress(address.toEntity())
    }

    override suspend fun getAddressById(id: Long): WorkAddress? =
        workAddressDao.getAddressById(id)?.toDomain()

    private fun WorkAddressEntity.toDomain() = WorkAddress(
        id = id,
        name = name,
        addressText = addressText,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        isActive = isActive
    )

    private fun WorkAddress.toEntity() = WorkAddressEntity(
        id = id,
        name = name,
        addressText = addressText,
        latitude = latitude,
        longitude = longitude,
        radius = radius,
        isActive = isActive
    )
}
