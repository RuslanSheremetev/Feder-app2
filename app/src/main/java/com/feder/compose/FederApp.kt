package com.feder.compose

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient

class FederApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Глобальный обработчик непойманных исключений
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("FederCrash", "Uncaught exception in ${thread.name}", throwable)
            // Не крашим приложение, просто логируем
            // Если хотите — можно перезапустить активити
        }
    }
    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .build()
        
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
