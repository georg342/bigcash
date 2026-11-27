package com.bigcash.app

import android.app.Application
import com.bigcash.app.data.SeedInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class BigCashApp : Application() {

    @Inject
    lateinit var seedInitializer: SeedInitializer

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seedInitializer.seed() }
    }
}
