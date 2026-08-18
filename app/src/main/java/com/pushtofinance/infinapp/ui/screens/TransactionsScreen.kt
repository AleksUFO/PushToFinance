package com.pushtofinance.infinapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.data.TransactionEntity
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.components.AmountField
import com.pushtofinance.infinapp.ui.components.SheetEditor
import com.pushtofinance.infinapp.ui.components.EmptyState
import com.pushtofinance.infinapp.ui.components.IconAvatar
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.components.toEntityColor
import com.pushtofinance.infinapp.util.Format
import com.pushtofinance.infinapp.util.formatDateInput
import com.pushtofinance.infinapp.util.parseDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsContent(vm: MainViewModel) {
    val transactions by vm.transactions.collectAsState()
    val methods by vm.methods.collectAsState()
    val categories by vm.categories.collectAsState()
    val stores by vm.stores.collectAsState()

    var editing by remember { mutableStateOf<TransactionEntity?>(null) }
    var showNew by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (transactions.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.ReceiptLong, null, tint = MaterialTheme.colorScheme.outline) },
                title = "No transactions",
                subtitle = "Add manually or wait for captured payments from banking apps."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { t ->
                    val method = methods.firstOrNull { it.id == t.paymentMethodId }
                    val cat = categories.firstOrNull { it.id == t.categoryId }
                    val store = stores.firstOrNull { it.id == t.storeId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editing = t },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                        IconAvatar(
                            letter = method?.iconLetter ?: cat?.iconLetter ?: "T",
                            color = (method?.color ?: cat?.color ?: 0xFF64748B).toEntityColor(),
                            size = 42.dp
                        )
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(
                                store?.name ?: t.note ?: (method?.name ?: "Transaction"),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${Format.date(t.timestamp)} • ${cat?.name ?: "no category"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            if (t.isIncome) "+${Format.money(t.amount, t.currency)}" else "-${Format.money(t.amount, t.currency)}",
                            fontWeight = FontWeight.Bold,
                            color = if (t.isIncome) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                        )
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showNew = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }
    }

    val active = if (showNew) TransactionEntity(amount = 0.0, currency = "PLN", amountPln = 0.0, timestamp = System.currentTimeMillis()) else editing
    if (active != null) {
        TransactionEditor(
            transaction = active,
            isNew = showNew,
            methods = methods,
            categories = categories,
            stores = stores,
            onDismiss = {
                showNew = false
                editing = null
            },
            onSave = { t, storeName ->
                vm.saveTransactionWithStore(t, storeName, showNew)
                showNew = false
                editing = null
            },
            onDelete = {
                if (!showNew) vm.deleteTransaction(active)
                editing = null
            }
        )
    }
}

@Composable
private fun TransactionEditor(
    transaction: TransactionEntity,
    isNew: Boolean,
    methods: List<com.pushtofinance.infinapp.data.PaymentMethodEntity>,
    categories: List<com.pushtofinance.infinapp.data.CategoryEntity>,
    stores: List<com.pushtofinance.infinapp.data.StoreEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity, String?) -> Unit,
    onDelete: () -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString().replace('.', ',')) }
    var currency by remember { mutableStateOf(transaction.currency) }
    var store by remember {
        mutableStateOf(transaction.storeId?.let { id -> stores.firstOrNull { it.id == id }?.name } ?: "")
    }
    var categoryId by remember { mutableStateOf(transaction.categoryId) }
    var methodId by remember { mutableStateOf(transaction.paymentMethodId ?: methods.firstOrNull { it.isDefault }?.id) }
    var date by remember { mutableStateOf(formatDateInput(transaction.timestamp)) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var isIncome by remember { mutableStateOf(transaction.isIncome) }

    SheetEditor(
        title = if (isNew) "New transaction" else "Edit transaction",
        onDismiss = onDismiss,
        onDelete = if (isNew) null else onDelete,
        onConfirm = {
            val amountVal = Format.parseAmount(amount)
            val ts = parseDate(date) ?: System.currentTimeMillis()
            if (amountVal != null) {
                onSave(
                    transaction.copy(
                        amount = amountVal,
                        currency = currency,
                        amountPln = amountVal, // converted on save in the ViewModel
                        categoryId = categoryId,
                        paymentMethodId = methodId,
                        note = note.takeIf { it.isNotBlank() },
                        timestamp = ts,
                        isIncome = isIncome
                    ),
                    store
                )
            }
        }
    ) {
        AmountField(amount, { amount = it }, currency, { currency = it })
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Switch(checked = isIncome, onCheckedChange = { isIncome = it })
            Text(
                "Income",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isIncome) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        OutlinedTextField(
            value = store,
            onValueChange = { store = it },
            label = { Text("Store") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        PickerField("Category", categories.firstOrNull { it.id == categoryId }, categories, { categoryDisplayName(it, categories) }, { categoryId = it.id }, Modifier.padding(top = 8.dp))
        PickerField("Method", methods.firstOrNull { it.id == methodId }, methods, { "${it.name}${if (it.isDefault) " (default)" else ""}" }, { methodId = it.id }, Modifier.padding(top = 8.dp))
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (dd.MM.yyyy)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}