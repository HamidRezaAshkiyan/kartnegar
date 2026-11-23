package com.kartnegar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kartnegar.app.data.ScannedItemEntity
import com.kartnegar.app.domain.ItemType

@Composable
fun VaultScreen(
    items: List<ScannedItemEntity>,
    onCopyValue: (String) -> Unit,
    onShareValue: (String) -> Unit,
    onUpdateLabel: (Long, String) -> Unit,
    onDeleteItem: (ScannedItemEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<ItemType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<ScannedItemEntity?>(null) }
    var editLabelText by remember { mutableStateOf("") }

    val filtered = remember(items, selectedFilter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        items.filter { entity ->
            val matchesType = selectedFilter == null || entity.itemType() == selectedFilter
            val matchesQuery = q.isEmpty() ||
                entity.normalizedValue.lowercase().contains(q) ||
                entity.label.lowercase().contains(q) ||
                (entity.bankNameEn?.lowercase()?.contains(q) == true) ||
                (entity.bankNameFa?.lowercase()?.contains(q) == true)
            matchesType && matchesQuery
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            placeholder = { Text("Search label, bank, or digits...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${items.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == ItemType.CARD,
                    onClick = { selectedFilter = ItemType.CARD },
                    label = { Text("Cards") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == ItemType.SHEBA,
                    onClick = { selectedFilter = ItemType.SHEBA },
                    label = { Text("Sheba") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == ItemType.NATIONAL_ID,
                    onClick = { selectedFilter = ItemType.NATIONAL_ID },
                    label = { Text("National ID") }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (items.isEmpty()) "Your local vault is empty" else "No matching items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Scan a screenshot and tap Save to store cards, Shebas, and National IDs offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { entity ->
                    VaultItemCard(
                        entity = entity,
                        onCopy = { onCopyValue(entity.normalizedValue) },
                        onShare = { onShareValue(entity.normalizedValue) },
                        onEdit = {
                            editLabelText = entity.label
                            editingItem = entity
                        },
                        onDelete = { onDeleteItem(entity) }
                    )
                }
            }
        }
    }

    editingItem?.let { target ->
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit Label") },
            text = {
                OutlinedTextField(
                    value = editLabelText,
                    onValueChange = { editLabelText = it },
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateLabel(target.id, editLabelText)
                    editingItem = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun VaultItemCard(
    entity: ScannedItemEntity,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val type = entity.itemType()
    val accent = when {
        entity.accentColorHex != null -> Color(entity.accentColorHex)
        type == ItemType.NATIONAL_ID -> Color(0xFF0F766E)
        type == ItemType.SHEBA -> Color(0xFF2563EB)
        else -> MaterialTheme.colorScheme.primary
    }
    val icon = when (type) {
        ItemType.CARD -> Icons.Outlined.CreditCard
        ItemType.SHEBA -> Icons.Outlined.AccountBalance
        ItemType.NATIONAL_ID -> Icons.Outlined.Badge
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = entity.label.ifBlank {
                                entity.bankNameFa ?: when (type) {
                                    ItemType.CARD -> "شماره کارت"
                                    ItemType.SHEBA -> "شماره شبا"
                                    ItemType.NATIONAL_ID -> "کد ملی"
                                }
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (entity.bankNameEn != null) {
                            Text(
                                text = entity.bankNameEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy")
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = entity.formattedValue,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (type == ItemType.SHEBA) 14.sp else 17.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
