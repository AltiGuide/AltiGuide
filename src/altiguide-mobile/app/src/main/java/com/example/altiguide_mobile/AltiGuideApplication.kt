
package com.example.altiguide_mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import java.io.File

@HiltAndroidApp
class AltiGuideApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize OSMDroid configuration
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        // Store tile cache in app's private cache directory (no external storage permission needed)
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
    }
}

