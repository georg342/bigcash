package com.bigcash.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigcash.app.notification.NotificationAccess
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.app.ui.components.LiquidBackground
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.core.model.ThemeMode

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onReplayOnboarding: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val settings = state.settings
    val granted = NotificationAccess.isGranted(context)
    var successTemplate by remember(settings.successTemplate) { mutableStateOf(settings.successTemplate) }
    var failureTemplate by remember(settings.failureTemplate) { mutableStateOf(settings.failureTemplate) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (content != null) viewModel.importJson(content)
        }
    }

    LaunchedEffect(state.share) {
        state.share?.let { intent ->
            context.startActivity(intent)
            viewModel.consumeShare()
        }
    }

    LiquidBackground {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 40.dp,
                    bottom = contentPadding.calculateBottomPadding() + 40.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.displaySmall)
            GlassTag(
                text = if (granted) "Notification access granted" else "Notification access missing",
                selected = granted,
                onClick = { context.startActivity(NotificationAccess.settingsIntent()) },
            )

            GlassSurface {
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        GlassTag(
                            text = mode.name.lowercase().replaceFirstChar(Char::uppercase),
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setTheme(mode) },
                        )
                    }
                }
            }

            GlassSurface {
                Text("Notifications", style = MaterialTheme.typography.titleMedium)
                GlassTag(
                    text = if (settings.notificationsEnabled) "App notifications on" else "App notifications off",
                    selected = settings.notificationsEnabled,
                    onClick = { viewModel.setNotificationsEnabled(!settings.notificationsEnabled) },
                    modifier = Modifier.padding(top = 8.dp),
                )
                GlassTextField(
                    value = successTemplate,
                    onValueChange = { successTemplate = it },
                    label = "Success template",
                    modifier = Modifier.padding(top = 10.dp),
                )
                GlassTag(
                    text = "Save success template",
                    onClick = { viewModel.setSuccessTemplate(successTemplate) },
                    modifier = Modifier.padding(top = 8.dp),
                )
                GlassTextField(
                    value = failureTemplate,
                    onValueChange = { failureTemplate = it },
                    label = "Failure template",
                    modifier = Modifier.padding(top = 10.dp),
                )
                GlassTag(
                    text = "Save failure template",
                    onClick = { viewModel.setFailureTemplate(failureTemplate) },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            PackageSection(
                packages = state.packages,
                onSave = viewModel::savePackage,
                onDelete = viewModel::deletePackage,
            )
            ParsingSection(config = settings.parsing, onUpdate = viewModel::updateParsing)
            DataSection(
                onExportCsv = viewModel::exportCsv,
                onExportJson = viewModel::exportJson,
                onImport = { importLauncher.launch(arrayOf("application/json")) },
                onClearData = viewModel::clearAllData,
                onReplayOnboarding = onReplayOnboarding,
            )

            state.message?.let { message ->
                Text(text = message, style = MaterialTheme.typography.labelSmall, color = GlassColors.Mint)
            }
        }
    }
}
