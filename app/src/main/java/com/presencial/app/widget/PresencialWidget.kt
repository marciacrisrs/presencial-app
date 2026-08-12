package com.presencial.app.widget

import android.content.Intent
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.presencial.app.MainActivity
import com.presencial.app.R

abstract class BasePresencialWidget(private val widgetSize: WidgetSize) : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val info = WidgetInfoLoader.load(context)

        provideContent {
            GlanceTheme {
                WidgetContent(context, widgetSize, info)
            }
        }
    }
}

class PresencialWidgetSmall : BasePresencialWidget(WidgetSize.SMALL)
class PresencialWidgetMedium : BasePresencialWidget(WidgetSize.MEDIUM)
class PresencialWidgetLarge : BasePresencialWidget(WidgetSize.LARGE)

@Composable
private fun WidgetContent(context: Context, widgetSize: WidgetSize, info: WidgetInfo) {
    val colors = WidgetColors.from(context)

    val modifier = GlanceModifier
        .fillMaxSize()
        .background(R.color.widget_background)
        .cornerRadius(WIDGET_CORNER_RADIUS.dp)
        .clickable(
            actionStartActivity(
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )
        )
        .padding(WIDGET_PADDING.dp)

    when (widgetSize) {
        WidgetSize.SMALL -> SmallLayout(info, colors, modifier)
        WidgetSize.MEDIUM -> MediumLayout(info, colors, modifier)
        WidgetSize.LARGE -> LargeLayout(info, colors, modifier)
    }
}

@Composable
private fun SmallLayout(
    info: WidgetInfo,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    val context = androidx.glance.LocalContext.current

    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = headlineFor(info, context),
            style = TextStyle(
                color = colors.headline(info.status),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        if (info.status != WidgetStatus.GOAL_MET && info.required > 0) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = context.getString(
                    R.string.widget_compact_progress,
                    info.completed,
                    info.required
                ),
                style = TextStyle(
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun MediumLayout(
    info: WidgetInfo,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    val context = androidx.glance.LocalContext.current

    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = context.getString(
                R.string.widget_progress_format,
                info.completed,
                info.required
            ),
            style = TextStyle(
                color = colors.success,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        if (info.required > 0) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            LinearProgressIndicator(
                progress = info.progressFraction,
                modifier = GlanceModifier.fillMaxWidth().height(5.dp),
                color = colors.accent(info.status),
                backgroundColor = colors.secondaryText
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = context.getString(R.string.widget_percentage_format, info.achievedPercentage),
                style = TextStyle(
                    color = colors.secondaryText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = headlineFor(info, context),
            style = TextStyle(
                color = colors.headline(info.status),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun LargeLayout(
    info: WidgetInfo,
    colors: WidgetColors,
    modifier: GlanceModifier
) {
    val context = androidx.glance.LocalContext.current

    Column(
        modifier = modifier,
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = context.getString(R.string.widget_title_month, info.monthName),
            style = TextStyle(
                color = colors.secondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = context.getString(
                R.string.widget_progress_format,
                info.completed,
                info.required
            ),
            style = TextStyle(
                color = colors.success,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )

        if (info.required > 0) {
            Spacer(modifier = GlanceModifier.height(10.dp))
            LinearProgressIndicator(
                progress = info.progressFraction,
                modifier = GlanceModifier.fillMaxWidth().height(6.dp),
                color = colors.accent(info.status),
                backgroundColor = colors.secondaryText
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = context.getString(R.string.widget_percentage_format, info.achievedPercentage),
                style = TextStyle(
                    color = colors.secondaryText,
                    fontSize = 13.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = headlineFor(info, context),
            style = TextStyle(
                color = colors.headline(info.status),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        if (info.todayIsWorkday) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(
                text = if (info.todayIsPresencial) {
                    context.getString(R.string.widget_today_presencial)
                } else {
                    context.getString(R.string.widget_today_pending)
                },
                style = TextStyle(
                    color = if (info.todayIsPresencial) colors.success else colors.secondaryText,
                    fontSize = 12.sp
                )
            )
        }
    }
}

private fun headlineFor(info: WidgetInfo, context: Context): String = when (info.status) {
    WidgetStatus.GOAL_MET -> context.getString(R.string.widget_goal_met)
    WidgetStatus.NO_GOAL -> context.getString(R.string.widget_configure_goal)
    else -> context.resources.getQuantityString(
        R.plurals.widget_remaining_days,
        info.remaining,
        info.remaining
    )
}

private data class WidgetColors(
    val success: ColorProvider,
    val warning: ColorProvider,
    val primaryText: ColorProvider,
    val secondaryText: ColorProvider
) {
    fun headline(status: WidgetStatus): ColorProvider = when (status) {
        WidgetStatus.BEHIND -> warning
        WidgetStatus.GOAL_MET -> success
        else -> primaryText
    }

    fun accent(status: WidgetStatus): ColorProvider = when (status) {
        WidgetStatus.BEHIND -> warning
        WidgetStatus.GOAL_MET -> success
        else -> success
    }

    companion object {
        fun from(context: Context): WidgetColors {
            fun provider(colorRes: Int): ColorProvider {
                val color = androidx.compose.ui.graphics.Color(context.getColor(colorRes))
                return androidx.glance.color.ColorProvider(day = color, night = color)
            }

            return WidgetColors(
                success = provider(R.color.widget_success),
                warning = provider(R.color.widget_warning),
                primaryText = provider(R.color.widget_primary_text),
                secondaryText = provider(R.color.widget_text_secondary)
            )
        }
    }
}

private const val WIDGET_CORNER_RADIUS = 24
private const val WIDGET_PADDING = 12

class PresencialWidgetReceiverSmall : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidgetSmall()
}

class PresencialWidgetReceiverMedium : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidgetMedium()
}

class PresencialWidgetReceiverLarge : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PresencialWidgetLarge()
}
