package com.bigcash.app.ui.util

import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.TransactionType
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

/** Presentation helpers shared by every screen. */
object Formatters {

    private val locale: Locale = Locale("pt", "BR")
    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", locale)

    fun money(minor: Long?, currency: String = "BRL"): String {
        if (minor == null) return "\u2014"
        val format = NumberFormat.getCurrencyInstance(locale)
        runCatching { format.currency = Currency.getInstance(currency) }
        return format.format(minor / 100.0)
    }

    fun signedMoney(minor: Long?, type: TransactionType, currency: String = "BRL"): String {
        val prefix = when (type) {
            TransactionType.INCOME -> "+"
            TransactionType.EXPENSE -> "-"
            TransactionType.UNKNOWN -> ""
        }
        return "$prefix${money(minor, currency)}"
    }

    fun dateTime(epochMillis: Long): String = dateTimeFormatter.format(
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()),
    )

    fun day(epochMillis: Long): String = DateTimeFormatter.ofPattern("EEEE, dd MMM", locale)
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

    fun monthLabel(year: Int, month: Int): String =
        DateTimeFormatter.ofPattern("MMMM yyyy", locale)
            .format(Instant.now().atZone(ZoneId.systemDefault()).withYear(year).withMonth(month))

    fun methodLabel(method: PaymentMethod): String = when (method) {
        PaymentMethod.PIX -> "Pix"
        PaymentMethod.CREDIT_CARD -> "Credit card"
        PaymentMethod.DEBIT_CARD -> "Debit card"
        PaymentMethod.BOLETO -> "Boleto"
        PaymentMethod.TRANSFER -> "Transfer"
        PaymentMethod.CASH -> "Cash"
        PaymentMethod.OTHER -> "Other"
        PaymentMethod.UNKNOWN -> "Unknown"
    }

    fun typeLabel(type: TransactionType): String = when (type) {
        TransactionType.INCOME -> "Income"
        TransactionType.EXPENSE -> "Expense"
        TransactionType.UNKNOWN -> "Unknown"
    }

    fun statusLabel(status: ParseStatus): String = when (status) {
        ParseStatus.SUCCESS -> "Parsed"
        ParseStatus.PARTIAL -> "Partial"
        ParseStatus.FAILED -> "Failed"
    }

    fun bankLabel(bankKey: String): String = when (bankKey) {
        "nubank" -> "Nubank"
        "c6" -> "C6 Bank"
        "mercadopago" -> "Mercado Pago"
        "*" -> "Any bank"
        else -> bankKey.replaceFirstChar { it.uppercase() }
    }

    fun amountToInput(minor: Long?): String =
        if (minor == null) "" else String.format(locale, "%.2f", minor / 100.0).replace('.', ',')

    fun inputToMinor(value: String): Long? {
        val normalized = value.trim().replace(".", "").replace(',', '.')
        val parsed = normalized.toDoubleOrNull() ?: return null
        return Math.round(parsed * 100.0)
    }
}
