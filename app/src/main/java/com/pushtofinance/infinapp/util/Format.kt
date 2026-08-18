package com.pushtofinance.infinapp.util

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Format {
    private fun nf(currency: String): NumberFormat =
        NumberFormat.getNumberInstance(Locale.GERMANY).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

    fun money(amount: Double, currency: String = "PLN"): String {
        val symbol = when (currency.uppercase()) {
            "PLN" -> "zł"
            "EUR" -> "€"
            "USD" -> "$"
            else -> currency.uppercase()
        }
        return "${nf(currency).format(amount)} $symbol"
    }

    fun moneyPlain(amount: Double, currency: String = "PLN"): String {
        val symbol = when (currency.uppercase()) {
            "PLN" -> "zł"
            "EUR" -> "€"
            "USD" -> "$"
            else -> currency.uppercase()
        }
        return "${nf(currency).format(amount)} $symbol"
    }

    fun parseAmount(raw: String): Double? {
        val clean = raw.replace(' ', '.').replace(',', '.')
        return clean.toDoubleOrNull()
    }

    fun round2(v: Double): Double = (v * 100).toLong() / 100.0

    fun date(ts: Long): String {
        val df = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return df.format(java.util.Date(ts))
    }

    fun dateTime(ts: Long): String {
        val df = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return df.format(java.util.Date(ts))
    }

    fun colorHex(argb: Long): String {
        val c = argb.toInt()
        return "#%06X".format(Locale.US, c and 0xFFFFFF)
    }
}