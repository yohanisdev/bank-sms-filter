package com.example.hermodit.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Common date and time formatting utilities.
 */
object DateTimeUtils {
    fun formatDate(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)
        return formatter.format(instant.atZone(zoneId))
    }

    fun formatTime(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", locale)
        return formatter.format(instant.atZone(zoneId))
    }

    fun formatDateTime(
        instant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a", locale)
        return formatter.format(instant.atZone(zoneId))
    }
}
