package com.pushtofinance.infinapp.util

import java.util.Calendar

fun monthStart(ts: Long): Long {
    val c = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return c.timeInMillis
}

fun addMonths(ts: Long, months: Int): Long {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    c.add(Calendar.MONTH, months)
    return c.timeInMillis
}

fun dayStart(ts: Long): Long {
    val c = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return c.timeInMillis
}

fun parseDate(text: String): Long? {
    val parts = text.trim().split(".", "-", "/").map { it.trim() }
    if (parts.size != 3) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return try {
        val c = Calendar.getInstance()
        c.clear()
        c.set(year, month - 1, day)
        c.timeInMillis
    } catch (e: Exception) {
        null
    }
}

fun formatDateInput(ts: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    val dd = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val mm = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val yyyy = c.get(Calendar.YEAR).toString()
    return "$dd.$mm.$yyyy"
}