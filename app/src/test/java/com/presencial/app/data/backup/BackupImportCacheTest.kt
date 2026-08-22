package com.presencial.app.data.backup

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException
import java.io.InputStream

class BackupImportCacheTest {

    private lateinit var cacheDir: File

    @BeforeEach
    fun setUp() {
        cacheDir = File.createTempFile("backup_import_cache", "").apply {
            delete()
            mkdir()
        }
    }

    @AfterEach
    fun tearDown() {
        cacheDir.deleteRecursively()
    }

    @Test
    fun `copyFromStream writes selected backup into cache`() {
        val staged = BackupImportCache.copyFromStream(
            cacheDir,
            "backup-b".byteInputStream()
        )

        assertEquals(BackupImportCache.FILE_NAME, staged.name)
        assertEquals("backup-b", staged.readText())
    }

    @Test
    fun `copyFromStream replaces previous cache only after a successful copy`() {
        File(cacheDir, BackupImportCache.FILE_NAME).writeText("backup-a")

        val staged = BackupImportCache.copyFromStream(
            cacheDir,
            "backup-b".byteInputStream()
        )

        assertEquals("backup-b", staged.readText())
        assertFalse(File(cacheDir, BackupImportCache.STAGING_FILE_NAME).exists())
    }

    @Test
    fun `failed copy keeps previous cache file intact`() {
        val previous = File(cacheDir, BackupImportCache.FILE_NAME)
        previous.writeText("backup-a")
        val failingStream = object : InputStream() {
            override fun read(): Int = throw IOException("read failed")
        }

        val thrown = assertThrows(IOException::class.java) {
            BackupImportCache.copyFromStream(cacheDir, failingStream)
        }

        assertEquals("read failed", thrown.message)
        assertEquals("backup-a", previous.readText())
        assertFalse(File(cacheDir, BackupImportCache.STAGING_FILE_NAME).exists())
    }
}
