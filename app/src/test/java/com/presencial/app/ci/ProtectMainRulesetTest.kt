package com.presencial.app.ci

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class ProtectMainRulesetTest {

    @Test
    fun `protect main exige pull request checks atualizados e sem bypass`() {
        val ruleset = JSONObject(rulesetFile().readText())

        assertEquals("Protect main", ruleset.getString("name"))
        assertEquals("branch", ruleset.getString("target"))
        assertEquals("active", ruleset.getString("enforcement"))
        assertEquals(0, ruleset.getJSONArray("bypass_actors").length())
        assertEquals(
            "~DEFAULT_BRANCH",
            ruleset.getJSONObject("conditions")
                .getJSONObject("ref_name")
                .getJSONArray("include")
                .getString(0),
        )

        val rules = ruleset.getJSONArray("rules")
        val byType = (0 until rules.length()).associate { index ->
            val rule = rules.getJSONObject(index)
            rule.getString("type") to rule
        }

        assertTrue(byType.containsKey("deletion"))
        assertTrue(byType.containsKey("non_fast_forward"))

        val pullRequest = byType.getValue("pull_request").getJSONObject("parameters")
        assertEquals(0, pullRequest.getInt("required_approving_review_count"))
        assertEquals(
            setOf("merge", "squash", "rebase"),
            (0 until pullRequest.getJSONArray("allowed_merge_methods").length())
                .map { pullRequest.getJSONArray("allowed_merge_methods").getString(it) }
                .toSet(),
        )

        val statusChecks = byType.getValue("required_status_checks").getJSONObject("parameters")
        assertTrue(statusChecks.getBoolean("strict_required_status_checks_policy"))
        assertTrue(statusChecks.getBoolean("do_not_enforce_on_create"))

        val contexts = statusChecks.getJSONArray("required_status_checks")
        val checks = (0 until contexts.length()).associate { index ->
            val check = contexts.getJSONObject(index)
            check.getString("context") to check.getInt("integration_id")
        }

        assertEquals(
            mapOf(
                "quality" to GITHUB_ACTIONS_APP_ID,
                "instrumented" to GITHUB_ACTIONS_APP_ID,
                "CodeQL (java-kotlin)" to GITHUB_ACTIONS_APP_ID,
                "Dependency Review" to GITHUB_ACTIONS_APP_ID,
                "Scorecard" to GITHUB_ACTIONS_APP_ID,
                "SonarCloud Code Analysis" to SONARCLOUD_APP_ID,
            ),
            checks,
        )
    }

    @Test
    fun `scorecard roda em pull request sem publicar fora da branch padrao`() {
        val workflow = scorecardWorkflow().readText()

        assertTrue(workflow.contains("pull_request:"))
        assertFalse(Regex("publish_results:\\s*true").containsMatchIn(workflow))
        assertTrue(workflow.contains("github.event_name != 'pull_request'"))
    }

    private fun rulesetFile(): File = repoFile(".github/rulesets/protect-main.json")

    private fun scorecardWorkflow(): File = repoFile(".github/workflows/scorecard.yml")

    private fun repoFile(relativePath: String): File {
        val cwd = File(checkNotNull(System.getProperty("user.dir")))
        val root = generateSequence(cwd) { it.parentFile }
            .first { candidate -> File(candidate, ".github").isDirectory }
        return File(root, relativePath)
    }

    private companion object {
        const val GITHUB_ACTIONS_APP_ID = 15368
        const val SONARCLOUD_APP_ID = 12526
    }
}
