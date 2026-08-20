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
        """(?i)(?:wyd(?:atk|a|ałeś|ane|ano)|zapłac(?:ono|one)|płatnoś[ćc]|kupno|zakup|transakcj[ae]|obciążen[ieo]|charge|payment|paid|spent|kwota|amount)[^0-9!?.\n]{0,40}([0-9]+(?:[.,][0-9]{1,2})?)\s*(zł|zl|pln|złotych|eur|euro|€|usd|us\$|\$)"""
    )
    private val amountBeforeRe = Regex(
        """(?i)(?:zł|zl|pln|złotych|eur|euro|€|usd|us\$|\$)\s*([0-9]+(?:[.,][0-9]{1,2})?)"""
    )
    private val amountPlainRe = Regex(
        """(?i)([0-9]+(?:[.,][0-9]{1,2})?)\s*(zł|zl|pln|złotych|eur|euro|€|usd|us\$|\$)(?![0-9])"""
    )
    private val incomeRe = Regex(
        """(?i)(przesyła|otrzymałeś|otrzymałaś|otrzymano|wpłynęło|wpłynęła|wpłynął|uznanie|uznano|zasilenie|zasilono|wpłata|wpływ|przelew przychodzący|przychodzący|wynagrodzenie|received|credited|incoming|deposit|income|salary)"""
    )

    private val storeFromTextRe = Regex(
        """(?i)\bw\s+sklepie\s+([^.!?]{2,40})|\bzapłac(?:ono|one|ałeś|ano)\s+[^!.\n]{0,40}?\bw\s+([^.!?]{2,40})"""
    )

    fun parse(title: String?, text: String?): ParsedPush {
        val full = listOfNotNull(title, text).joinToString(" | ")
        val (amt, cur) = extractAmount(full)
        val card = extractCard(full)
        val income = incomeRe.containsMatchIn(full)
        val store = if (income) null else extractStore(full)
        return ParsedPush(amount = amt, currency = cur, cardName = card, storeName = store, isIncome = income)
    }

    private fun extractAmount(text: String): Pair<Double?, String> {
        val m = amountRe.find(text)
        if (m != null) return parseAmount(m.groupValues[1], m.groupValues[2])
        val mb = amountBeforeRe.find(text)
        if (mb != null) return parseAmount(mb.groupValues[2], mb.groupValues[1])
        val mp = amountPlainRe.find(text)
        if (mp != null) return parseAmount(mp.groupValues[1], mp.groupValues[2])
        return null to Currency.PLN
    }

    private fun parseAmount(numStr: String, curStr: String): Pair<Double?, String> {
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
        """(?i)(karta|card|kartą|kartę)[:\s]*(visa|mastercard|maestro|revolut|apple\s?pay|google\s?pay|blik|\*{2,4}\s?[0-9]{2,4}|[a-ząćęłńóśźż]+(?: [a-ząćęłńóśźż]+){0,2})"""
    )
    private val brandRe = Regex("""(?i)\b(visa|mastercard|maestro|revolut|apple\s?pay|google\s?pay)\b""")

    private fun extractCard(text: String): String? {
        val m = cardRe.find(text)
        if (m != null) return cleanName(m.groupValues[2]).uppercase().ifBlank { null }
        val b = brandRe.find(text)
        if (b != null) return cleanName(b.groupValues[1]).uppercase().ifBlank { null }
        val stars = Regex("""\*{3,4}\s?([0-9]{1,4})""").find(text)
        if (stars != null) return "Card ****${stars.groupValues[1]}"
        return null
    }

    private fun extractStore(text: String): String? {
        val fromText = storeFromTextRe.find(text)
            ?.groupValues
            ?.drop(1)
            ?.firstOrNull { !it.isNullOrBlank() }
            ?.let { cleanName(it) }
        if (fromText != null && isValidStore(fromText)) return fromText

        val titleSeg = text.split("|").firstOrNull()?.let { cleanName(it) }
        if (titleSeg != null && isValidStore(titleSeg)) return titleSeg
        return null
    }

    private fun isValidStore(name: String): Boolean {
        if (name.length !in 2..28) return false
        if (name.any { it.isDigit() }) return false
        if (amountRe.containsMatchIn(name)) return false
        if (name.contains("karta", ignoreCase = true) || name.contains("saldo", ignoreCase = true)) return false
        if (name.contains("zł", ignoreCase = true) || name.contains("pln", ignoreCase = true)) return false
        val filler = listOf("kwocie", "wysokości", "wysokosci", "dniu", "miesiącu", "miesiacu", "sumie", "ramach", "czasie", "sklepie", "konto", "transakcji", "płatności", "platnosci")
        if (name.split(Regex("""\s+""")).firstOrNull()?.lowercase() in filler) return false
        return true
    }

    private val nonNameChars = Regex("""[^\p{L}\p{N}\s.,'’/&*\-+]+""")

    private fun cleanName(s: String): String =
        s.replace(nonNameChars, " ").replace(Regex("""\s+"""), " ").trim()
}