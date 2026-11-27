package com.bigcash.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bigcash.app.data.SettingsRepository
import com.bigcash.app.data.seed.DefaultContent
import com.bigcash.app.ui.BigCashRoot
import com.bigcash.app.ui.theme.BigCashTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = DefaultContent.settings(),
            )
            BigCashTheme(themeMode = settings.themeMode) {
                BigCashRoot(
                    settings = settings,
                    onOnboardingFinished = {
                        lifecycleScope.launch {
                            settingsRepository.update { it.copy(onboardingCompleted = true) }
                        }
                    },
                    onReplayOnboarding = {
                        lifecycleScope.launch {
                            settingsRepository.update { it.copy(onboardingCompleted = false) }
                        }
                    },
                )
            }
        }
    }
}
