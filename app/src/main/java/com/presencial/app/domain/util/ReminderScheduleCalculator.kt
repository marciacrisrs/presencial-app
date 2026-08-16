package com.presencial.app.domain.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Calculates the delay until the next configured reminder time.
 *
 * The reminder time is a target, not a guarantee: WorkManager may execute later
 * because Android controls background scheduling and power constraints.
 */
object ReminderScheduleCalculator {

    fun initialDelayMillis(
        now: LocalDateTime,
        reminderTime: LocalTime
    ): Long {
        val todayTarget = now.toLocalDate().atTime(reminderTime)
        val nextTarget = if (todayTarget.isAfter(now)) {
            todayTarget
        } else {
            todayTarget.plusDays(1)
        }
        return Duration.between(now, nextTarget).toMillis().coerceAtLeast(1L)
    }
}
