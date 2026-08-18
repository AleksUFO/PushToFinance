package com.pushtofinance.infinapp.notification

data class CapturedPush(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val amount: Double,
    val currency: String,
    val amountPln: Double,
    val cardName: String?,
    val storeName: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncome: Boolean = false
)

object PendingCaptures {
    private const val MERGE_WINDOW_MS = 60_000L
    private val _items = kotlinx.coroutines.flow.MutableStateFlow<List<CapturedPush>>(emptyList())
    val items: kotlinx.coroutines.flow.StateFlow<List<CapturedPush>> = _items

    fun add(item: CapturedPush) {
        val now = System.currentTimeMillis()
        val existing = _items.value.filter { now - it.timestamp < MERGE_WINDOW_MS }.toMutableList()
        existing.add(item)
        _items.value = existing
    }

    fun seed(items: List<CapturedPush>) {
        if (items.isNotEmpty()) _items.value = items
    }

    fun clear() {
        _items.value = emptyList()
    }
}