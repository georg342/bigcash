package com.bigcash.core.parsing

import com.bigcash.core.model.ParseOutcome
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.ParsingConfig
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.RuleTrace
import com.bigcash.core.model.TransactionType

/**
 * Cascading, fully data-driven parsing engine.
 *
 * Rules are ordered by [ParsingRule.priority] (1 runs first) and filtered by bank key.
 * A bank key of `*` means "applies to every bank", which keeps cross-bank rules configurable.
 * The engine never contains bank specific literals: everything comes from the rule rows and
 * [ParsingConfig], both persisted in Room and editable from the calibration panel.
 */
class ParsingEngine(
    private val config: ParsingConfig,
) {
    private val amountParser = AmountParser(config)

    fun parse(bankKey: String, text: String, rules: List<ParsingRule>): ParseOutcome {
        val traces = mutableListOf<RuleTrace>()
        var partial: ParseOutcome? = null

        for (rule in orderedRules(bankKey, rules)) {
            val match = RuleMatcher.match(rule.id, rule.name, rule.type, rule.pattern, text)
            if (match == null) {
                traces += rule.toTrace(matched = false, note = "no match")
                continue
            }

            val amount = extractAmount(rule, match)
            val type = resolvedType(rule, text)
            val method = resolvedMethod(rule, text)
            val card = groupValue(rule.cardGroup, match)?.filter(Char::isDigit)?.takeLast(6)
            val counterparty = groupValue(rule.counterpartyGroup, match)
            val currency = rule.currencyValue?.takeIf { it.isNotBlank() } ?: config.defaultCurrency

            traces += rule.toTrace(
                matched = true,
                note = if (amount == null) "matched, amount not found" else match.note,
                matchedText = match.matchedText,
                amountMinor = amount,
                type = type,
                method = method,
            )

            if (amount == null) {
                if (partial == null) {
                    partial = ParseOutcome(
                        status = ParseStatus.PARTIAL,
                        bankKey = bankKey,
                        currency = currency,
                        type = type,
                        method = method,
                        cardLastDigits = card,
                        counterparty = counterparty,
                        description = match.matchedText,
                        matchedRuleId = rule.id,
                        traces = traces.toList(),
                    )
                }
                continue
            }

            return ParseOutcome(
                status = ParseStatus.SUCCESS,
                bankKey = bankKey,
                amountMinor = amount,
                currency = currency,
                type = type,
                method = method,
                cardLastDigits = card,
                counterparty = counterparty,
                description = match.matchedText,
                matchedRuleId = rule.id,
                traces = traces.toList(),
            )
        }

        return partial ?: ParseOutcome(
            status = ParseStatus.FAILED,
            bankKey = bankKey,
            currency = config.defaultCurrency,
            traces = traces.toList(),
        )
    }


    /** Full audit trail for the playground: every rule is evaluated, even after a success. */
    fun trace(bankKey: String, text: String, rules: List<ParsingRule>): List<RuleTrace> =
        orderedRules(bankKey, rules).map { rule ->
            val match = RuleMatcher.match(rule.id, rule.name, rule.type, rule.pattern, text)
            if (match == null) {
                rule.toTrace(matched = false, note = "no match")
            } else {
                rule.toTrace(
                    matched = true,
                    note = match.note,
                    matchedText = match.matchedText,
                    amountMinor = extractAmount(rule, match),
                    type = resolvedType(rule, text),
                    method = resolvedMethod(rule, text),
                )
            }
        }

    fun amountFrom(text: String?): Long? = amountParser.parse(text)

    private fun orderedRules(bankKey: String, rules: List<ParsingRule>): List<ParsingRule> = rules
        .asSequence()
        .filter { it.isActive }
        .filter { it.bankKey == bankKey || it.bankKey == WILDCARD_BANK }
        .sortedWith(compareBy({ it.priority }, { it.id }))
        .toList()

    private fun extractAmount(rule: ParsingRule, match: RuleMatch): Long? {
        val fromGroup = groupValue(rule.amountGroup, match)
        return amountParser.parse(fromGroup) ?: amountParser.parse(match.matchedText)
    }

    private fun groupValue(index: Int?, match: RuleMatch): String? {
        if (index == null || index < 0) return null
        return match.groups.getOrNull(index)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun resolvedType(rule: ParsingRule, text: String): TransactionType =
        TransactionType.fromKey(rule.typeValue).takeIf { it != TransactionType.UNKNOWN }
            ?: inferType(text)

    private fun resolvedMethod(rule: ParsingRule, text: String): PaymentMethod =
        PaymentMethod.fromKey(rule.methodValue).takeIf { it != PaymentMethod.UNKNOWN }
            ?: inferMethod(text)

    private fun inferType(text: String): TransactionType {
        val lower = text.lowercase()
        val income = config.incomeKeywords.any { it.isNotBlank() && lower.contains(it.lowercase()) }
        val expense = config.expenseKeywords.any { it.isNotBlank() && lower.contains(it.lowercase()) }
        return when {
            income && !expense -> TransactionType.INCOME
            expense && !income -> TransactionType.EXPENSE
            else -> TransactionType.UNKNOWN
        }
    }

    private fun inferMethod(text: String): PaymentMethod {
        val lower = text.lowercase()
        val hit = config.methodKeywords.entries.firstOrNull { (_, keywords) ->
            keywords.any { it.isNotBlank() && lower.contains(it.lowercase()) }
        }
        return PaymentMethod.fromKey(hit?.key)
    }

    private fun ParsingRule.toTrace(
        matched: Boolean,
        note: String,
        matchedText: String? = null,
        amountMinor: Long? = null,
        type: TransactionType = TransactionType.UNKNOWN,
        method: PaymentMethod = PaymentMethod.UNKNOWN,
    ): RuleTrace = RuleTrace(
        ruleId = id,
        ruleName = name,
        ruleType = this.type,
        priority = priority,
        matched = matched,
        matchedText = matchedText,
        amountMinor = amountMinor,
        type = type,
        method = method,
        note = note,
    )

    companion object {
        const val WILDCARD_BANK = "*"
    }
}
