package com.pushtofinance.infinapp.currency

import com.pushtofinance.infinapp.data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object Currency {
    const val PLN = "PLN"
    const val EUR = "EUR"
    const val USD = "USD"
    val supported = listOf(PLN, EUR, USD)

    // Fallback approximate rates when offline (how many EUR/USD per 1 PLN)
    val fallbackPlnTo: Map<String, Double> = mapOf(
        PLN to 1.0,
        EUR to 0.232,
        USD to 0.252
    )
}

class ExchangeRateClient(private val settings: SettingsManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun getPlnToMap(): Map<String, Double> {
        val (cache, ts) = settings.currentRateCache()
        val fresh = System.currentTimeMillis() - ts < 12 * 60 * 60 * 1000L
        if (cache.isNotBlank() && fresh) {
            return parseCache(cache) ?: Currency.fallbackPlnTo
        }
        return try {
            withContext(Dispatchers.IO) {
                val req = Request.Builder().url("https://open.er-api.com/v6/latest/PLN").build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext Currency.fallbackPlnTo
                    val body = resp.body?.string() ?: return@withContext Currency.fallbackPlnTo
                    val rates = json.parseToJsonElement(body).jsonObject["rates"]?.jsonObject ?: return@withContext Currency.fallbackPlnTo
                    val map = Currency.supported.associateWith { c ->
                        rates[c]?.jsonPrimitive?.content?.toDoubleOrNull() ?: Currency.fallbackPlnTo[c] ?: 1.0
                    }
                    settings.setRateCache(rates.toString(), System.currentTimeMillis())
                    map
                }
            }
        } catch (e: Exception) {
            Currency.fallbackPlnTo
        }
    }

    private fun parseCache(cache: String): Map<String, Double>? = try {
        val rates = json.parseToJsonElement(cache).jsonObject
        Currency.supported.associateWith { c ->
            rates[c]?.jsonPrimitive?.content?.toDoubleOrNull() ?: Currency.fallbackPlnTo[c] ?: 1.0
        }
    } catch (e: Exception) {
        null
    }

    suspend fun toPln(amount: Double, currency: String): Double {
        if (currency == Currency.PLN) return amount
        val plnToX = getPlnToMap()[currency] ?: return amount
        return amount / plnToX
    }

    suspend fun fromPln(amountPln: Double, currency: String): Double {
        if (currency == Currency.PLN) return amountPln
        val plnToX = getPlnToMap()[currency] ?: return amountPln
        return amountPln * plnToX
    }
}