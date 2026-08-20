package com.presencial.app.data.backup

import java.io.File
import java.io.InputStream

/**
 * Copies a user-selected backup into app cache using a staging file so a failed
 * read cannot leave (or reuse) a previous import as the restore source.
 */
object BackupImportCache {
    const val FILE_NAME = "import_backup.json"
    const val STAGING_FILE_NAME = "import_backup.json.tmp"

    fun copyFromStream(cacheDir: File, inputStream: InputStream): File {
        val staging = File(cacheDir, STAGING_FILE_NAME)
        val target = File(cacheDir, FILE_NAME)
        try {
            inputStream.use { input ->
                staging.outputStream().use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            if (!staging.renameTo(target)) {
                staging.copyTo(target, overwrite = true)
            }
            return target
        } finally {
            if (staging.exists()) {
                staging.delete()
            }
        }
    }
}
