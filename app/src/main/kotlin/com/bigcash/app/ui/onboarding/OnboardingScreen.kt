package com.bigcash.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import com.bigcash.app.notification.NotificationAccess
import com.bigcash.app.ui.components.GlassAction
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.LiquidBackground
import com.bigcash.app.ui.theme.GlassColors

/** First run: explains the app and opens the notification access settings. */
@Composable
fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(NotificationAccess.isGranted(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = NotificationAccess.isGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LiquidBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(text = "BigCash", style = MaterialTheme.typography.displaySmall)
            Text(
                text = "Turn bank notifications into a clean, searchable ledger. Everything stays on this device.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GlassTag(text = if (granted) "Notification access granted" else "Notification access required")

            GlassSurface(contentPadding = PaddingValues(20.dp)) {
                Text("How it works", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))
                Step("1", "Grant notification access so BigCash can read bank notifications.")
                Step("2", "Every notification from the monitored packages is stored verbatim.")
                Step("3", "Configurable rules extract amount, type, method and card digits.")
                Step("4", "Calibrate the rules with real samples until nothing fails to parse.")
            }

            GlassSurface(contentPadding = PaddingValues(20.dp)) {
                Text("Privacy", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No network calls, no analytics. Raw notifications and transactions live only in the local Room database.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            GlassAction(
                text = "Open notification access settings",
                onClick = { context.startActivity(NotificationAccess.settingsIntent()) },
            )

            GlassAction(
                text = if (granted) "Continue to dashboard" else "Continue anyway",
                enabled = true,
                onClick = onFinish,
            )

            Text(
                text = "You can revisit this checklist from Settings at any time.",
                style = MaterialTheme.typography.labelSmall,
                color = GlassColors.TextSecondaryDark,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Step(index: String, text: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = index,
            style = MaterialTheme.typography.titleMedium,
            color = GlassColors.Mint,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
