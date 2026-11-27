package com.bigcash.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.theme.LocalGlassTokens

/** Glass styled text field. */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    supportingText: String? = null,
) {
    val tokens = LocalGlassTokens.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier.fillMaxWidth(),
        supportingText = supportingText?.let { hint -> { Text(hint) } },
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = tokens.surfaceTint.copy(alpha = if (tokens.isDark) 0.10f else 0.7f),
            unfocusedContainerColor = tokens.surfaceTint.copy(alpha = if (tokens.isDark) 0.06f else 0.5f),
            focusedIndicatorColor = GlassColors.Mint,
            unfocusedIndicatorColor = tokens.borderColor,
        ),
    )
}

/** Filled glass action button. */
@Composable
fun GlassAction(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = if (enabled) {
                    Brush.linearGradient(listOf(GlassColors.Mint, GlassColors.Periwinkle))
                } else {
                    Brush.linearGradient(
                        listOf(
                            tokens.surfaceTint.copy(alpha = 0.2f),
                            tokens.surfaceTint.copy(alpha = 0.1f),
                        ),
                    )
                },
                shape = RoundedCornerShape(20.dp),
            )
            .border(1.dp, tokens.borderColor, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, role = Role.Button) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) GlassColors.Ink else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Row of glass tags. */
@Composable
fun GlassTagRow(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { label ->
            GlassTag(text = label, modifier = Modifier.weight(1f, fill = false))
        }
    }
}
