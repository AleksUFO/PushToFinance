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
import androidx.compose.material.icons.filled.HourglassEmpty
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.data.TempPocketEntity
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.components.SheetEditor
import com.pushtofinance.infinapp.ui.components.EmptyState
import com.pushtofinance.infinapp.ui.components.AmountField
import com.pushtofinance.infinapp.util.Format
import com.pushtofinance.infinapp.util.formatDateInput
import com.pushtofinance.infinapp.util.parseDate

@Composable
fun PocketsContent(vm: MainViewModel) {
    val pockets by vm.pockets.collectAsState()
    val categories by vm.categories.collectAsState()
    var editing by remember { mutableStateOf<TempPocketEntity?>(null) }
    var showNew by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (pockets.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.HourglassEmpty, null, tint = MaterialTheme.colorScheme.outline) },
                title = "No pockets",
                subtitle = "A temporary pocket restricts selected categories to a time window (e.g. holidays, festivities)."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pockets, key = { it.id }) { p ->
                    val catNames = p.categoryIds.split(",").mapNotNull { s -> s.toLongOrNull() }
                        .mapNotNull { id -> categories.firstOrNull { it.id == id }?.name }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editing = p },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            p.budgetAmount?.let { Text(Format.money(it), fontWeight = FontWeight.Bold) }
                        }
                        Text(
                            "${Format.date(p.startDate)} – ${Format.date(p.endDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (catNames.isNotEmpty()) {
                            Text(
                                catNames.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

    val active = if (showNew)
        TempPocketEntity(name = "", categoryIds = "", startDate = System.currentTimeMillis(), endDate = System.currentTimeMillis())
    else editing
    if (active != null) {
        PocketEditor(
            pocket = active,
            isNew = showNew,
            categories = categories,
            onDismiss = { showNew = false; editing = null },
            onSave = { p -> vm.savePocket(p, showNew); showNew = false; editing = null },
            onDelete = { vm.deletePocket(active); editing = null }
        )
    }
}

@Composable
private fun PocketEditor(
    pocket: TempPocketEntity,
    isNew: Boolean,
    categories: List<com.pushtofinance.infinapp.data.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TempPocketEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(pocket.name) }
    var startDate by remember { mutableStateOf(formatDateInput(pocket.startDate)) }
    var endDate by remember { mutableStateOf(formatDateInput(pocket.endDate)) }
    var budget by remember { mutableStateOf(pocket.budgetAmount?.let { Format.round2(it).toString().replace('.', ',') } ?: "") }
    var note by remember { mutableStateOf(pocket.note ?: "") }
    val selected = remember {
        mutableStateOf(pocket.categoryIds.split(",").mapNotNull { it.toLongOrNull() }.toSet())
    }

    SheetEditor(
        title = if (isNew) "New pocket" else "Edit pocket",
        onDismiss = onDismiss,
        onDelete = if (isNew) null else onDelete,
        onConfirm = {
            val s = parseDate(startDate) ?: System.currentTimeMillis()
            val e = parseDate(endDate) ?: System.currentTimeMillis()
            if (name.isNotBlank() && e >= s) {
                onSave(
                    pocket.copy(
                        name = name.trim(),
                        categoryIds = selected.value.joinToString(","),
                        startDate = s,
                        endDate = e,
                        budgetAmount = Format.parseAmount(budget),
                        note = note.takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text("Select categories covered by this pocket:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        categories.forEach { c ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = c.id in selected.value,
                    onCheckedChange = { checked ->
                        selected.value = if (checked) selected.value + c.id else selected.value - c.id
                    }
                )
                Text(c.name)
            }
        }
        OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start (dd.MM.yyyy)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text("End (dd.MM.yyyy)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        AmountField(budget, { budget = it }, "PLN", {}, Modifier.padding(top = 8.dp))
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}