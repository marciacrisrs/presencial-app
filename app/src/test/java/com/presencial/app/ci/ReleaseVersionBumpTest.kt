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
    fun `falha ao abrir o pull request de versao nao cancela o envio para a Play`() {
        val workflow = workflow("android-release.yml")
        val create = workflow.indexOf("gh pr create")
        val play = workflow.indexOf("Publish to Play internal testing")

        assertTrue(create >= 0)
        assertTrue(play > create, "O envio para a Play continua depois da tentativa de abrir o PR")
        assertTrue(
            workflow.substring(create, play).contains("|| echo \"::warning::"),
            "Se o token do Actions não puder abrir o PR, o job segue para a Play"
        )
    }

    @Test
    fun `envio para a Play nao manda a edicao para revisao automaticamente`() {
        val workflow = workflow("android-release.yml")
        val play = workflow.indexOf("Publish to Play internal testing")
        assertTrue(play >= 0)
        assertTrue(
            workflow.substring(play).contains("changesNotSentForReview: true"),
            "A API da Play recusa o commit quando a edição iria para revisão sozinha"
        )
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
