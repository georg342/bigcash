package com.bigcash.core.parsing

import com.bigcash.core.model.RuleType

/** Result of matching one rule against a notification text. */
data class RuleMatch(
    val ruleId: Long,
    val ruleName: String,
    val matchedText: String,
    val groups: List<String>,
    val note: String = "",
)

/**
 * Evaluates the three configurable rule types.
 *
 * - [RuleType.REGEX]: the pattern is compiled as-is.
 * - [RuleType.MASK]: `#` matches a digit, `*` matches any run of characters, everything else is literal.
 * - [RuleType.KEYWORD]: the pattern is a comma separated keyword list; the first contained keyword matches.
 */
object RuleMatcher {

    fun maskToRegex(mask: String): String = buildString {
        mask.forEach { char ->
            when (char) {
                '#' -> append("\\d")
                '*' -> append(".*")
                else -> append(Regex.escape(char.toString()))
            }
        }
    }

    fun describePattern(type: RuleType, pattern: String): String = when (type) {
        RuleType.REGEX -> pattern
        RuleType.MASK -> maskToRegex(pattern)
        RuleType.KEYWORD -> keywordsOf(pattern).joinToString("|") { Regex.escape(it) }
    }

    fun keywordsOf(pattern: String): List<String> = pattern
        .split(',', ';', '|')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    fun match(ruleId: Long, ruleName: String, type: RuleType, pattern: String, text: String): RuleMatch? {
        if (pattern.isBlank() || text.isBlank()) return null
        return when (type) {
            RuleType.KEYWORD -> {
                val hit = keywordsOf(pattern).firstOrNull { text.contains(it, ignoreCase = true) }
                hit?.let {
                    RuleMatch(
                        ruleId = ruleId,
                        ruleName = ruleName,
                        matchedText = it,
                        groups = listOf(it),
                        note = "keyword match",
                    )
                }
            }

            RuleType.MASK, RuleType.REGEX -> {
                val regex = runCatching {
                    Regex(
                        pattern = if (type == RuleType.MASK) maskToRegex(pattern) else pattern,
                        options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
                    )
                }.getOrNull() ?: return null

                regex.find(text)?.let { result ->
                    RuleMatch(
                        ruleId = ruleId,
                        ruleName = ruleName,
                        matchedText = result.value,
                        groups = result.groupValues,
                        note = if (type == RuleType.MASK) "mask match" else "regex match",
                    )
                }
            }
        }
    }

    /** Validates a user supplied pattern so the calibration panel can show errors before saving. */
    fun validate(type: RuleType, pattern: String): String? {
        if (pattern.isBlank()) return "Pattern must not be blank"
        return when (type) {
            RuleType.KEYWORD -> if (keywordsOf(pattern).isEmpty()) "Add at least one keyword" else null
            RuleType.MASK, RuleType.REGEX -> runCatching {
                Regex(if (type == RuleType.MASK) maskToRegex(pattern) else pattern)
            }.exceptionOrNull()?.let { "Invalid pattern: ${it.message}" }
        }
    }
}
