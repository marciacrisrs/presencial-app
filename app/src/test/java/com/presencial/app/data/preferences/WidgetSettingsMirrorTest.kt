package com.presencial.app.data.preferences

import com.presencial.app.domain.model.PresencePolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.DayOfWeek

class WidgetSettingsMirrorTest {

    @Test
    fun `when DataStore already has settings, then do not overwrite from the widget mirror`() {
        val restored = WidgetSettingsMirror.restoreFromWidgetPrefs(
            dataStoreHasRequiredPercentage = true,
            dataStoreHasPolicy = false,
            widgetRequiredPercentage = 60,
            widgetCountSaturdays = true,
            widgetPolicyJson = PresencePolicyMapper.toJson(
                PresencePolicy(freePercentage = 60, freePercentageEnabled = true)
            )
        )

        assertNull(restored)
    }

    @Test
    fun `when widget mirror is empty, then leave DataStore defaults alone`() {
        val restored = WidgetSettingsMirror.restoreFromWidgetPrefs(
            dataStoreHasRequiredPercentage = false,
            dataStoreHasPolicy = false,
            widgetRequiredPercentage = null,
            widgetCountSaturdays = null,
            widgetPolicyJson = null
        )

        assertNull(restored)
    }

    @Test
    fun `when Auto Backup restored widget prefs but not DataStore, then recover the presence policy`() {
        val policy = PresencePolicy(
            companyName = "Acme",
            freePercentageEnabled = true,
            freePercentage = 60,
            fixedWeekdaysEnabled = true,
            mandatoryWeekdays = setOf(DayOfWeek.TUESDAY)
        )

        val restored = WidgetSettingsMirror.restoreFromWidgetPrefs(
            dataStoreHasRequiredPercentage = false,
            dataStoreHasPolicy = false,
            widgetRequiredPercentage = 60,
            widgetCountSaturdays = true,
            widgetPolicyJson = PresencePolicyMapper.toJson(policy)
        )

        assertEquals(60, restored?.requiredPercentage)
        assertEquals(true, restored?.countSaturdaysAsWorkdays)
        assertEquals("Acme", restored?.presencePolicy?.companyName)
        assertEquals(setOf(DayOfWeek.TUESDAY), restored?.presencePolicy?.mandatoryWeekdays)
    }
}
