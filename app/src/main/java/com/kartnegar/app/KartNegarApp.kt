package com.kartnegar.app

import android.app.Application
import com.kartnegar.app.data.KartNegarDatabase
import com.kartnegar.app.data.ScannedItemRepository
import com.kartnegar.app.ocr.OnDeviceOcrEngine

class KartNegarApp : Application() {
    lateinit var repository: ScannedItemRepository
        private set

    lateinit var ocrEngine: OnDeviceOcrEngine
        private set

    override fun onCreate() {
        super.onCreate()
        val database = KartNegarDatabase.getInstance(this)
        repository = ScannedItemRepository(database.scannedItemDao())
        ocrEngine = OnDeviceOcrEngine(this)
    }
}
