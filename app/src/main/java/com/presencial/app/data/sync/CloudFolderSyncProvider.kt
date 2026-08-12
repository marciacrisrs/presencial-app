package com.presencial.app.data.sync

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
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
                val backupUri = findBackupUri(resolver, treeUri, fileName)
                    ?: createBackupUri(resolver, treeUri, fileName)
                resolver.openOutputStream(backupUri, "wt")?.use { stream ->
                    stream.write(content)
                } ?: error("Não foi possível gravar o backup na pasta")
            }
        }

    override suspend fun downloadBackup(fileName: String): Result<ByteArray> =
        withContext(ioDispatcher) {
            runCatching {
                val treeUri = getTreeUri() ?: error("Selecione uma pasta na nuvem")
                val backupUri = findBackupUri(context.contentResolver, treeUri, fileName)
                    ?: error("Nenhum backup encontrado na pasta")
                context.contentResolver.openInputStream(backupUri)?.use { it.readBytes() }
                    ?: error("Não foi possível ler o backup")
            }
        }

    suspend fun connectFolder(treeUri: Uri, selectedProvider: CloudStorageProvider) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(treeUri, flags)
        }.getOrElse { error("Não foi possível acessar a pasta selecionada") }
        require(hasPersistedPermission(treeUri)) { "Permissão da pasta não concedida" }
        val label = readTreeDisplayName(treeUri) ?: selectedProvider.displayName
        dataStore.edit { prefs ->
            prefs[Keys.TREE_URI] = treeUri.toString()
            prefs[Keys.PROVIDER] = selectedProvider.name
            prefs[Keys.FOLDER_LABEL] = label
        }
    }

    suspend fun isFolderAccessible(): Boolean {
        val treeUri = getTreeUri() ?: return false
        return hasPersistedPermission(treeUri) && readTreeDisplayName(treeUri) != null
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

    private fun readTreeDisplayName(treeUri: Uri): String? = runCatching {
        context.contentResolver.query(
            treeUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()

    private fun hasPersistedPermission(treeUri: Uri): Boolean =
        context.contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == treeUri &&
                permission.isReadPermission &&
                permission.isWritePermission
        }

    private fun findBackupUri(resolver: ContentResolver, treeUri: Uri, fileName: String): Uri? {
        require(fileName == BACKUP_FILE_NAME) { "Nome de arquivo inválido" }
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                if (cursor.getString(nameColumn) == fileName) {
                    val documentId = cursor.getString(idColumn)
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                }
            }
        }
        return null
    }

    private fun createBackupUri(resolver: ContentResolver, treeUri: Uri, fileName: String): Uri =
        DocumentsContract.createDocument(resolver, treeUri, MIME_JSON, fileName)
            ?: error("Não foi possível criar o arquivo de backup")

    private object Keys {
        val TREE_URI = stringPreferencesKey("cloud_sync_tree_uri")
        val PROVIDER = stringPreferencesKey("cloud_sync_provider")
        val FOLDER_LABEL = stringPreferencesKey("cloud_sync_folder_label")
    }

    companion object {
        private const val MIME_JSON = "application/json"
        const val BACKUP_FILE_NAME = "presencial_backup.json"
    }
}
