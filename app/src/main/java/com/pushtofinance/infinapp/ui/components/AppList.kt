package com.pushtofinance.infinapp.ui.components

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(val pkg: String, val label: String)

object InstalledAppsCache {
    @Volatile
    var cached: List<InstalledApp>? = null
}

@Composable
fun rememberInstalledApps(context: Context): State<List<InstalledApp>> {
    return produceState(initialValue = InstalledAppsCache.cached ?: emptyList(), context) {
        val cached = InstalledAppsCache.cached
        if (cached != null) {
            value = cached
        } else {
            val computed = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                pm.getInstalledApplications(PackageManager.MATCH_ALL)
                    .filter { it.packageName != context.packageName }
                    .mapNotNull { app ->
                        runCatching {
                            val label = pm.getApplicationLabel(app).toString()
                            InstalledApp(app.packageName, label)
                        }.getOrNull()
                    }
                    .distinctBy { it.pkg }
                    .sortedBy { it.label.lowercase() }
            }
            InstalledAppsCache.cached = computed
            value = computed
        }
    }
}

@Composable
fun AppSelectionList(
    apps: List<InstalledApp>,
    selected: Set<String>,
    onToggle: (String, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val q = query.trim()
    val filtered = remember(apps, q) {
        if (q.isEmpty()) apps
        else apps.filter { it.label.contains(q, ignoreCase = true) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Apps (${selected.size} selected)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (expanded) "Tap to collapse" else "Tap to expand the list",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    AnimatedVisibility(visible = expanded) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search apps") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            if (filtered.isEmpty()) {
                Text(
                    "No results",
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(filtered, key = { it.pkg }) { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = app.pkg in selected,
                                onCheckedChange = { onToggle(app.pkg, it) }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(app.label, fontWeight = FontWeight.Medium)
                                Text(
                                    app.pkg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}