package com.bigcash.core.parsing

import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.RuleType
import com.bigcash.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Covers the configurable rule types and their precedence rules. */
class RuleTypesTest {

    private val engine = TestFixtures.engine
    private val rules = TestFixtures.rules

    @Test
    fun `keyword rule wins when it has a lower priority number`() {
        val keywordRule = ParsingRule(
            id = 5,
            name = "Keyword expense",
            bankKey = "c6",
            type = RuleType.KEYWORD,
            pattern = "pagamento, boleto",
            priority = 5,
            typeValue = TransactionType.EXPENSE.name,
            methodValue = PaymentMethod.BOLETO.name,
        )

        val outcome = engine.parse(
            bankKey = "c6",
            text = "Pagamento de boleto R$ 45,00",
            rules = rules + keywordRule,
        )

        assertEquals(ParseStatus.SUCCESS, outcome.status)
        assertEquals(45_00L, outcome.amountMinor)
        assertEquals(PaymentMethod.BOLETO, outcome.method)
        assertEquals(TransactionType.EXPENSE, outcome.type)
        assertTrue(outcome.traces.first { it.ruleId == keywordRule.id }.matched)
    }

    @Test
    fun `mask rule matches digits and reports the matched text`() {
        val maskRule = ParsingRule(
            id = 6,
            name = "Mask final digits",
            bankKey = "c6",
            type = RuleType.MASK,
            pattern = "*final ####*",
            priority = 1,
            cardGroup = 1,
        )

        val trace = engine.trace(
            bankKey = "c6",
            text = "Compra aprovada cartão final 9876 valor R$ 10,00",
            rules = listOf(maskRule),
        ).first()

        assertTrue(trace.matched)
        assertNotNull(trace.matchedText)
    }

    @Test
    fun `inactive rules are skipped`() {
        val outcome = engine.parse(
            bankKey = "nubank",
            text = "Você recebeu um Pix de R$ 10,00",
            rules = rules.map { rule ->
                if (rule.id == TestFixtures.nubankPixIn.id) rule.copy(isActive = false) else rule
            },
        )

        assertEquals(TestFixtures.wildcard.id, outcome.matchedRuleId)
        assertEquals(1_000L, outcome.amountMinor)
    }

    @Test
    fun `rules of other banks do not leak`() {
        val outcome = engine.parse(
            bankKey = "mercadopago",
            text = "Compra no débito com o cartão final 4321 no valor de R$ 89,90",
            rules = rules,
        )

        assertEquals(ParseStatus.SUCCESS, outcome.status)
        assertEquals(TestFixtures.wildcard.id, outcome.matchedRuleId)
    }

    @Test
    fun `pattern validation rejects broken regex`() {
        assertNotNull(RuleMatcher.validate(RuleType.REGEX, "([a-z"))
        assertEquals(null, RuleMatcher.validate(RuleType.REGEX, """(\d+),(\d{2})"""))
        assertNotNull(RuleMatcher.validate(RuleType.KEYWORD, "  ,  "))
    }
}
