package com.bigcash.core.parsing

import com.bigcash.core.model.ParsingConfig

/**
 * Converts a textual amount into integer minor units (cents).
 *
 * Everything it knows about currencies comes from [ParsingConfig]:
 * recognized currency symbols, decimal separator, thousands separator and the accepted range.
 */
class AmountParser(private val config: ParsingConfig) {

    private val numberRegex = Regex("""\d[\d.,\s\u00A0]*""")
    private val inlineNumberRegex: Regex by lazy {
        val pattern = config.fallbackAmountPattern.trim()
        if (pattern.isEmpty()) numberRegex else runCatching { Regex(pattern) }.getOrDefault(numberRegex)
    }

    /** Returns the amount in minor units, or null when no valid amount is present. */
    fun parse(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val cleaned = stripCurrencySymbols(raw)
        val candidates = inlineNumberRegex.findAll(cleaned)
            .map { it.value.trim() }
            .filter { it.any(Char::isDigit) }
            .toList()
        if (candidates.isEmpty()) return null

        val candidate =
            candidates.firstOrNull { it.length > 3 && it.contains(config.decimalSeparator) }
                ?: candidates.first()
        return toMinorUnits(candidate)
    }

    /** Parses a value whose decimal separator is already normalized to `,` or `.`. */
    fun toMinorUnits(rawCandidate: String): Long? {
        var value = rawCandidate
            .replace(" ", "")
            .replace("\u00A0", "")
            .replace("\u202F", "")
            .trim()
        if (value.isEmpty()) return null

        val negative = value.startsWith("-")
        value = value.removePrefix("-").removePrefix("+")

        val decimal = config.decimalSeparator
        val thousands = config.thousandsSeparator
        val hasDecimal = value.contains(decimal)
        val hasThousands = thousands != decimal && value.contains(thousands)

        if (hasDecimal && hasThousands) {
            value = value.replace(thousands.toString(), "")
        } else if (hasThousands) {
            val groups = value.split(thousands)
            val looksLikeGrouping = groups.size > 1 && groups.drop(1).all { it.length == 3 }
            value = if (looksLikeGrouping) groups.joinToString("") else value.replace(thousands.toString(), "")
        }

        value = value.replace(decimal, '.')
        value = value.filter { it.isDigit() || it == '.' }
        if (value.isEmpty() || value == ".") return null

        val normalized = if (value.count { it == '.' } > 1) {
            val lastDot = value.lastIndexOf('.')
            value.substring(0, lastDot).replace(".", "") + value.substring(lastDot)
        } else {
            value
        }

        val asDouble = normalized.toDoubleOrNull() ?: return null
        val minor = Math.round(asDouble * 100.0)
        if (minor < config.minAmountMinor || minor > config.maxAmountMinor) return null
        return if (negative) -minor else minor
    }

    private fun stripCurrencySymbols(value: String): String {
        var result = value
        config.currencySymbols.forEach { symbol ->
            if (symbol.isNotBlank()) {
                result = result.replace(symbol, " ", ignoreCase = true)
            }
        }
        return result
    }
}
