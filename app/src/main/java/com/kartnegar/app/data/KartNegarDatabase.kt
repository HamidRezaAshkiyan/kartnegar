package com.kartnegar.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ScannedItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KartNegarDatabase : RoomDatabase() {
    abstract fun scannedItemDao(): ScannedItemDao

    companion object {
        @Volatile
        private var instance: KartNegarDatabase? = null

        fun getInstance(context: Context): KartNegarDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KartNegarDatabase::class.java,
                    "kartnegar_vault.db"
                ).build().also { instance = it }
            }
        }
    }
}
