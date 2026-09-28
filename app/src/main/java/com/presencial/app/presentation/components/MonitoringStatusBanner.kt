package com.presencial.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.presencial.app.R

@Composable
fun MonitoringStatusBanner(
    activeAddressCount: Int,
    foregroundGranted: Boolean,
    backgroundGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeAddressCount == 0) return

    val isFullyConfigured = foregroundGranted && backgroundGranted
    val content = monitoringBannerContent(
        isFullyConfigured = isFullyConfigured,
        foregroundGranted = foregroundGranted,
        activeAddressCount = activeAddressCount
    )
    val containerColor = if (isFullyConfigured) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val iconTint = if (isFullyConfigured) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val icon = if (isFullyConfigured) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = content.description },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = content.title, style = MaterialTheme.typography.titleSmall)
                Text(text = content.body, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private data class MonitoringBannerContent(
    val description: String,
    val title: String,
    val body: String
)

@Composable
private fun monitoringBannerContent(
    isFullyConfigured: Boolean,
    foregroundGranted: Boolean,
    activeAddressCount: Int
): MonitoringBannerContent {
    val description = stringResource(
        if (isFullyConfigured) {
            R.string.monitoring_content_description_active
        } else {
            R.string.monitoring_content_description_incomplete
        }
    )
    val title = if (isFullyConfigured) {
        pluralStringResource(
            R.plurals.monitoring_active_title,
            activeAddressCount,
            activeAddressCount
        )
    } else {
        stringResource(R.string.monitoring_incomplete_title)
    }
    val body = monitoringDescription(isFullyConfigured, foregroundGranted)
    return MonitoringBannerContent(description, title, body)
}

@Composable
private fun monitoringDescription(
    isFullyConfigured: Boolean,
    foregroundGranted: Boolean
): String = when {
    isFullyConfigured -> stringResource(R.string.monitoring_active_description)
    !foregroundGranted -> stringResource(R.string.monitoring_need_foreground)
    else -> stringResource(R.string.monitoring_need_background)
}
