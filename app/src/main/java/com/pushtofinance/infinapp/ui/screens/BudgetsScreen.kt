package com.pushtofinance.infinapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.data.BudgetEntity
import com.pushtofinance.infinapp.data.Types
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.components.AmountField
import com.pushtofinance.infinapp.ui.components.AvatarPalette
import com.pushtofinance.infinapp.ui.components.SheetEditor
import com.pushtofinance.infinapp.ui.components.EmptyState
import com.pushtofinance.infinapp.ui.components.IconAvatar
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.components.toEntityColor
import com.pushtofinance.infinapp.util.Format
import com.pushtofinance.infinapp.util.monthStart

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BudgetsSection(vm: MainViewModel) {
    var subTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = subTab) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Budgets") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Pockets") })
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when (subTab) {
                0 -> BudgetsContent(vm)
                else -> PocketsContent(vm)
            }
        }
    }
}

@Composable
fun BudgetsContent(vm: MainViewModel) {
    val budgets by vm.budgets.collectAsState()
    val transactions by vm.transactions.collectAsState()
    val categories by vm.categories.collectAsState()
    var editing by remember { mutableStateOf<BudgetEntity?>(null) }
    var showNew by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (budgets.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.PieChart, null, tint = MaterialTheme.colorScheme.outline) },
                title = "No budgets",
                subtitle = "Set a total or periodic budget (week / month / year)."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp)
            ) {
                items(budgets, key = { it.id }) { b ->
                    val (from, to) = windowFor(b)
                    val spent = (if (b.categoryId != null)
                        transactions.filter { it.categoryId == b.categoryId && it.timestamp in from..to && !it.isIncome }.sumOf { it.amountPln }
                    else transactions.filter { it.timestamp in from..to && !it.isIncome }.sumOf { it.amountPln })
                    val fraction = if (b.amount > 0) (spent / b.amount).toFloat().coerceIn(0f, 1f) else 0f
                    val over = b.amount > 0 && spent > b.amount
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { editing = b }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconAvatar(
                                    letter = categories.firstOrNull { it.id == b.categoryId }?.iconLetter ?: "B",
                                    color = categories.firstOrNull { it.id == b.categoryId }
                                        ?.let { it.color.toEntityColor() }
                                        ?: AvatarPalette[(b.id % AvatarPalette.size).toInt()],
                                    size = 34.dp
                                )
                                Text(
                                    b.name,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 10.dp).weight(1f)
                                )
                                Text(periodLabel(b.period), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${Format.money(spent)} of ${Format.money(b.amount, b.currency)}",
                                modifier = Modifier.padding(top = 6.dp),
                                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            LinearProgressIndicator(
                                progress = { fraction },
                                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(6.dp)
                            )
                            if (b.categoryId != null) {
                                Text(
                                    "Category: ${categoryDisplayName(categories.firstOrNull { it.id == b.categoryId }, categories)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
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

    val active = if (showNew) BudgetEntity(name = "", amount = 0.0, currency = "PLN", period = Types.PERIOD_MONTHLY) else editing
    if (active != null) {
        BudgetEditor(
            budget = active,
            isNew = showNew,
            categories = categories,
            onDismiss = { showNew = false; editing = null },
            onSave = { b -> vm.saveBudget(b, showNew); showNew = false; editing = null },
            onDelete = { vm.deleteBudget(active); editing = null }
        )
    }
}

private fun windowFor(b: BudgetEntity): Pair<Long, Long> {
    val now = System.currentTimeMillis()
    return when (b.period) {
        Types.PERIOD_WEEKLY -> now to now
        Types.PERIOD_MONTHLY -> {
            val ms = monthStart(now)
            ms to (com.pushtofinance.infinapp.util.addMonths(ms, 1) - 1)
        }
        Types.PERIOD_YEARLY -> {
            val c = java.util.Calendar.getInstance()
            c.set(java.util.Calendar.DAY_OF_YEAR, 1)
            c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0); c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0)
            c.timeInMillis to now
        }
        else -> 0L to Long.MAX_VALUE
    }
}

private fun periodLabel(p: String): String = when (p) {
    Types.PERIOD_TOTAL -> "Total"
    Types.PERIOD_WEEKLY -> "Weekly"
    Types.PERIOD_MONTHLY -> "Monthly"
    Types.PERIOD_YEARLY -> "Yearly"
    else -> p
}

@Composable
private fun BudgetEditor(
    budget: BudgetEntity,
    isNew: Boolean,
    categories: List<com.pushtofinance.infinapp.data.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (BudgetEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(budget.name) }
    var amount by remember { mutableStateOf(Format.round2(budget.amount).toString().replace('.', ',')) }
    var currency by remember { mutableStateOf(budget.currency) }
    var period by remember { mutableStateOf(budget.period) }
    var categoryId by remember { mutableStateOf(budget.categoryId) }

    val periodOptions = listOf(
        Triple(Types.PERIOD_TOTAL, "Total", "Since the beginning"),
        Triple(Types.PERIOD_WEEKLY, "Weekly", "Current week"),
        Triple(Types.PERIOD_MONTHLY, "Monthly", "Current month"),
        Triple(Types.PERIOD_YEARLY, "Yearly", "Current year")
    )

    SheetEditor(
        title = if (isNew) "New budget" else "Edit budget",
        onDismiss = onDismiss,
        onDelete = if (isNew) null else onDelete,
        onConfirm = {
            val a = Format.parseAmount(amount)
            if (a != null) {
                onSave(budget.copy(name = name.trim(), amount = a, currency = currency, period = period, categoryId = categoryId))
            }
        }
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Budget name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        AmountField(amount, { amount = it }, currency, { currency = it }, Modifier.padding(top = 8.dp))
        PickerField("Period", periodOptions.firstOrNull { it.first == period }, periodOptions, { it.second }, { period = it.first }, Modifier.padding(top = 8.dp))
        PickerField(
            "Category (optional)",
            categories.firstOrNull { it.id == categoryId },
            listOf(null as com.pushtofinance.infinapp.data.CategoryEntity?) + categories,
            { it?.name ?: "All" },
            { categoryId = it?.id },
            Modifier.padding(top = 8.dp)
        )
    }
}