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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.data.PaymentMethodEntity
import com.pushtofinance.infinapp.data.Types
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.components.AmountField
import com.pushtofinance.infinapp.ui.components.ColorRow
import com.pushtofinance.infinapp.ui.components.SheetEditor
import com.pushtofinance.infinapp.ui.components.EmptyState
import com.pushtofinance.infinapp.ui.components.IconAvatar
import com.pushtofinance.infinapp.ui.components.LetterField
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.components.toEntityColor
import com.pushtofinance.infinapp.ui.components.toStoredColor
import com.pushtofinance.infinapp.util.Format

@Composable
fun PaymentsContent(vm: MainViewModel) {
    val methods by vm.methods.collectAsState()
    var editing by remember { mutableStateOf<PaymentMethodEntity?>(null) }
    var showNew by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (methods.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.CreditCard, null, tint = MaterialTheme.colorScheme.outline) },
                title = "No payment methods",
                subtitle = "Add a card from a push, a card manually, or cash. Enter a balance for each method."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(methods, key = { it.id }) { m ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editing = m },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                        IconAvatar(m.iconLetter, m.color.toEntityColor(), size = 42.dp)
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(m.name, fontWeight = FontWeight.Medium)
                            Text(
                                typeLabel(m) + if (m.isDefault) " • default" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(Format.money(m.balance, m.currency), fontWeight = FontWeight.Bold)
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

    val active = if (showNew) PaymentMethodEntity(name = "", color = 0xFF16A34A, iconLetter = "K", type = Types.METHOD_CARD_PUSH) else editing
    if (active != null) {
        MethodEditor(
            method = active,
            isNew = showNew,
            onDismiss = { showNew = false; editing = null },
            onSave = { m -> vm.saveMethod(m, showNew); showNew = false; editing = null },
            onDelete = { vm.deleteMethod(active); editing = null }
        )
    }
}

private fun typeLabel(m: PaymentMethodEntity): String = when (m.type) {
    Types.METHOD_CARD_PUSH -> "Card (push)"
    Types.METHOD_CARD_MANUAL -> "Card (manual)"
    Types.METHOD_CASH -> "Cash"
    else -> "Method"
}

@Composable
private fun MethodEditor(
    method: PaymentMethodEntity,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (PaymentMethodEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(method.name) }
    var letter by remember { mutableStateOf(method.iconLetter) }
    var color by remember { mutableStateOf(method.color.toEntityColor()) }
    var type by remember { mutableStateOf(method.type) }
    var provider by remember { mutableStateOf(method.provider ?: Types.PROVIDER_PLAIN) }
    var balance by remember { mutableStateOf(Format.round2(method.balance).toString().replace('.', ',')) }
    var currency by remember { mutableStateOf(method.currency) }
    var isDefault by remember { mutableStateOf(method.isDefault) }

    val typeOptions = listOf(
        Triple(Types.METHOD_CARD_PUSH, "Card from push", "Automatically detected from notifications"),
        Triple(Types.METHOD_CARD_MANUAL, "Card (manual)", "Added manually"),
        Triple(Types.METHOD_CASH, "Cash", "Cash payments")
    )

    SheetEditor(
        title = if (isNew) "New payment method" else "Edit method",
        onDismiss = onDismiss,
        onDelete = if (isNew) null else onDelete,
        onConfirm = {
            val bal = Format.parseAmount(balance) ?: 0.0
            onSave(
                method.copy(
                    name = name.trim(),
                    iconLetter = letter.take(2).uppercase().ifEmpty { "M" },
                    color = color.toStoredColor(),
                    type = type,
                    provider = if (type == Types.METHOD_CARD_PUSH) provider else null,
                    balance = bal,
                    currency = currency,
                    isDefault = isDefault
                )
            )
        }
    ) {
        PickerField("Typ", typeOptions.firstOrNull { it.first == type }, typeOptions, { it.second }, { type = it.first })
        if (type == Types.METHOD_CARD_PUSH) {
            PickerField(
                "Provider",
                if (provider == Types.PROVIDER_GOOGLE_PAY) "Google Pay" else "Other",
                listOf(Types.PROVIDER_GOOGLE_PAY, Types.PROVIDER_PLAIN),
                { if (it == Types.PROVIDER_GOOGLE_PAY) "Google Pay" else "Other bank / provider" },
                { provider = it },
                Modifier.padding(top = 8.dp)
            )
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            LetterField(letter, { letter = it })
            Text(
                "The letter is shown as the method icon.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        ColorRow(color, { color = it }, Modifier.padding(top = 8.dp))
        AmountField(balance, { balance = it }, currency, { currency = it }, Modifier.padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
            Text("Set as default")
        }
    }
}