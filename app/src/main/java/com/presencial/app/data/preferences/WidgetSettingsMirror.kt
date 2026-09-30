package com.presencial.app.data.preferences

import com.presencial.app.domain.model.PresencePolicy

internal object WidgetSettingsMirror {

    fun restoreFromWidgetPrefs(
        dataStoreHasRequiredPercentage: Boolean,
        dataStoreHasPolicy: Boolean,
        widgetRequiredPercentage: Int?,
        widgetCountSaturdays: Boolean?,
        widgetPolicyJson: String?
    ): RestoreSnapshot? {
        if (dataStoreHasRequiredPercentage || dataStoreHasPolicy) return null
        if (
            widgetRequiredPercentage == null &&
            widgetCountSaturdays == null &&
            widgetPolicyJson.isNullOrBlank()
        ) {
            return null
        }

        val percentage = widgetRequiredPercentage ?: DEFAULT_PERCENTAGE
        val policy = PresencePolicyMapper.fromJson(widgetPolicyJson, percentage)
        return RestoreSnapshot(
            requiredPercentage = policy.freePercentage,
            countSaturdaysAsWorkdays = widgetCountSaturdays ?: false,
            presencePolicy = policy
        )
    }

    data class RestoreSnapshot(
        val requiredPercentage: Int,
        val countSaturdaysAsWorkdays: Boolean,
        val presencePolicy: PresencePolicy
    )

    private const val DEFAULT_PERCENTAGE = 40
}
