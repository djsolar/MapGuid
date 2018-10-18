package com.twinflag.mapguid

import android.app.Application
import android.content.Context
import android.os.Environment
import java.io.File
import kotlin.properties.Delegates

class MyApplication: Application() {

    companion object {
        private val TAG = "MyApplication"

        private const val DEFAULT_MAP_FOLDER = "map"

        var context: Context by Delegates.notNull()
            private set

        val MAP_PATH: String = createMapDir()
        private fun createMapDir(): String {
            var path = "/mnt/sdcard"
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                path = Environment.getExternalStorageDirectory().path
            }
            val file = File(path, DEFAULT_MAP_FOLDER)
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.path
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        val file = File(MAP_PATH)
        println(file.exists())
    }
}