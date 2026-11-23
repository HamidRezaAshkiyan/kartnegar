package com.kartnegar.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedItemDao {

    @Query("SELECT * FROM scanned_items ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<ScannedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ScannedItemEntity): Long

    @Query("UPDATE scanned_items SET label = :label WHERE id = :id")
    suspend fun updateLabel(id: Long, label: String)

    @Delete
    suspend fun delete(item: ScannedItemEntity)

    @Query("DELETE FROM scanned_items")
    suspend fun deleteAll()
}
