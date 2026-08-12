package com.presencial.app.domain.sync

interface CloudSyncProvider {
    suspend fun isSignedIn(): Boolean

    suspend fun signOut()

    suspend fun getAccountEmail(): String?

    suspend fun uploadBackup(fileName: String, content: ByteArray): Result<Unit>

    suspend fun downloadBackup(fileName: String): Result<ByteArray>
}
