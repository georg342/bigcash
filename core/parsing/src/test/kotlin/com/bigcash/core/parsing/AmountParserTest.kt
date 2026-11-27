package com.bigcash.core.parsing

import com.bigcash.core.model.ParsingConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Amount parsing keeps Brazilian grouping and rejects values outside the configured range. */
class AmountParserTest {

    private val parser = AmountParser(TestFixtures.config)

    @Test
    fun `parses grouped brazilian amount`() {
        assertEquals(1_234_567L, parser.parse("R$ 12.345,67"))
    }

    @Test
    fun `parses plain decimal amount`() {
        assertEquals(1_000L, parser.parse("10,00"))
    }

    @Test
    fun `parses value without currency symbol`() {
        assertEquals(9_990L, parser.parse("99,90"))
    }

    @Test
    fun `returns null when there is no number`() {
        assertNull(parser.parse("sem valor"))
        assertNull(parser.parse(null))
    }

    @Test
    fun `rejects amounts outside the configured range`() {
        val strict = AmountParser(TestFixtures.config.copy(minAmountMinor = 500L, maxAmountMinor = 10_000L))
        assertNull(strict.parse("1,00"))
        assertNull(strict.parse("5.000,00"))
        assertEquals(600L, strict.parse("6,00"))
    }

    @Test
    fun `uses the configured decimal separator`() {
        val dotConfig = ParsingConfig(
            currencySymbols = listOf("$"),
            decimalSeparator = '.',
            thousandsSeparator = ',',
            fallbackAmountPattern = """\d{1,3}(?:,\d{3})*\.\d{2}""",
        )
        val dotParser = AmountParser(dotConfig)
        assertEquals(123_456L, dotParser.parse("$1,234.56"))
    }
}
