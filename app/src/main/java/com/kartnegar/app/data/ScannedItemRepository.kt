package com.kartnegar.app.data

import com.kartnegar.app.domain.DetectedItem
import kotlinx.coroutines.flow.Flow

class ScannedItemRepository(private val dao: ScannedItemDao) {

    val savedItems: Flow<List<ScannedItemEntity>> = dao.observeAll()

    suspend fun saveDetectedItem(item: DetectedItem, label: String = "") {
        dao.upsert(
            ScannedItemEntity(
                type = item.type.name,
                normalizedValue = item.normalizedValue,
                formattedValue = item.formattedValue,
                bankCode = item.bank?.code,
                bankNameFa = item.bank?.nameFa,
                bankNameEn = item.bank?.nameEn,
                accentColorHex = item.bank?.accentColorHex,
                label = label.trim(),
                expiryDate = item.expiryDate,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateLabel(id: Long, label: String) {
        dao.updateLabel(id, label.trim())
    }

    suspend fun delete(item: ScannedItemEntity) {
        dao.delete(item)
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }
}
