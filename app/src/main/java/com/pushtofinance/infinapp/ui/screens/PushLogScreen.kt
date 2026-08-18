package com.pushtofinance.infinapp.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.data.Types
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.components.EmptyState
import com.pushtofinance.infinapp.util.Format

@Composable
fun PushLogContent(vm: MainViewModel) {
    val logs by vm.allPushLogs.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (logs.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.outline) },
                title = "No pushes detected",
                subtitle = "Payment pushes will appear here when the app detects them in monitored apps."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.appName, fontWeight = FontWeight.Medium)
                            Text(
                                (if (log.isIncome) "+" else "-") + (log.amount?.let { Format.money(it, log.currency ?: "PLN") } ?: "no amount"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (log.isIncome) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${Format.dateTime(log.timestamp)} • ${log.storeName ?: "no store"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            log.text?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        when (log.status) {
                                            Types.STATUS_SAVED -> "Saved"
                                            Types.STATUS_DISCARDED -> "Discarded"
                                            else -> "Pending"
                                        }
                                    )
                                }
                            )
                            IconButton(onClick = { vm.deletePushLog(log) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}