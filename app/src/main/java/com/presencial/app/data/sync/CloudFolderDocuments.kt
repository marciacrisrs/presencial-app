package com.presencial.app.data.sync

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract

internal object CloudFolderDocuments {

    fun readTreeDisplayName(resolver: ContentResolver, treeUri: Uri): String? = runCatching {
        queryDisplayName(resolver, treeDocumentUri(treeUri))
    }.getOrNull() ?: runCatching {
        queryDisplayName(resolver, treeUri)
    }.getOrNull()

    fun hasPersistedPermission(resolver: ContentResolver, treeUri: Uri): Boolean =
        resolver.persistedUriPermissions.any { permission ->
            permission.isReadPermission &&
                permission.isWritePermission &&
                uriRefsSameTree(permission.uri, treeUri)
        }

    fun writeBackupContent(resolver: ContentResolver, backupUri: Uri, content: ByteArray) {
        val stream = resolver.openOutputStream(backupUri)
            ?: resolver.openOutputStream(backupUri, "wt")
        stream?.use { it.write(content) } ?: error("Não foi possível gravar o backup na pasta")
    }

    fun findBackupUri(resolver: ContentResolver, treeUri: Uri, fileName: String): Uri? {
        require(fileName == CloudFolderSyncProvider.BACKUP_FILE_NAME) { "Nome de arquivo inválido" }
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

    fun createBackupUri(resolver: ContentResolver, treeUri: Uri, fileName: String): Uri =
        DocumentsContract.createDocument(resolver, treeDocumentUri(treeUri), MIME_JSON, fileName)
            ?: error("Não foi possível criar o arquivo de backup")

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? =
        resolver.query(
            uri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    private fun uriRefsSameTree(stored: Uri, requested: Uri): Boolean {
        if (stored == requested) return true
        return runCatching {
            DocumentsContract.getTreeDocumentId(stored) ==
                DocumentsContract.getTreeDocumentId(requested)
        }.getOrDefault(false)
    }

    private fun treeDocumentUri(treeUri: Uri): Uri =
        DocumentsContract.buildDocumentUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )

    private const val MIME_JSON = "application/json"
}
