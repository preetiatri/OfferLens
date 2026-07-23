package com.offerlens

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import net.sqlcipher.database.SQLiteDatabase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class OfferLensApplication : Application(), coil.ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SQLCipher for encrypted database
        SQLiteDatabase.loadLibs(this)
        
        // Initialize Timber for logging
        if (com.offerlens.BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        
        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!com.offerlens.BuildConfig.DEBUG)
        
        Timber.d("OfferLens Application initialized")
    }

    /**
     * A Timber Tree for Release builds that pipes errors to Crashlytics.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) {
                return
            }

            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log(message)
            
            if (t != null) {
                crashlytics.recordException(t)
            }
        }
    }

    override fun newImageLoader(): coil.ImageLoader {
        return coil.ImageLoader.Builder(this)
            .memoryCache {
                coil.memory.MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
