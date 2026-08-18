package com.pushtofinance.infinapp.notification

import com.pushtofinance.infinapp.currency.Currency

data class ParsedPush(
    val amount: Double? = null,
    val currency: String = Currency.PLN,
    val cardName: String? = null,
    val storeName: String? = null,
    val isIncome: Boolean = false
)

object PushParser {

    private val amountRe = Regex(
        """(?i)(?:wyd(?:atk|a|ałeś|ane|ano)|zapłac(?:ono|one)|płatnoś[ćc]|płatność|kupno|zakup|transakcj[ae]|obciążen[ieo]|charge|payment|paid|spent|kwota|amount)[^0-9]*([0-9]+(?:[.,][0-9]{1,2})?)\s*(zł|zl|pln|złotych|eur|euro|€|usd|us\$|\$)"""
    )
    private val amountBeforeRe = Regex(
        """(?i)(?:zł|zl|pln|eur|euro|€|usd|us\$|\$)\s*([0-9]+(?:[.,][0-9]{1,2})?)"""
    )
    private val amountPlainRe = Regex(
        """(?i)([0-9]+(?:[.,][0-9]{1,2})?)\s*(zł|zl|pln|złotych|eur|euro|€|usd|us\$|\$)(?![0-9])"""
    )
    private val incomeRe = Regex(
        """(?i)(wpłynęło|wpłynęła|wpłynął|uznanie|uznano|zasilenie|zasilono|wpłata|wpływ|przelew przychodzący|otrzymano|przychodzący|wynagrodzenie|received|credited|incoming|deposit|income|salary)"""
    )

    fun parse(title: String?, text: String?): ParsedPush {
        val full = listOfNotNull(title, text).joinToString(" | ")
        val (amt, cur) = extractAmount(full)
        val card = extractCard(full)
        val store = extractStore(full)
        return ParsedPush(amount = amt, currency = cur, cardName = card, storeName = store, isIncome = incomeRe.containsMatchIn(full))
    }

    private fun extractAmount(text: String): Pair<Double?, String> {
        val m = amountRe.find(text) ?: amountBeforeRe.find(text) ?: amountPlainRe.find(text) ?: return null to Currency.PLN
        val numStr = m.groupValues[1]
        val curStr = m.groupValues[2].ifEmpty {
            // when amountBeforeRe matches, the currency is in groupValues[1]
            m.groupValues[0].substringBefore(m.groupValues[1]).trim()
        }
        val amount = numStr.replace(',', '.').toDoubleOrNull() ?: return null to Currency.PLN
        val currency = when (curStr.lowercase()) {
            "zł", "zl", "pln", "złotych" -> Currency.PLN
            "eur", "euro", "€" -> Currency.EUR
            "usd", "us$", "$" -> Currency.USD
            else -> Currency.PLN
        }
        return amount to currency
    }

    private val cardRe = Regex(
        """(?i)(karta|card|kartą|kartę)[:\s]*(visa|mastercard|maestro|revolut|apple\s?pay|google\s?pay|blik|\*{2,4}\s?[0-9]{2,4}|[a-z]+(?: [a-z]+){0,2})"""
    )
    private val brandRe = Regex("""(?i)\b(visa|mastercard|maestro|revolut|apple\s?pay|google\s?pay)\b""")

    private fun extractCard(text: String): String? {
        val m = cardRe.find(text)
        if (m != null) return m.groupValues[2].trim().replace(Regex("""\s+"""), " ").uppercase()
        val b = brandRe.find(text)
        if (b != null) return b.groupValues[1].uppercase()
        val stars = Regex("""\*{3,4}\s?([0-9]{1,4})""").find(text)
        if (stars != null) return "Card ****${stars.groupValues[1]}"
        return null
    }

    private fun extractStore(text: String): String? {
        val segments = text.split("|")
        for (s in segments) {
            val t = s.trim()
            if (t.length in 2..28 && !t.any { it.isDigit() } && !amountRe.containsMatchIn(t) &&
                !t.contains("karta", ignoreCase = true) && !t.contains("zł", ignoreCase = true)
            ) return t.trim()
        }
        return null
    }
}