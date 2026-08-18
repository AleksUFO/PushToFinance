package com.pushtofinance.infinapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pushtofinance.infinapp.notification.CapturedPush
import com.pushtofinance.infinapp.notification.PendingCaptures
import com.pushtofinance.infinapp.ui.screens.DashboardScreen
import com.pushtofinance.infinapp.ui.screens.FinanceScreen
import com.pushtofinance.infinapp.ui.screens.SettingsScreen
import com.pushtofinance.infinapp.ui.screens.TestPushScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val FINANCE = "finance"
    const val SETTINGS = "settings"
    const val TEST_PUSH = "test_push"
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppNavHost(vm: MainViewModel) {
    val captureVm: CaptureViewModel = viewModel()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val bottomItems = listOf(
        BottomItem(Routes.DASHBOARD, "Home", Icons.Filled.Home),
        BottomItem(Routes.FINANCE, "Finance", Icons.Filled.ReceiptLong),
        BottomItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
    )
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    val pendingItems by PendingCaptures.items.collectAsState()
    val pendingLogsList by vm.pendingPushLogs.collectAsState()

    LaunchedEffect(pendingLogsList.size) {
        if (pendingLogsList.isNotEmpty() && PendingCaptures.items.value.isEmpty()) {
            PendingCaptures.seed(pendingLogsList.map { d ->
                CapturedPush(
                    id = d.id, packageName = d.packageName, appName = d.appName,
                    title = d.title, text = d.text, amount = d.amount ?: 0.0,
                    currency = d.currency ?: "PLN", amountPln = d.amountPln ?: 0.0,
                    cardName = d.cardName, storeName = d.storeName, timestamp = d.timestamp,
                    isIncome = d.isIncome
                )
            })
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 12.dp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) { DashboardScreen(vm, navController) }
            composable(Routes.FINANCE) { FinanceScreen(vm) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, navController) }
            composable(Routes.TEST_PUSH) { TestPushScreen() }
        }
    }

    if (pendingItems.isNotEmpty()) {
        CaptureSheet(
            vm = captureVm,
            onDismiss = {},
            onSaved = {}
        )
    }
}