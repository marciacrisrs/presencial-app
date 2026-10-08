package com.presencial.app.ci

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class ReleaseVersionBumpTest {

    @Test
    fun `release abre pull request em vez de enviar o bump direto para main`() {
        val workflow = workflow("android-release.yml")

        assertFalse(workflow.contains("HEAD:main"))
        assertFalse(workflow.contains("HEAD:\${GITHUB_REF_NAME}"))
        assertFalse(workflow.contains("actions: write"))
        assertTrue(workflow.contains("gh pr create"))
        assertTrue(workflow.contains("pull-requests: write"))
    }

    @Test
    fun `dependency locks abre pull request em vez de enviar direto para main`() {
        val workflow = workflow("dependency-locks.yml")

        assertFalse(Regex("(?m)^\\s*git push\\s*$").containsMatchIn(workflow))
        assertFalse(workflow.contains("actions: write"))
        assertTrue(workflow.contains("gh pr create"))
        assertTrue(workflow.contains("pull-requests: write"))
    }

    private fun workflow(name: String): String {
        val cwd = File(checkNotNull(System.getProperty("user.dir")))
        val root = generateSequence(cwd) { it.parentFile }
            .first { candidate -> File(candidate, ".github").isDirectory }
        return File(root, ".github/workflows/$name").readText()
    }
}
