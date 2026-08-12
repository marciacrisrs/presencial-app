package com.presencial.app.data.preferences

import com.presencial.app.domain.model.PolicyConflictPriority
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.model.WeekParity
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate

object PresencePolicyMapper {

    fun toJson(policy: PresencePolicy): String {
        val normalized = policy.normalized()
        return JSONObject().apply {
            put("companyName", normalized.companyName)
            put("freePercentageEnabled", normalized.freePercentageEnabled)
            put("freePercentage", normalized.freePercentage)
            put("fixedWeekdaysEnabled", normalized.fixedWeekdaysEnabled)
            put(
                "mandatoryWeekdays",
                normalized.mandatoryWeekdays.joinToString(",") { it.name }
            )
            put("alternatingWeeksEnabled", normalized.alternatingWeeksEnabled)
            put("alternatingAnchorDate", normalized.alternatingAnchorDate.toEpochDay())
            put("onSiteWeekParity", normalized.onSiteWeekParity.name)
            put("conflictPriority", normalized.conflictPriority.name)
        }.toString()
    }

    fun fromJson(raw: String?, fallbackPercentage: Int): PresencePolicy {
        if (raw.isNullOrBlank()) {
            return PresencePolicy.fromLegacyPercentage(fallbackPercentage)
        }
        return runCatching {
            val json = JSONObject(raw)
            PresencePolicy(
                companyName = json.optString("companyName", ""),
                freePercentageEnabled = json.optBoolean("freePercentageEnabled", true),
                freePercentage = json.optInt("freePercentage", fallbackPercentage),
                fixedWeekdaysEnabled = json.optBoolean("fixedWeekdaysEnabled", false),
                mandatoryWeekdays = parseWeekdays(json.optString("mandatoryWeekdays", "")),
                alternatingWeeksEnabled = json.optBoolean("alternatingWeeksEnabled", false),
                alternatingAnchorDate = LocalDate.ofEpochDay(
                    json.optLong(
                        "alternatingAnchorDate",
                        LocalDate.now().with(DayOfWeek.MONDAY).toEpochDay()
                    )
                ),
                onSiteWeekParity = json.optString("onSiteWeekParity", WeekParity.EVEN.name)
                    .let { runCatching { WeekParity.valueOf(it) }.getOrDefault(WeekParity.EVEN) },
                conflictPriority = json.optString("conflictPriority", PolicyConflictPriority.UNION_MAX.name)
                    .let {
                        runCatching { PolicyConflictPriority.valueOf(it) }
                            .getOrDefault(PolicyConflictPriority.UNION_MAX)
                    }
            ).normalized()
        }.getOrDefault(PresencePolicy.fromLegacyPercentage(fallbackPercentage))
    }

    private fun parseWeekdays(raw: String): Set<DayOfWeek> =
        raw.split(",")
            .mapNotNull { token ->
                token.trim().takeIf { it.isNotEmpty() }
                    ?.let { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }
            }
            .toSet()
}
