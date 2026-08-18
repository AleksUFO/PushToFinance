package com.pushtofinance.infinapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.ai.AiGuess
import com.pushtofinance.infinapp.data.CategoryEntity
import com.pushtofinance.infinapp.data.PaymentMethodEntity
import com.pushtofinance.infinapp.data.PushLogEntity
import com.pushtofinance.infinapp.data.StoreEntity
import com.pushtofinance.infinapp.notification.PendingCaptures
import com.pushtofinance.infinapp.ui.components.AmountField
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.theme.SuccessGreen
import com.pushtofinance.infinapp.util.Format

private data class RowEdit(
    val amountText: String,
    val currency: String,
    val storeName: String,
    val categoryId: Long?,
    val methodId: Long?,
    val isIncome: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(
    vm: CaptureViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val logs by vm.pendingLogs.collectAsState()
    val methods by vm.methods.collectAsState()
    val categories by vm.categories.collectAsState()
    val stores by vm.stores.collectAsState()
    val ai by vm.ai.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val edits = remember { mutableStateMapOf<Long, RowEdit>() }

    fun defaultMethod(): Long? = methods.firstOrNull { it.isDefault }?.id ?: methods.firstOrNull()?.id

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) vm.refreshAi()
        logs.forEach { log ->
            if (!edits.containsKey(log.id)) {
                edits[log.id] = RowEdit(
                    amountText = log.amount?.let { Format.round2(it).toString().replace('.', ',') } ?: "",
                    currency = log.currency ?: "PLN",
                    storeName = log.storeName ?: "",
                    categoryId = null,
                    methodId = defaultMethod(),
                    isIncome = log.isIncome
                )
            }
        }
    }

    fun onSheetDismiss() {
        PendingCaptures.clear()
        onDismiss()
    }

    if (logs.isEmpty()) {
        LaunchedEffect(Unit) { onSheetDismiss() }
        return
    }

    ModalBottomSheet(
        onDismissRequest = { onSheetDismiss() },
        sheetState = sheetState,
        modifier = Modifier.wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                if (logs.size > 1) "Save these ${logs.size} transactions?" else "Save this transaction?",
                style = MaterialTheme.typography.titleLarge
            )
            if (logs.size > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        "Several payments detected in a short window — treated as one group.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                Format.dateTime(logs.minOf { it.timestamp }),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            logs.forEach { log ->
                val edit = edits[log.id] ?: RowEdit("", "PLN", "", null, null)
                CaptureRowCard(
                    log = log,
                    edit = edit,
                    aiGuess = ai[log.id],
                    methods = methods,
                    categories = categories,
                    stores = stores,
                    onChange = { edits[log.id] = it },
                    onDiscard = { vm.discard(log.id) }
                )
            }

            Button(
                onClick = {
                    val rows = edits.mapNotNull { (logId, e) ->
                        val amount = Format.parseAmount(e.amountText) ?: return@mapNotNull null
                        CaptureRowInput(
                            logId = logId,
                            amount = amount,
                            currency = e.currency,
                            storeName = e.storeName.takeIf { it.isNotBlank() },
                            categoryId = e.categoryId,
                            methodId = e.methodId,
                            isIncome = e.isIncome
                        )
                    }
                    vm.save(rows)
                    PendingCaptures.clear()
                    onSaved()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                Text("Save transactions")
            }
            TextButton(
                onClick = { onSheetDismiss() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Later")
            }
        }
    }
}

@Composable
private fun CaptureRowCard(
    log: PushLogEntity,
    edit: RowEdit,
    aiGuess: AiGuess?,
    methods: List<PaymentMethodEntity>,
    categories: List<CategoryEntity>,
    stores: List<StoreEntity>,
    onChange: (RowEdit) -> Unit,
    onDiscard: () -> Unit
) {
    val catName = categories.firstOrNull { it.id == edit.categoryId }
    val methodName = methods.firstOrNull { it.id == edit.methodId }

    LaunchedEffect(aiGuess) {
        if (aiGuess == null) return@LaunchedEffect
        val guessCat = aiGuess.category?.takeIf { it.isNotBlank() }
        val match = guessCat?.let { g -> categories.firstOrNull { it.name.equals(g, ignoreCase = true) } }
        if (match != null && edit.categoryId == null) {
            onChange(edit.copy(categoryId = match.id))
        }
        if (edit.storeName.isBlank() && !aiGuess.store.isNullOrBlank()) {
            onChange(edit.copy(storeName = aiGuess.store.orEmpty()))
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "  ${log.appName}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onDiscard) { Text("Discard", color = MaterialTheme.colorScheme.error) }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Switch(
                    checked = edit.isIncome,
                    onCheckedChange = { onChange(edit.copy(isIncome = it)) }
                )
                Text(
                    "Income",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (edit.isIncome) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            AmountField(
                value = edit.amountText,
                onValueChange = { onChange(edit.copy(amountText = it)) },
                currency = edit.currency,
                onCurrencyChange = { onChange(edit.copy(currency = it)) }
            )
            OutlinedTextField(
                value = edit.storeName,
                onValueChange = { onChange(edit.copy(storeName = it)) },
                label = { Text("Store") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            if (stores.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    stores.take(5).forEach { s ->
                        SuggestionChip(
                            onClick = { onChange(edit.copy(storeName = s.name)) },
                            label = { Text(s.name) }
                        )
                    }
                }
            }
            PickerField(
                label = "Category",
                current = catName,
                options = categories,
                display = { com.pushtofinance.infinapp.ui.screens.categoryDisplayName(it, categories) },
                onSelect = { onChange(edit.copy(categoryId = it.id)) },
                modifier = Modifier.padding(top = 8.dp)
            )
            PickerField(
                label = "Payment method / card",
                current = methodName,
                options = methods,
                display = { "${it.name}${if (it.isDefault) " (default)" else ""}" },
                onSelect = { onChange(edit.copy(methodId = it.id)) },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}