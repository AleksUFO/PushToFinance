package com.pushtofinance.infinapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.pushtofinance.infinapp.data.BudgetEntity
import com.pushtofinance.infinapp.data.CategoryEntity
import com.pushtofinance.infinapp.data.TransactionEntity
import com.pushtofinance.infinapp.ui.DashboardData
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.Routes
import com.pushtofinance.infinapp.ui.components.IconAvatar
import com.pushtofinance.infinapp.ui.components.toEntityColor
import com.pushtofinance.infinapp.util.Format
import com.pushtofinance.infinapp.util.monthStart
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(vm: MainViewModel, nav: NavHostController) {
    val dataOrNull by vm.dashboard.collectAsState()
    val ms = remember { monthStart(System.currentTimeMillis()) }

    if (dataOrNull == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    val data = dataOrNull!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "P",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    "PushToFinance",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 10.dp)
                )
                Text(
                    Format.date(System.currentTimeMillis()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Total balance",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    Format.money(data.totalBalance),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "${data.methods.size} payment methods",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (data.pendingPushCount > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        if (data.pendingPushCount == 1)
                            "  1 captured payment waiting to be saved"
                        else
                            "  ${data.pendingPushCount} captured payments waiting to be saved",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Spent", style = MaterialTheme.typography.labelMedium)
                    Text(
                        Format.money(data.thisMonthSpent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        "vs ${Format.money(data.prevMonthSpent)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Income", style = MaterialTheme.typography.labelMedium)
                    Text(
                        Format.money(data.thisMonthIncome),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        "this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pockets", style = MaterialTheme.typography.labelMedium)
                    val active = data.pockets.count { it.endDate >= System.currentTimeMillis() && it.startDate <= System.currentTimeMillis() }
                    Text(
                        active.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        "active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        val monthlyBudgets = data.budgets.filter { it.period == "MONTHLY" }
        if (monthlyBudgets.isNotEmpty()) {
            SectionCard("Monthly budgets", Icons.Filled.PieChart) {
                monthlyBudgets.forEach { b ->
                    val spent = spendFor(b, data.transactions, ms, monthEnd(ms))
                    BudgetRow(b.name, spent, b.amount, b.categoryId, data.categories)
                }
            }
        }

        val totalBudgets = data.budgets.filter { it.period == "TOTAL" }
        if (totalBudgets.isNotEmpty()) {
            SectionCard("Total budgets", Icons.Filled.Savings) {
                totalBudgets.forEach { b ->
                    val spent = spendFor(b, data.transactions, 0L, Long.MAX_VALUE)
                    BudgetRow(b.name, spent, b.amount, b.categoryId, data.categories)
                }
            }
        }

        val topCats = data.byCategory.entries
            .filter { it.key != null }
            .sortedByDescending { it.value }
            .take(6)
        if (topCats.isNotEmpty()) {
            SectionCard("Categories this month", Icons.Filled.Category) {
                val max = topCats.maxOf { it.value }
                topCats.forEach { (catId, spent) ->
                    val cat = data.categories.firstOrNull { it.id == catId }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        IconAvatar(
                            letter = cat?.iconLetter ?: "?",
                            color = colorOf(cat, catId),
                            size = 30.dp
                        )
                        Text(cat?.name ?: "No category", modifier = Modifier.padding(start = 10.dp).weight(1f))
                        Text(Format.money(spent), fontWeight = FontWeight.Medium)
                    }
                    LinearProgressIndicator(
                        progress = { if (max > 0) (spent / max).toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .height(6.dp)
                    )
                }
            }
        }

        SectionCard("Quick access", Icons.Filled.TrendingUp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = false, onClick = { nav.navigate(Routes.FINANCE) }, label = { Text("Categories") })
                FilterChip(selected = false, onClick = { nav.navigate(Routes.FINANCE) }, label = { Text("Budgets") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                FilterChip(selected = false, onClick = { nav.navigate(Routes.FINANCE) }, label = { Text("Transactions") })
                FilterChip(selected = false, onClick = { nav.navigate(Routes.FINANCE) }, label = { Text("Payment methods") })
            }
        }
    }
}

private fun monthEnd(ts: Long): Long = com.pushtofinance.infinapp.util.addMonths(monthStart(ts), 1) - 1

private fun spendFor(b: BudgetEntity, txs: List<TransactionEntity>, from: Long, to: Long): Double {
    val filtered = txs.filter { it.timestamp in from..to }
    return if (b.categoryId != null) filtered.filter { it.categoryId == b.categoryId }.sumOf { it.amountPln }
    else filtered.sumOf { it.amountPln }
}

private fun colorOf(cat: CategoryEntity?, catId: Long?): Color {
    if (cat != null) return cat.color.toEntityColor()
    val seed = (catId ?: 0L)
    val idx = ((seed % com.pushtofinance.infinapp.ui.components.AvatarPalette.size).toInt() + com.pushtofinance.infinapp.ui.components.AvatarPalette.size) % com.pushtofinance.infinapp.ui.components.AvatarPalette.size
    return com.pushtofinance.infinapp.ui.components.AvatarPalette[idx]
}

@Composable
private fun BudgetRow(name: String, spent: Double, budget: Double, categoryId: Long?, cats: List<CategoryEntity>) {
    val fraction = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val over = budget > 0 && spent > budget
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconAvatar(
                letter = cats.firstOrNull { it.id == categoryId }?.iconLetter ?: "Σ",
                color = colorOf(cats.firstOrNull { it.id == categoryId }, categoryId),
                size = 30.dp
            )
            Text(name, modifier = Modifier.padding(start = 10.dp).weight(1f), fontWeight = FontWeight.Medium)
            Text(
                "${Format.money(spent)} / ${Format.money(budget)}",
                fontWeight = FontWeight.Medium,
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(6.dp)
        )
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "  $title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}