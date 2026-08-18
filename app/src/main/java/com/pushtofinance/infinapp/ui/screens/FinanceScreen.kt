package com.pushtofinance.infinapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pushtofinance.infinapp.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(vm: MainViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Methods", "Transactions", "Budgets", "Categories", "Pushes")

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        PrimaryTabRow(
            selectedTabIndex = tab,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            tabs.forEachIndexed { i, label ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i },
                    text = {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            when (tab) {
                0 -> PaymentsContent(vm)
                1 -> TransactionsContent(vm)
                2 -> BudgetsSection(vm)
                3 -> CategoriesContent(vm)
                else -> PushLogContent(vm)
            }
        }
    }
}