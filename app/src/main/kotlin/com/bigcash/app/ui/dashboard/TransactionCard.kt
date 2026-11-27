package com.bigcash.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigcash.app.ui.components.GlassSurface
import com.bigcash.app.ui.components.GlassTag
import com.bigcash.app.ui.theme.GlassColors
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.Transaction
import com.bigcash.core.model.TransactionType

/** One transaction rendered as a glass card. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
) {
    val accent = when (transaction.type) {
        TransactionType.INCOME -> GlassColors.Mint
        TransactionType.EXPENSE -> GlassColors.Pink
        TransactionType.UNKNOWN -> GlassColors.TextSecondaryDark
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = Formatters.signedMoney(transaction.amountMinor, transaction.type, transaction.currency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = Formatters.bankLabel(transaction.bankKey),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = transaction.description
                ?: transaction.counterparty
                ?: Formatters.methodLabel(transaction.method),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp),
        )

        Text(
            text = buildString {
                append(Formatters.dateTime(transaction.occurredAt))
                transaction.cardLastDigits?.let { append("  \u2022  card ").append(it) }
                if (transaction.isManuallyEdited) append("  \u2022  edited")
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GlassTag(text = Formatters.methodLabel(transaction.method))
            GlassTag(text = Formatters.statusLabel(transaction.parseStatus))
            if (transaction.possibleDuplicate) {
                GlassTag(text = "Possible duplicate")
            }
            transaction.tags.forEach { tag -> GlassTag(text = "#$tag") }
        }
    }
}

/** Compact chip row to filter by parse status. */
@Composable
fun StatusChip(status: ParseStatus, selected: Boolean, onClick: () -> Unit) {
    GlassTag(
        text = Formatters.statusLabel(status),
        selected = selected,
        onClick = onClick,
    )
}
