package com.presencial.app.data.backup

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class AutoBackupRulesTest {

    @Test
    fun `legacy full backup includes DataStore settings used by the dashboard`() {
        val includes = includePaths(repoFile("app/src/main/res/xml/backup_rules.xml"), "include")

        assertTrue(includes.contains(DATABASE_NAME), "Room database must remain in Auto Backup")
        assertTrue(
            includes.contains(WIDGET_SHARED_PREFS),
            "Widget SharedPreferences mirror must remain in Auto Backup"
        )
        assertTrue(
            includes.contains(DATASTORE_SETTINGS),
            "Dashboard settings live in DataStore, not SharedPreferences"
        )
    }

    @Test
    fun `cloud and device transfer backup includes DataStore settings used by the dashboard`() {
        val extraction = repoFile("app/src/main/res/xml/data_extraction_rules.xml")
        val cloudIncludes = includePaths(extraction, "include", parentTag = "cloud-backup")
        val transferIncludes = includePaths(extraction, "include", parentTag = "device-transfer")

        listOf(cloudIncludes, transferIncludes).forEach { includes ->
            assertTrue(includes.contains(DATABASE_NAME), "Room database must remain in Auto Backup")
            assertTrue(
                includes.contains(WIDGET_SHARED_PREFS),
                "Widget SharedPreferences mirror must remain in Auto Backup"
            )
            assertTrue(
                includes.contains(DATASTORE_SETTINGS),
                "Dashboard settings live in DataStore, not SharedPreferences"
            )
        }
    }

    private fun includePaths(
        xmlFile: File,
        tagName: String,
        parentTag: String? = null
    ): Set<Pair<String, String>> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile)
        val nodes = document.getElementsByTagName(tagName)
        return (0 until nodes.length).mapNotNull { index ->
            val element = nodes.item(index) as Element
            val parent = element.parentNode as? Element
            if (parentTag != null && parent?.tagName != parentTag) {
                null
            } else {
                element.getAttribute("domain") to element.getAttribute("path")
            }
        }.toSet()
    }

    private fun repoFile(relativePath: String): File {
        val cwd = File(checkNotNull(System.getProperty("user.dir")))
        val root = generateSequence(cwd) { it.parentFile }
            .first { candidate -> File(candidate, ".github").isDirectory }
        return File(root, relativePath)
    }

    private companion object {
        val DATABASE_NAME = "database" to "presencial.db"
        val WIDGET_SHARED_PREFS = "sharedpref" to "presencial_settings.xml"
        val DATASTORE_SETTINGS = "file" to "datastore/presencial_settings.preferences_pb"
    }
}
