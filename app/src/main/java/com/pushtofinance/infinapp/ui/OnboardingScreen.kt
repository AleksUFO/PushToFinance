package com.pushtofinance.infinapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.pushtofinance.infinapp.ai.AiProvider
import com.pushtofinance.infinapp.ui.components.AppSelectionList
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.components.rememberInstalledApps

@Composable
fun OnboardingScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val selectedApps by vm.selectedApps.collectAsState()
    val themePref by vm.theme.collectAsState()
    val apps by rememberInstalledApps(context)

    var provider by remember { mutableStateOf(AiProvider.GEMINI.id) }
    var model by remember { mutableStateOf(AiProvider.GEMINI.defaultModel) }
    var keyDraft by remember { mutableStateOf("") }
    val listenerEnabled = remember(context) {
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }
    var notificationsGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 || ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val currentProvider = AiProvider.fromId(provider)
    LaunchedEffect(provider) {
        model = currentProvider.defaultModel
    }

    Scaffold { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                "Welcome to PushToFinance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "PushToFinance captures payment pushes from your banking apps and helps you record them — with card, amount, store and category.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            StepCard(1, "Notification access", "PushToFinance must read notifications to detect payments.") {
                if (listenerEnabled) {
                    StatusRow("Granted")
                } else {
                    Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
                        Text("Grant access")
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= 33) {
                StepCard(2, "Notifications", "Allow the app to show notifications about captured payments.") {
                    if (notificationsGranted) {
                        StatusRow("Granted")
                    } else {
                        Button(onClick = {
                            ActivityCompat.requestPermissions(
                                (context as android.app.Activity),
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                100
                            )
                        }) {
                            Text("Allow")
                        }
                    }
                }
            }

            StepCard(3, "Select banking apps", "Which apps should payments be captured from?") {
                AppSelectionList(
                    apps = apps,
                    selected = selectedApps,
                    onToggle = { pkg, checked ->
                        val next = if (checked) selectedApps + pkg else selectedApps - pkg
                        vm.setSelectedApps(next)
                    }
                )
            }

            StepCard(4, "AI provider (optional)", "Automatic category and store detection. Paste an API key (optional).") {
                PickerField(
                    label = "Provider",
                    current = currentProvider,
                    options = AiProvider.entries.toList(),
                    display = { it.label },
                    onSelect = {
                        provider = it.id
                        model = it.defaultModel
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
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
            }

            StepCard(5, "Theme", "Choose the display mode.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = themePref == "SYSTEM", onClick = { vm.setTheme("SYSTEM") }, label = { Text("System") })
                    FilterChip(selected = themePref == "LIGHT", onClick = { vm.setTheme("LIGHT") }, label = { Text("Light") })
                    FilterChip(selected = themePref == "DARK", onClick = { vm.setTheme("DARK") }, label = { Text("Dark") })
                }
            }

            Button(
                onClick = {
                    if (keyDraft.isNotBlank()) {
                        vm.setAiProvider(provider)
                        vm.setAiKey(keyDraft)
                        vm.setAiModel(model)
                    }
                    vm.setOnboarded(true)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Get started")
            }
            Text(
                "Remember: allow notification listening and select your banking apps.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StepCard(number: Int, title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "  $number. $title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun StatusRow(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text("  $label", color = MaterialTheme.colorScheme.primary)
    }
}