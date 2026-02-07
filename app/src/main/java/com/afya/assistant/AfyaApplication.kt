package com.afya.assistant

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.afya.assistant.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class for Afya Assistant.
 * 
 * Initializes:
 * - Dependency injection (Hilt)
 * - Background sync (WorkManager)
 * - Voice recognition engine
 * - Offline database
 */
@HiltAndroidApp
class AfyaApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Schedule periodic sync
        scheduleSyncWork()
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    
    private fun scheduleSyncWork() {
        val workManager = WorkManager.getInstance(this)
        SyncWorker.schedulePeriodic(workManager)
    }
}
