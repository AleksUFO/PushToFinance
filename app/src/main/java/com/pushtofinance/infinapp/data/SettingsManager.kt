package com.pushtofinance.infinapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.prefsDataStore by preferencesDataStore(name = "ptf_settings")

class SettingsManager(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("theme") // SYSTEM / LIGHT / DARK
        // The API key value keeps the historical "gemini_key" storage key so
        // users upgrading from the single-provider build keep their key.
        val KEY_AI_KEY = stringPreferencesKey("gemini_key")
        val KEY_AI_PROVIDER = stringPreferencesKey("ai_provider")
        val KEY_AI_MODEL = stringPreferencesKey("ai_model")
        val KEY_AI_BASE_URL = stringPreferencesKey("ai_base_url")
        val KEY_SELECTED_APPS = stringSetPreferencesKey("selected_apps")
        val KEY_ONBOARDED = booleanPreferencesKey("onboarded")
        val KEY_RATE_CACHE = stringPreferencesKey("rate_cache")
        val KEY_RATE_TS = longPreferencesKey("rate_ts")
    }

    val theme: Flow<String> = context.prefsDataStore.data.map { it[KEY_THEME] ?: "SYSTEM" }
    val aiKey: Flow<String> = context.prefsDataStore.data.map { it[KEY_AI_KEY] ?: "" }
    val aiProvider: Flow<String> = context.prefsDataStore.data.map { it[KEY_AI_PROVIDER] ?: "" }
    val aiModel: Flow<String> = context.prefsDataStore.data.map { it[KEY_AI_MODEL] ?: "" }
    val aiBaseUrl: Flow<String> = context.prefsDataStore.data.map { it[KEY_AI_BASE_URL] ?: "" }
    val selectedApps: Flow<Set<String>> = context.prefsDataStore.data.map { it[KEY_SELECTED_APPS] ?: emptySet() }
    val onboarded: Flow<Boolean> = context.prefsDataStore.data.map { it[KEY_ONBOARDED] ?: false }
    val rateCache: Flow<String> = context.prefsDataStore.data.map { it[KEY_RATE_CACHE] ?: "" }
    val rateTimestamp: Flow<Long> = context.prefsDataStore.data.map { it[KEY_RATE_TS] ?: 0L }

    suspend fun setTheme(value: String) = context.prefsDataStore.edit { it[KEY_THEME] = value }
    suspend fun setAiKey(value: String) = context.prefsDataStore.edit { it[KEY_AI_KEY] = value }
    suspend fun setAiProvider(value: String) = context.prefsDataStore.edit { it[KEY_AI_PROVIDER] = value }
    suspend fun setAiModel(value: String) = context.prefsDataStore.edit { it[KEY_AI_MODEL] = value }
    suspend fun setAiBaseUrl(value: String) = context.prefsDataStore.edit { it[KEY_AI_BASE_URL] = value }
    suspend fun setSelectedApps(value: Set<String>) = context.prefsDataStore.edit { it[KEY_SELECTED_APPS] = value }
    suspend fun setOnboarded(value: Boolean) = context.prefsDataStore.edit { it[KEY_ONBOARDED] = value }
    suspend fun setRateCache(json: String, ts: Long) = context.prefsDataStore.edit {
        it[KEY_RATE_CACHE] = json
        it[KEY_RATE_TS] = ts
    }

    suspend fun currentAiKey(): String = aiKey.first()
    suspend fun currentAiProvider(): String = aiProvider.first()
    suspend fun currentAiModel(): String = aiModel.first()
    suspend fun currentAiBaseUrl(): String = aiBaseUrl.first()
    suspend fun currentSelectedApps(): Set<String> = selectedApps.first()
    suspend fun currentRateCache(): Pair<String, Long> = rateCache.first() to rateTimestamp.first()
}