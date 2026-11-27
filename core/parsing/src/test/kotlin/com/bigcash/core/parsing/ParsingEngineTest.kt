package com.bigcash.core.parsing

import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Engine tests driven by real notification wording from the three supported banks. */
class ParsingEngineTest {

    private val engine = TestFixtures.engine
    private val rules = TestFixtures.rules

    @Test
    fun `nubank pix received is parsed as income`() {
        val outcome = engine.parse(
            bankKey = "nubank",
            text = "Você recebeu um Pix de R$ 1.234,56 de Maria Souza",
            rules = rules,
        )

        assertEquals(ParseStatus.SUCCESS, outcome.status)
        assertEquals(123_456L, outcome.amountMinor)
        assertEquals(TransactionType.INCOME, outcome.type)
        assertEquals(PaymentMethod.PIX, outcome.method)
        assertEquals(TestFixtures.nubankPixIn.id, outcome.matchedRuleId)
    }

    @Test
    fun `nubank card purchase is parsed as expense`() {
        val outcome = engine.parse(
            bankKey = "nubank",
            text = "Compra no crédito de R$ 23,90 aprovada no Nubank",
            rules = rules,
        )

        assertEquals(ParseStatus.SUCCESS, outcome.status)
        assertEquals(2_390L, outcome.amountMinor)
        assertEquals(TransactionType.EXPENSE, outcome.type)
    }

    @Test
    fun `c6 debit keeps the card last digits`() {
        val outcome = engine.parse(
            bankKey = "c6",
            text = "Compra no débito com o cartão final 4321 no valor de R$ 89,90",
            rules = rules,
        )

        assertEquals(ParseStatus.SUCCESS, outcome.status)
        assertEquals(8_990L, outcome.amountMinor)
        assertEquals("4321", outcome.cardLastDigits)
        assertEquals(PaymentMethod.DEBIT_CARD, outcome.method)
    }

    @Test
    fun `mercado pago money received uses the bank rule`() {
        val outcome = engine.parse(
            bankKey = "mercadopago",
            text = "Você recebeu R$ 300,00 na sua conta Mercado Pago",
            rules = rules,
        )

        assertEquals(ParseStatus.SUCCESS, outcome.status)
        assertEquals(30_000L, outcome.amountMinor)
        assertEquals(TransactionType.INCOME, outcome.type)
        assertEquals(TestFixtures.mercadoPagoIn.id, outcome.matchedRuleId)
    }

    @Test
    fun `unknown format falls back to the wildcard rule`() {
        val outcome = engine.parse(
            bankKey = "nubank",
            text = "Saldo disponível R$ 12,34",
            rules = rules,
        )

        assertEquals(ParseStatus.SUCCESS, outcome.status)
        assertEquals(1_234L, outcome.amountMinor)
        assertEquals(TestFixtures.wildcard.id, outcome.matchedRuleId)
    }

    @Test
    fun `notification without any amount fails and keeps a trace`() {
        val outcome = engine.parse(
            bankKey = "nubank",
            text = "Sua fatura fechou. Confira os detalhes no app.",
            rules = rules,
        )

        assertEquals(ParseStatus.FAILED, outcome.status)
        assertNull(outcome.amountMinor)
        assertEquals(3, outcome.traces.size)
        assertTrue(outcome.traces.none { it.matched })
    }
}
