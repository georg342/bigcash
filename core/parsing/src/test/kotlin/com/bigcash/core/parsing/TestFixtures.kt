package com.bigcash.core.parsing

import com.bigcash.core.model.ParsingConfig
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.RuleType
import com.bigcash.core.model.TransactionType

/** Rule rows and config used by the engine tests, mirroring what the app seeds into Room. */
object TestFixtures {

    val config = ParsingConfig(
        currencySymbols = listOf("R$", "BRL"),
        decimalSeparator = ',',
        thousandsSeparator = '.',
        incomeKeywords = listOf("recebeu", "recebido", "recebida", "entrada", "creditado"),
        expenseKeywords = listOf("pagou", "pagamento", "compra", "enviou", "enviado", "débito", "debito"),
        methodKeywords = mapOf(
            PaymentMethod.PIX.name to listOf("pix"),
            PaymentMethod.CREDIT_CARD.name to listOf("cartão de crédito", "credito", "crédito"),
            PaymentMethod.DEBIT_CARD.name to listOf("débito", "debito"),
            PaymentMethod.BOLETO.name to listOf("boleto"),
        ),
        fallbackAmountPattern = """\d{1,3}(?:\.\d{3})*,\d{2}""",
        defaultCurrency = "BRL",
        dedupeWindowMillis = 60_000L,
    )

    val nubankPixIn = ParsingRule(
        id = 1,
        name = "Nubank Pix received",
        bankKey = "nubank",
        type = RuleType.REGEX,
        pattern = """(?i)(recebeu|recebido)[^\d]{0,40}(\d{1,3}(?:\.\d{3})*,\d{2})""",
        priority = 10,
        amountGroup = 2,
        typeValue = TransactionType.INCOME.name,
        methodValue = PaymentMethod.PIX.name,
    )

    val nubankCard = ParsingRule(
        id = 2,
        name = "Nubank card purchase",
        bankKey = "nubank",
        type = RuleType.REGEX,
        pattern = """(?i)compra[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
        priority = 30,
        amountGroup = 1,
        typeValue = TransactionType.EXPENSE.name,
        methodValue = PaymentMethod.CREDIT_CARD.name,
    )

    val c6Debit = ParsingRule(
        id = 3,
        name = "C6 debit card purchase",
        bankKey = "c6",
        type = RuleType.REGEX,
        pattern = """(?i)d[ée]bito[^\d]{0,40}final\s*(\d{4})[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
        priority = 20,
        amountGroup = 2,
        cardGroup = 1,
        typeValue = TransactionType.EXPENSE.name,
        methodValue = PaymentMethod.DEBIT_CARD.name,
    )

    val mercadoPagoIn = ParsingRule(
        id = 4,
        name = "Mercado Pago money received",
        bankKey = "mercadopago",
        type = RuleType.REGEX,
        pattern = """(?i)(recebeu|recebido|entrada)[^\d]{0,40}(\d{1,3}(?:\.\d{3})*,\d{2})""",
        priority = 10,
        amountGroup = 2,
        typeValue = TransactionType.INCOME.name,
        methodValue = PaymentMethod.PIX.name,
    )

    val wildcard = ParsingRule(
        id = 99,
        name = "Any bank generic amount",
        bankKey = "*",
        type = RuleType.REGEX,
        pattern = """(\d{1,3}(?:\.\d{3})*,\d{2})""",
        priority = 200,
        amountGroup = 1,
    )

    val rules = listOf(nubankPixIn, nubankCard, c6Debit, mercadoPagoIn, wildcard)

    val engine = ParsingEngine(config)
}
