package com.bigcash.app.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigcash.app.data.ValueCodec
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.components.GlassTextField
import com.bigcash.core.model.ParsingConfig

/**
 * Every parameter the parsing engine reads, editable here.
 * Changing a value and saving immediately affects new notifications and reprocessing.
 */
@Composable
fun ParsingSection(
    config: ParsingConfig,
    onUpdate: ((ParsingConfig) -> ParsingConfig) -> Unit,
) {
    var currencySymbols by remember(config.currencySymbols) {
        mutableStateOf(config.currencySymbols.joinToString(", "))
    }
    var decimal by remember(config.decimalSeparator) { mutableStateOf(config.decimalSeparator.toString()) }
    var thousands by remember(config.thousandsSeparator) { mutableStateOf(config.thousandsSeparator.toString()) }
    var defaultCurrency by remember(config.defaultCurrency) { mutableStateOf(config.defaultCurrency) }
    var fallbackPattern by remember(config.fallbackAmountPattern) { mutableStateOf(config.fallbackAmountPattern) }
    var dedupeWindow by remember(config.dedupeWindowMillis) { mutableStateOf(config.dedupeWindowMillis.toString()) }
    var minAmount by remember(config.minAmountMinor) { mutableStateOf((config.minAmountMinor / 100).toString()) }
    var maxAmount by remember(config.maxAmountMinor) { mutableStateOf((config.maxAmountMinor / 100).toString()) }
    var incomeKeywords by remember(config.incomeKeywords) {
        mutableStateOf(config.incomeKeywords.joinToString(", "))
    }
    var expenseKeywords by remember(config.expenseKeywords) {
        mutableStateOf(config.expenseKeywords.joinToString(", "))
    }
    var methodKeywords by remember(config.methodKeywords) { mutableStateOf(ValueCodec.encodeMap(config.methodKeywords)) }

    GlassSurface {
        Text("Parsing engine", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "The engine only uses these values and the rule table. Run Reprocess history after changes.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        GlassTextField(currencySymbols, { currencySymbols = it }, "Currency symbols (comma separated)")
        GlassTextField(decimal, { decimal = it.take(1) }, "Decimal separator")
        GlassTextField(thousands, { thousands = it.take(1) }, "Thousands separator")
        GlassTextField(defaultCurrency, { defaultCurrency = it }, "Default currency")
        GlassTextField(fallbackPattern, { fallbackPattern = it }, "Fallback amount pattern", minLines = 2, singleLine = false)
        GlassTextField(dedupeWindow, { dedupeWindow = it.filter(Char::isDigit) }, "Dedupe window (ms)")
        GlassTextField(minAmount, { minAmount = it.filter(Char::isDigit) }, "Minimum amount")
        GlassTextField(maxAmount, { maxAmount = it.filter(Char::isDigit) }, "Maximum amount")
        GlassTextField(incomeKeywords, { incomeKeywords = it }, "Income keywords", minLines = 2, singleLine = false)
        GlassTextField(expenseKeywords, { expenseKeywords = it }, "Expense keywords", minLines = 2, singleLine = false)
        GlassTextField(
            value = methodKeywords,
            onValueChange = { methodKeywords = it },
            label = "Method keywords (PIX=pix,chave; CREDIT_CARD=credito)",
            minLines = 3,
            singleLine = false,
        )
        GlassTag(
            text = "Save parsing settings",
            onClick = {
                val parsedMethods = methodKeywords
                    .split(';')
                    .mapNotNull { entry ->
                        val parts = entry.split('=', limit = 2)
                        if (parts.size != 2) return@mapNotNull null
                        parts[0].trim() to parts[1].split(',').map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    .toMap()
                onUpdate { current ->
                    current.copy(
                        currencySymbols = currencySymbols.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                        decimalSeparator = decimal.firstOrNull() ?: current.decimalSeparator,
                        thousandsSeparator = thousands.firstOrNull() ?: current.thousandsSeparator,
                        defaultCurrency = defaultCurrency.ifBlank { current.defaultCurrency },
                        fallbackAmountPattern = fallbackPattern,
                        dedupeWindowMillis = dedupeWindow.toLongOrNull() ?: current.dedupeWindowMillis,
                        minAmountMinor = (minAmount.toLongOrNull() ?: 0L) * 100,
                        maxAmountMinor = (maxAmount.toLongOrNull() ?: 0L) * 100,
                        incomeKeywords = incomeKeywords.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                        expenseKeywords = expenseKeywords.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                        methodKeywords = parsedMethods,
                    )
                }
            },
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}
