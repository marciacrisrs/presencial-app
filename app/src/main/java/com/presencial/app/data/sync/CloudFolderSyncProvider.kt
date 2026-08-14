package com.presencial.app.data.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.presencial.app.di.IoDispatcher
import com.presencial.app.domain.model.CloudStorageProvider
import com.presencial.app.domain.sync.CloudSyncProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudFolderSyncProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CloudSyncProvider {

    override suspend fun isSignedIn(): Boolean = getTreeUri() != null

    override suspend fun signOut() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.TREE_URI)
            prefs.remove(Keys.PROVIDER)
            prefs.remove(Keys.FOLDER_LABEL)
        }
    }

    override suspend fun getAccountEmail(): String? =
        dataStore.data.map { it[Keys.FOLDER_LABEL] }.first()

    override suspend fun uploadBackup(fileName: String, content: ByteArray): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                val treeUri = getTreeUri() ?: error("Selecione uma pasta na nuvem")
                val resolver = context.contentResolver
                val backupUri = CloudFolderDocuments.findBackupUri(resolver, treeUri, fileName)
                    ?: CloudFolderDocuments.createBackupUri(resolver, treeUri, fileName)
                CloudFolderDocuments.writeBackupContent(resolver, backupUri, content)
            }
        }

    override suspend fun downloadBackup(fileName: String): Result<ByteArray> =
        withContext(ioDispatcher) {
            runCatching {
                val treeUri = getTreeUri() ?: error("Selecione uma pasta na nuvem")
                val resolver = context.contentResolver
                val backupUri = CloudFolderDocuments.findBackupUri(resolver, treeUri, fileName)
                    ?: error("Nenhum backup encontrado na pasta")
                resolver.openInputStream(backupUri)?.use { it.readBytes() }
                    ?: error("Não foi possível ler o backup")
            }
        }

    suspend fun connectFolder(treeUri: Uri, selectedProvider: CloudStorageProvider) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val resolver = context.contentResolver
        runCatching {
            resolver.takePersistableUriPermission(treeUri, flags)
        }.getOrElse { error("Não foi possível acessar a pasta selecionada") }
        require(CloudFolderDocuments.hasPersistedPermission(resolver, treeUri)) {
            "Permissão da pasta não concedida"
        }
        val label = CloudFolderDocuments.readTreeDisplayName(resolver, treeUri)
            ?: selectedProvider.displayName
        dataStore.edit { prefs ->
            prefs[Keys.TREE_URI] = treeUri.toString()
            prefs[Keys.PROVIDER] = selectedProvider.name
            prefs[Keys.FOLDER_LABEL] = label
        }
    }

    suspend fun isFolderAccessible(): Boolean {
        val treeUri = getTreeUri() ?: return false
        return CloudFolderDocuments.hasPersistedPermission(context.contentResolver, treeUri)
    }

    suspend fun selectedProvider(): CloudStorageProvider {
        val name = dataStore.data.map { it[Keys.PROVIDER] }.first()
        return CloudStorageProvider.entries.firstOrNull { it.name == name }
            ?: CloudStorageProvider.GOOGLE_DRIVE
    }

    suspend fun setSelectedProvider(provider: CloudStorageProvider) {
        dataStore.edit { prefs ->
            prefs[Keys.PROVIDER] = provider.name
        }
    }

    private suspend fun getTreeUri(): Uri? {
        val value = dataStore.data.map { it[Keys.TREE_URI] }.first() ?: return null
        return Uri.parse(value)
    }

    private object Keys {
        val TREE_URI = stringPreferencesKey("cloud_sync_tree_uri")
        val PROVIDER = stringPreferencesKey("cloud_sync_provider")
        val FOLDER_LABEL = stringPreferencesKey("cloud_sync_folder_label")
    }

    companion object {
        const val BACKUP_FILE_NAME = "presencial_backup.json"
    }
}
