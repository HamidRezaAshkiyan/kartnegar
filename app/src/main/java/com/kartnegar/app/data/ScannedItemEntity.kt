package com.kartnegar.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kartnegar.app.domain.ItemType

@Entity(
    tableName = "scanned_items",
    indices = [Index(value = ["normalizedValue"], unique = true)]
)
data class ScannedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // ItemType.name
    val normalizedValue: String,
    val formattedValue: String,
    val bankCode: String? = null,
    val bankNameFa: String? = null,
    val bankNameEn: String? = null,
    val accentColorHex: Long? = null,
    val label: String = "",
    val expiryDate: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun itemType(): ItemType = runCatching { ItemType.valueOf(type) }.getOrDefault(ItemType.CARD)
}
