package com.bigcash.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bigcash.app.data.model.AppSettings
import com.bigcash.app.ui.calibration.CalibrationScreen
import com.bigcash.app.ui.dashboard.DashboardScreen
import com.bigcash.app.ui.onboarding.OnboardingScreen
import com.bigcash.app.ui.settings.SettingsScreen
import com.bigcash.app.ui.summary.SummaryScreen
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.theme.LocalGlassTokens

private enum class AppTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Home),
    CALIBRATION("Calibrate", Icons.Filled.List),
    SUMMARY("Summary", Icons.Filled.Star),
    SETTINGS("Settings", Icons.Filled.Settings),
}

@Composable
fun BigCashRoot(
    settings: AppSettings,
    onOnboardingFinished: () -> Unit,
    onReplayOnboarding: () -> Unit,
) {
    if (!settings.onboardingCompleted) {
        OnboardingScreen(onFinish = onOnboardingFinished)
        return
    }

    var tab by rememberSaveable { mutableStateOf(AppTab.DASHBOARD) }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            GlassBottomBar(selected = tab, onSelect = { tab = it })
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (tab) {
                AppTab.DASHBOARD -> DashboardScreen(
                    modifier = Modifier.padding(top = 0.dp),
                    contentPadding = padding,
                )

                AppTab.CALIBRATION -> CalibrationScreen(contentPadding = padding)
                AppTab.SUMMARY -> SummaryScreen(contentPadding = padding)
                AppTab.SETTINGS -> SettingsScreen(
                    contentPadding = padding,
                    onReplayOnboarding = onReplayOnboarding,
                )
            }
        }
    }
}

@Composable
private fun GlassBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    val tokens = LocalGlassTokens.current
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        tokens.surfaceTint.copy(alpha = if (tokens.isDark) 0.16f else 0.85f),
                        tokens.surfaceTint.copy(alpha = if (tokens.isDark) 0.08f else 0.6f),
                    ),
                ),
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTab.entries.forEach { entry ->
            val active = entry == selected
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(role = Role.Tab) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(entry)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = entry.label,
                    tint = if (active) GlassColors.Mint else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) GlassColors.Mint else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
