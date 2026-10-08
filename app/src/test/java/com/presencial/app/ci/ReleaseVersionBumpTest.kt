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
        assertTrue(workflow.contains("gh pr create"))
        assertTrue(workflow.contains("gh workflow run"))
        assertTrue(workflow.contains("pull-requests: write"))
        assertTrue(workflow.contains("actions: write"))
    }

    @Test
    fun `dependency locks abre pull request em vez de enviar direto para main`() {
        val workflow = workflow("dependency-locks.yml")

        assertFalse(Regex("(?m)^\\s*git push\\s*$").containsMatchIn(workflow))
        assertTrue(workflow.contains("gh pr create"))
        assertTrue(workflow.contains("gh workflow run"))
    }

    @Test
    fun `actions write fica no job e nao no token do workflow inteiro`() {
        listOf("android-release.yml", "dependency-locks.yml").forEach { name ->
            val workflow = workflow(name)
            val header = workflow.substringBefore("jobs:")
            assertFalse(header.contains("actions: write"), name)
            assertTrue(workflow.substringAfter("jobs:").contains("actions: write"), name)
        }
    }

    @Test
    fun `checks obrigatorios aceitam disparo para o commit do bump`() {
        listOf(
            "android.yml",
            "codeql.yml",
            "dependency-review.yml",
            "scorecard.yml",
        ).forEach { name ->
            assertTrue(workflow(name).contains("workflow_dispatch:"), name)
        }
    }

    private fun workflow(name: String): String {
        val cwd = File(checkNotNull(System.getProperty("user.dir")))
        val root = generateSequence(cwd) { it.parentFile }
            .first { candidate -> File(candidate, ".github").isDirectory }
        return File(root, ".github/workflows/$name").readText()
    }
}
