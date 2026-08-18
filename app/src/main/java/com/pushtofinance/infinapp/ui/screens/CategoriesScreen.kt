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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushtofinance.infinapp.data.CategoryEntity
import com.pushtofinance.infinapp.data.Types
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.components.ColorRow
import com.pushtofinance.infinapp.ui.components.SheetEditor
import com.pushtofinance.infinapp.ui.components.EmptyState
import com.pushtofinance.infinapp.ui.components.IconAvatar
import com.pushtofinance.infinapp.ui.components.LetterField
import com.pushtofinance.infinapp.ui.components.PickerField
import com.pushtofinance.infinapp.ui.components.toEntityColor
import com.pushtofinance.infinapp.ui.components.toStoredColor

fun categoryDisplayName(
    cat: com.pushtofinance.infinapp.data.CategoryEntity?,
    cats: List<com.pushtofinance.infinapp.data.CategoryEntity>
): String {
    if (cat == null) return "no category"
    val parent = cats.firstOrNull { it.id == cat.parentId }
    return if (parent != null) "${parent.name} / ${cat.name}" else cat.name
}

@Composable
fun CategoriesContent(vm: MainViewModel) {
    val categories by vm.categories.collectAsState()
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var showNew by remember { mutableStateOf(false) }

    val grouped = remember(categories) {
        val parents = categories.filter { it.parentId == null || categories.none { p -> p.id == it.parentId } }
        val children = categories.filter { it.parentId != null && categories.any { p -> p.id == it.parentId } }
        parents.map { it to 0 } + children.map { it to 1 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (categories.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Filled.Category, null, tint = MaterialTheme.colorScheme.outline) },
                title = "No categories",
                subtitle = "Create expense categories: planned, spontaneous, birthdays, etc."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grouped, key = { it.first.id }) { (c, indent) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editing = c },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = if (indent > 0) 26.dp else 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp)
                        ) {
                        IconAvatar(c.iconLetter, c.color.toEntityColor(), size = if (indent > 0) 34.dp else 42.dp)
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(c.name, fontWeight = FontWeight.Medium)
                            Text(
                                kindLabel(c.kind),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(if (c.isSystem) "System" else "Custom", style = MaterialTheme.typography.bodySmall)
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

    val active = if (showNew) CategoryEntity(name = "", color = 0xFF7C3AED, iconLetter = "K", kind = Types.CAT_OTHER) else editing
    if (active != null) {
        CategoryEditor(
            category = active,
            isNew = showNew,
            categories = categories,
            onDismiss = { showNew = false; editing = null },
            onSave = { c -> vm.saveCategory(c, showNew); showNew = false; editing = null },
            onDelete = { if (!active.isSystem) vm.deleteCategory(active); editing = null }
        )
    }
}

private fun kindLabel(kind: String): String = when (kind) {
    Types.CAT_PLANNED -> "Planned"
    Types.CAT_SPONTANEOUS -> "Spontaneous"
    else -> "Other"
}

@Composable
private fun CategoryEditor(
    category: CategoryEntity,
    isNew: Boolean,
    categories: List<com.pushtofinance.infinapp.data.CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var letter by remember { mutableStateOf(category.iconLetter) }
    var color by remember { mutableStateOf(category.color.toEntityColor()) }
    var kind by remember { mutableStateOf(category.kind) }
    var parentId by remember { mutableStateOf(category.parentId) }

    val kindOptions = listOf(
        Triple(Types.CAT_PLANNED, "Planned", "Planned expenses"),
        Triple(Types.CAT_SPONTANEOUS, "Spontaneous", "Impulsive expenses"),
        Triple(Types.CAT_OTHER, "Other", "Everything else")
    )

    SheetEditor(
        title = if (isNew) "New category" else "Edit category",
        onDismiss = onDismiss,
        onDelete = if (isNew || category.isSystem) null else onDelete,
        onConfirm = {
            onSave(
                category.copy(
                    name = name.trim(),
                    iconLetter = letter.take(2).uppercase().ifEmpty { "K" },
                    color = color.toStoredColor(),
                    kind = kind,
                    parentId = parentId
                )
            )
        }
    ) {
        PickerField("Type", kindOptions.firstOrNull { it.first == kind }, kindOptions, { it.second }, { kind = it.first })
        PickerField(
            "Parent category (optional)",
            categories.firstOrNull { it.id == parentId },
            listOf(null as com.pushtofinance.infinapp.data.CategoryEntity?) + categories.filter { it.id != category.id },
            { it?.name ?: "None (top-level)" },
            { parentId = it?.id },
            Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            LetterField(letter, { letter = it })
            Text(
                "The letter is shown as the category icon.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        ColorRow(color, { color = it }, Modifier.padding(top = 8.dp))
    }
}