package com.pushtofinance.infinapp.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import com.pushtofinance.infinapp.ai.AiProvider
import com.pushtofinance.infinapp.notification.ListenerKeepAliveService
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.Routes
import com.pushtofinance.infinapp.ui.components.AppSelectionList
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.components.rememberInstalledApps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val themePref by vm.theme.collectAsState()
    val selectedApps by vm.selectedApps.collectAsState()
    val aiKey by vm.aiKey.collectAsState()
    val aiProvider by vm.aiProvider.collectAsState()
    val aiModel by vm.aiModel.collectAsState()
    val aiBaseUrl by vm.aiBaseUrl.collectAsState()
    val keepAlive by vm.keepAlive.collectAsState()
    val apps by rememberInstalledApps(context)

    var keyDraft by remember { mutableStateOf(aiKey) }
    var providerDraft by remember { mutableStateOf(aiProvider) }
    var modelDraft by remember { mutableStateOf(aiModel) }
    var baseUrlDraft by remember { mutableStateOf(aiBaseUrl) }

    val listenerEnabled = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    val currentProvider = AiProvider.fromId(providerDraft)

    LaunchedEffect(providerDraft) {
        if (modelDraft.isBlank()) {
            modelDraft = currentProvider.defaultModel
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Palette, null, tint = MaterialTheme.colorScheme.primary)
                            Text("  Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                            FilterChip(selected = themePref == "SYSTEM", onClick = { vm.setTheme("SYSTEM") }, label = { Text("System") })
                            FilterChip(selected = themePref == "LIGHT", onClick = { vm.setTheme("LIGHT") }, label = { Text("Light") })
                            FilterChip(selected = themePref == "DARK", onClick = { vm.setTheme("DARK") }, label = { Text("Dark") })
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(top = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                            Text("  Notification access", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            if (listenerEnabled) "Notification access: granted" else "Notification access: not granted",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (listenerEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        if (!listenerEnabled) {
                            Button(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Grant notification access")
                            }
                        }
                        Text(
                            "Enable notification listening in system settings if it is not working yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            androidx.compose.material3.Switch(
                                checked = keepAlive,
                                onCheckedChange = { on ->
                                    vm.setKeepAlive(on)
                                    if (on) ListenerKeepAliveService.start(context)
                                    else ListenerKeepAliveService.stop(context)
                                }
                            )
                            Text(
                                "  Background listening",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            "Keeps the listener alive in the background with a foreground service. Turn it off to save battery — notifications are then read mainly when you open the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(top = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SmartToy, null, tint = MaterialTheme.colorScheme.primary)
                            Text("  AI provider", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Automatic category and store detection for captured payments.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        PickerField(
                            label = "Provider",
                            current = currentProvider,
                            options = AiProvider.entries.toList(),
                            display = { it.label },
                            onSelect = {
                                providerDraft = it.id
                                modelDraft = it.defaultModel
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        if (currentProvider == AiProvider.CUSTOM) {
                            OutlinedTextField(
                                value = baseUrlDraft,
                                onValueChange = { baseUrlDraft = it },
                                label = { Text("Base URL (OpenAI-compatible)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                        OutlinedTextField(
                            value = modelDraft,
                            onValueChange = { modelDraft = it },
                            label = { Text("Model") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        OutlinedTextField(
                            value = keyDraft,
                            onValueChange = { keyDraft = it },
                            label = { Text("API key") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        TextButton(
                            onClick = {
                                vm.setAiProvider(providerDraft)
                                vm.setAiKey(keyDraft)
                                vm.setAiModel(modelDraft)
                                vm.setAiBaseUrl(baseUrlDraft)
                            }
                        ) { Text("Save API settings") }
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(top = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monitored apps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Apps whose payment notifications will be captured.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        AppSelectionList(
                            apps = apps,
                            selected = selectedApps,
                            onToggle = { pkg, checked ->
                                val next = if (checked) selectedApps + pkg else selectedApps - pkg
                                vm.setSelectedApps(next)
                            }
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(top = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        OutlinedButton(
                            onClick = { nav.navigate(Routes.FINANCE) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.History, null)
                            Text("  Captured pushes")
                        }
                        OutlinedButton(
                            onClick = { vm.setOnboarded(false) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.RestartAlt, null)
                            Text("  Restart onboarding")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(top = 12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Testing", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Send a simulated payment push to verify capturing, parsing and saving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        OutlinedButton(
                            onClick = { nav.navigate(Routes.TEST_PUSH) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.BugReport, null)
                            Text("  Test push")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.padding(top = 12.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "  PushToFinance v${com.pushtofinance.infinapp.BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}