package com.pushtofinance.infinapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.notification.PushProcessor
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.components.rememberInstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPushScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apps by rememberInstalledApps(context)
    val selfPkg = context.packageName

    val testOption = com.pushtofinance.infinapp.ui.components.InstalledApp(
        pkg = selfPkg,
        label = "PushToFinance (test)"
    )
    val options = listOf(testOption) + apps

    var selectedApp by remember { mutableStateOf(testOption) }
    var title by remember { mutableStateOf("Payment at Biedronka") }
    var text by remember { mutableStateOf("Paid 25.50 PLN at Biedronka • Visa **** 1234") }
    var delaySec by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var inflow by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Test push") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Simulate a payment notification as if it came from a banking app. It is processed through the same path as a real push (parser, save, sheet / heads-up).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(modifier = Modifier.padding(top = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Parameters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    PickerField(
                        label = "App (push source)",
                        current = selectedApp,
                        options = options,
                        display = { it.label },
                        onSelect = { selectedApp = it },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Notification title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Body (amount, card, store)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    OutlinedTextField(
                        value = delaySec.toString(),
                        onValueChange = { v -> delaySec = v.toIntOrNull() ?: 0 },
                        label = { Text("Delay (seconds)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Switch(
                            checked = inflow,
                            onCheckedChange = {
                                inflow = it
                                if (it) {
                                    title = "Money credited to account"
                                    text = "3,500.00 PLN credited to your account • Salary payment"
                                } else {
                                    title = "Payment at Biedronka"
                                    text = "Paid 25.50 PLN at Biedronka • Visa **** 1234"
                                }
                            }
                        )
                        Text(
                            "Simulate income",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (busy) return@Button
                    busy = true
                    val pkg = selectedApp.pkg
                    val t = title
                    val txt = text
                    val delayMs = delaySec * 1000L
                    status = if (delayMs > 0) "Sending in $delaySec s…" else "Sending…"
                    scope.launch {
                        if (delayMs > 0) delay(delayMs)
                        withContext(Dispatchers.IO) {
                            PushProcessor(context).process(pkg, t, txt, force = true)
                        }
                        status = "Push captured — check the sheet / notification."
                        busy = false
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = null)
                Text("  Send test push")
            }

            if (status.isNotBlank()) {
                Row(modifier = Modifier.padding(top = 12.dp)) {
                    Icon(Icons.Filled.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("  $status", modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Tip: the parser detects amounts like “25.50 PLN”, “12.99 EUR”, “$4.99” and cards (Visa, Mastercard, Google Pay). Send two pushes with the same amount less than 1 minute apart to test duplicate detection.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}