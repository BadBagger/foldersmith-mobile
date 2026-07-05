package com.foldersmith.mobile.ui

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun Long.formatBytes(): String {
    if (this < 1024L) return "$this B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = this / 1024.0
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    return String.format(Locale.US, "%.1f %s", value, units[unitIndex])
}

fun Long.formatDateTime(): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(this))

fun Long?.formatDateOrNever(): String = this?.formatDateTime() ?: "No scan yet"
