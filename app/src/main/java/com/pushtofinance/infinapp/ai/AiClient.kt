package com.pushtofinance.infinapp.ai

import com.pushtofinance.infinapp.data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class AiGuess(val category: String?, val store: String?)

enum class AiProvider(
    val id: String,
    val label: String,
    val defaultModel: String,
    val defaultBaseUrl: String? = null
) {
    GEMINI("gemini", "Google Gemini", "gemini-2.0-flash"),
    OPENAI("openai", "OpenAI (GPT)", "gpt-4o-mini", "https://api.openai.com/v1"),
    DEEPSEEK("deepseek", "DeepSeek", "deepseek-chat", "https://api.deepseek.com/v1"),
    KIMI("kimi", "Kimi (Moonshot AI)", "moonshot-v1-8k", "https://api.moonshot.cn/v1"),
    CLAUDE("claude", "Anthropic Claude", "claude-3-5-haiku-latest"),
    CUSTOM("custom", "Custom (OpenAI-compatible)", "");

    companion object {
        fun fromId(id: String?): AiProvider = entries.firstOrNull { it.id == id } ?: GEMINI
    }
}

class AiClient(private val settings: SettingsManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private val media = "application/json; charset=utf-8".toMediaType()

    suspend fun detectCategoryAndStore(
        pushText: String,
        appName: String,
        availableCategories: List<String>
    ): AiGuess = withContext(Dispatchers.IO) {
        val key = settings.currentAiKey()
        if (key.isBlank()) return@withContext AiGuess(null, null)
        val provider = AiProvider.fromId(settings.currentAiProvider())
        val model = settings.currentAiModel()
        val baseUrl = settings.currentAiBaseUrl()
        val prompt = buildPrompt(pushText, appName, availableCategories)
        try {
            when (provider) {
                AiProvider.GEMINI -> gemini(provider, model, prompt, key)
                AiProvider.CLAUDE -> claude(provider, model, prompt, key)
                AiProvider.CUSTOM -> openAiCompatible(provider, model, baseUrl, prompt, key)
                else -> openAiCompatible(provider, model, null, prompt, key)
            }
        } catch (e: Exception) {
            AiGuess(null, null)
        }
    }

    private fun buildPrompt(pushText: String, appName: String, availableCategories: List<String>): String = buildString {
        appendLine("You are a personal finance assistant. From a banking notification, detect the expense CATEGORY and the STORE/merchant name.")
        appendLine("Reply in the same language as the notification.")
        appendLine("Available categories: ${availableCategories.joinToString(", ")}")
        appendLine("Banking app: $appName")
        appendLine("Notification: \"$pushText\"")
        appendLine("Reply with ONLY valid JSON: {\"category\":\"category_name_from_the_list_or_empty\",\"store\":\"store_name_or_empty\"}.")
    }

    private fun gemini(provider: AiProvider, modelSetting: String, prompt: String, key: String): AiGuess {
        val model = modelSetting.takeIf { it.isNotBlank() } ?: provider.defaultModel
        val payload = JsonObject(
            mapOf(
                "contents" to buildJsonArray {
                    add(
                        JsonObject(
                            mapOf(
                                "parts" to buildJsonArray {
                                    add(JsonObject(mapOf("text" to JsonPrimitive(prompt))))
                                }
                            )
                        )
                    )
                },
                "generationConfig" to JsonObject(
                    mapOf(
                        "responseMimeType" to JsonPrimitive("application/json"),
                        "temperature" to JsonPrimitive(0.2)
                    )
                )
            )
        )
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key")
            .post(payload.toString().toRequestBody(media))
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return AiGuess(null, null)
            val text = resp.body?.string() ?: return AiGuess(null, null)
            val root = json.parseToJsonElement(text).jsonObject
            val parts = root["candidates"]?.jsonArray?.getOrNull(0)
                ?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray
                ?: return AiGuess(null, null)
            val answer = parts.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.content }
                .joinToString("\n")
            return parseGuess(answer)
        }
    }

    private fun claude(provider: AiProvider, modelSetting: String, prompt: String, key: String): AiGuess {
        val model = modelSetting.takeIf { it.isNotBlank() } ?: provider.defaultModel
        val payload = JsonObject(
            mapOf(
                "model" to JsonPrimitive(model),
                "max_tokens" to JsonPrimitive(1024),
                "messages" to buildJsonArray {
                    add(JsonObject(mapOf("role" to JsonPrimitive("user"), "content" to JsonPrimitive(prompt))))
                }
            )
        )
        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", key)
            .header("anthropic-version", "2023-06-01")
            .post(payload.toString().toRequestBody(media))
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return AiGuess(null, null)
            val text = resp.body?.string() ?: return AiGuess(null, null)
            val root = json.parseToJsonElement(text).jsonObject
            val answer = root["content"]?.jsonArray?.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.content }
                ?.joinToString("\n") ?: return AiGuess(null, null)
            return parseGuess(answer)
        }
    }

    private fun openAiCompatible(provider: AiProvider, modelSetting: String, baseUrlSetting: String?, prompt: String, key: String): AiGuess {
        val model = modelSetting.takeIf { it.isNotBlank() } ?: provider.defaultModel
        val baseUrl = (baseUrlSetting?.takeIf { it.isNotBlank() } ?: provider.defaultBaseUrl)
            ?.trimEnd('/') ?: return AiGuess(null, null)
        if (model.isBlank()) return AiGuess(null, null)
        val payload = buildJsonObject {
            put("model", JsonPrimitive(model))
            put("temperature", JsonPrimitive(0.2))
            put("response_format", buildJsonObject { put("type", JsonPrimitive("json_object")) })
            put(
                "messages", buildJsonArray {
                    add(JsonObject(mapOf("role" to JsonPrimitive("system"), "content" to JsonPrimitive("You always reply with valid JSON."))))
                    add(JsonObject(mapOf("role" to JsonPrimitive("user"), "content" to JsonPrimitive(prompt))))
                }
            )
        }
        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(payload.toString().toRequestBody(media))
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return AiGuess(null, null)
            val text = resp.body?.string() ?: return AiGuess(null, null)
            val root = json.parseToJsonElement(text).jsonObject
            val answer = root["choices"]?.jsonArray?.getOrNull(0)
                ?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
                ?: return AiGuess(null, null)
            return parseGuess(answer)
        }
    }

    private fun parseGuess(answer: String): AiGuess {
        val cleaned = answer.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()
        val obj: JsonObject = try {
            json.parseToJsonElement(cleaned).jsonObject
        } catch (e: Exception) {
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            if (start < 0 || end <= start) return AiGuess(null, null)
            json.parseToJsonElement(cleaned.substring(start, end + 1)).jsonObject
        }
        return AiGuess(
            category = obj["category"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() },
            store = obj["store"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        )
    }
}