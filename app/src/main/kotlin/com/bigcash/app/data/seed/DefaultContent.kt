package com.bigcash.app.data.seed

import com.bigcash.app.data.model.AppSettings
import com.bigcash.core.model.AppNotificationConfig
import com.bigcash.core.model.MonitoredPackage
import com.bigcash.core.model.ParsingConfig
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.RuleType
import com.bigcash.core.model.TransactionType

/**
 * First-run data. Nothing here is compiled into the engine: every row is editable or deletable
 * from the calibration panel, so users can adapt the defaults to whatever their bank sends.
 */
object DefaultContent {

    const val BANK_NUBANK = "nubank"
    const val BANK_C6 = "c6"
    const val BANK_MERCADO_PAGO = "mercadopago"

    fun packages(): List<MonitoredPackage> = listOf(
        MonitoredPackage(
            packageName = "com.nu.production",
            bankKey = BANK_NUBANK,
            displayName = "Nubank",
            isBuiltIn = true,
        ),
        MonitoredPackage(
            packageName = "com.c6bank.app",
            bankKey = BANK_C6,
            displayName = "C6 Bank",
            isBuiltIn = true,
        ),
        MonitoredPackage(
            packageName = "com.mercadopago.wallet",
            bankKey = BANK_MERCADO_PAGO,
            displayName = "Mercado Pago",
            isBuiltIn = true,
        ),
    )

    fun settings(): AppSettings = AppSettings(
        notificationsEnabled = true,
        onboardingCompleted = false,
        successTemplate = "\u2705 {amount} \u2014 {bank} {method} recorded",
        failureTemplate = "\u26A0\uFE0F Could not parse a {bank} notification",
        parsing = ParsingConfig(
            currencySymbols = listOf("R$", "BRL", "US$"),
            decimalSeparator = ',',
            thousandsSeparator = '.',
            incomeKeywords = listOf(
                "recebeu",
                "recebido",
                "recebida",
                "entrada",
                "creditado",
                "crédito de",
                "depósito",
                "deposito",
                "pix recebido",
                "transferência recebida",
            ),
            expenseKeywords = listOf(
                "pagou",
                "pagamento",
                "compra",
                "comprou",
                "enviou",
                "enviado",
                "débito",
                "debito",
                "debitado",
                "saque",
                "boleto",
                "transferência enviada",
            ),
            methodKeywords = mapOf(
                PaymentMethod.PIX.name to listOf("pix"),
                PaymentMethod.CREDIT_CARD.name to listOf("cartão de crédito", "credito", "crédito"),
                PaymentMethod.DEBIT_CARD.name to listOf("cartão de débito", "débito", "debito"),
                PaymentMethod.BOLETO.name to listOf("boleto"),
                PaymentMethod.TRANSFER.name to listOf("ted", "transferência", "transferencia"),
                PaymentMethod.CASH.name to listOf("saque", "dinheiro"),
            ),
            fallbackAmountPattern = """\d{1,3}(?:[.\s]\d{3})*,\d{2}|\d+[.,]\d{2}""",
            defaultCurrency = "BRL",
            dedupeWindowMillis = 60_000L,
            minAmountMinor = 1L,
            maxAmountMinor = 100_000_000L,
        ),
        appNotification = AppNotificationConfig(
            enabled = true,
            successTemplate = "\u2705 {amount} \u2014 {bank} {method} recorded",
            failureTemplate = "\u26A0\uFE0F Could not parse a {bank} notification",
            channelId = "bigcash_transactions",
            channelName = "Recorded transactions",
            channelDescription = "Confirmation for every transaction BigCash records",
        ),
    )

    fun rules(): List<ParsingRule> = buildList {
        // --- Nubank -------------------------------------------------------------------------
        add(
            ParsingRule(
                name = "Nubank Pix received",
                bankKey = BANK_NUBANK,
                type = RuleType.REGEX,
                pattern = """(?i)(recebeu|recebido|recebida)[^\d]{0,40}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 10,
                description = "Matches Pix credits such as 'Voc\u00ea recebeu um Pix de R$ 50,00'",
                amountGroup = 2,
                typeValue = TransactionType.INCOME.name,
                methodValue = PaymentMethod.PIX.name,
            ),
        )
        add(
            ParsingRule(
                name = "Nubank Pix sent",
                bankKey = BANK_NUBANK,
                type = RuleType.REGEX,
                pattern = """(?i)(enviou|enviado|pagou)[^\d]{0,40}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 20,
                description = "Matches outgoing Pix and payments",
                amountGroup = 2,
                typeValue = TransactionType.EXPENSE.name,
                methodValue = PaymentMethod.PIX.name,
            ),
        )
        add(
            ParsingRule(
                name = "Nubank card purchase",
                bankKey = BANK_NUBANK,
                type = RuleType.REGEX,
                pattern = """(?i)compra[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 30,
                description = "Matches card purchases including installments",
                amountGroup = 1,
                typeValue = TransactionType.EXPENSE.name,
                methodValue = PaymentMethod.CREDIT_CARD.name,
            ),
        )
        add(
            ParsingRule(
                name = "Nubank card last digits",
                bankKey = BANK_NUBANK,
                type = RuleType.REGEX,
                pattern = """(?i)final\s*(\d{4})""",
                priority = 35,
                description = "Captures the card last digits when the notification mentions them",
                cardGroup = 1,
            ),
        )
        add(fallbackRule(BANK_NUBANK, 90))

        // --- C6 Bank ------------------------------------------------------------------------
        add(
            ParsingRule(
                name = "C6 Pix received",
                bankKey = BANK_C6,
                type = RuleType.REGEX,
                pattern = """(?i)(recebeu|pix recebido)[^\d]{0,40}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 10,
                description = "Incoming Pix on the C6 account",
                amountGroup = 2,
                typeValue = TransactionType.INCOME.name,
                methodValue = PaymentMethod.PIX.name,
            ),
        )
        add(
            ParsingRule(
                name = "C6 debit card purchase",
                bankKey = BANK_C6,
                type = RuleType.REGEX,
                pattern = """(?i)d[ée]bito[^\d]{0,40}final\s*(\d{4})[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 20,
                description = "Debit card purchases that mention the card last digits",
                amountGroup = 2,
                cardGroup = 1,
                typeValue = TransactionType.EXPENSE.name,
                methodValue = PaymentMethod.DEBIT_CARD.name,
            ),
        )
        add(
            ParsingRule(
                name = "C6 credit card purchase",
                bankKey = BANK_C6,
                type = RuleType.REGEX,
                pattern = """(?i)(compra|cr[ée]dito)[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 30,
                description = "Credit card purchases",
                amountGroup = 2,
                typeValue = TransactionType.EXPENSE.name,
                methodValue = PaymentMethod.CREDIT_CARD.name,
            ),
        )
        add(
            ParsingRule(
                name = "C6 boleto or payment",
                bankKey = BANK_C6,
                type = RuleType.REGEX,
                pattern = """(?i)(boleto|pagamento|pagou)[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 40,
                description = "Boleto payments and generic payments",
                amountGroup = 2,
                typeValue = TransactionType.EXPENSE.name,
                methodValue = PaymentMethod.BOLETO.name,
            ),
        )
        add(fallbackRule(BANK_C6, 90))

        // --- Mercado Pago -------------------------------------------------------------------
        add(
            ParsingRule(
                name = "Mercado Pago money received",
                bankKey = BANK_MERCADO_PAGO,
                type = RuleType.REGEX,
                pattern = """(?i)(recebeu|recebido|recebida|entrada)[^\d]{0,40}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 10,
                description = "Incoming Pix, transfers and cashback",
                amountGroup = 2,
                typeValue = TransactionType.INCOME.name,
                methodValue = PaymentMethod.PIX.name,
            ),
        )
        add(
            ParsingRule(
                name = "Mercado Pago Pix transfer",
                bankKey = BANK_MERCADO_PAGO,
                type = RuleType.REGEX,
                pattern = """(?i)pix[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 20,
                description = "Pix amounts without a clear direction, direction comes from keywords",
                amountGroup = 1,
                methodValue = PaymentMethod.PIX.name,
            ),
        )
        add(
            ParsingRule(
                name = "Mercado Pago payment",
                bankKey = BANK_MERCADO_PAGO,
                type = RuleType.REGEX,
                pattern = """(?i)(pagou|pagamento|compra|enviou)[^\d]{0,60}(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 30,
                description = "Card purchases and outgoing payments",
                amountGroup = 2,
                typeValue = TransactionType.EXPENSE.name,
                methodValue = PaymentMethod.OTHER.name,
            ),
        )
        add(fallbackRule(BANK_MERCADO_PAGO, 90))

        // --- Cross-bank ---------------------------------------------------------------------
        add(
            ParsingRule(
                name = "Any bank generic amount",
                bankKey = "*",
                type = RuleType.REGEX,
                pattern = """(\d{1,3}(?:\.\d{3})*,\d{2})""",
                priority = 200,
                description = "Last resort: captures the first Brazilian formatted amount",
                amountGroup = 1,
            ),
        )
    }

    private fun fallbackRule(bankKey: String, priority: Int) = ParsingRule(
        name = "$bankKey fallback amount",
        bankKey = bankKey,
        type = RuleType.REGEX,
        pattern = """(\d{1,3}(?:\.\d{3})*,\d{2})""",
        priority = priority,
        description = "Fallback rule: first Brazilian formatted amount in the notification",
        amountGroup = 1,
    )
}
