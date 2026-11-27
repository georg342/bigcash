package com.bigcash.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** CSV/JSON export plus the share sheet plumbing. */
@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ingestRepository: NotificationIngestRepository,
) {

    suspend fun exportCsv(): File = withContext(Dispatchers.IO) {
        val rows = ingestRepository.allTransactions()
        val builder = StringBuilder()
        builder.appendLine(
            "id,bank,package,type,method,amount_minor,currency,card_last_digits," +
                "counterparty,description,occurred_at,parse_status,tags,possible_duplicate",
        )
        rows.forEach { row ->
            builder.appendLine(
                listOf(
                    row.id,
                    row.bankKey,
                    row.packageName,
                    row.type.name,
                    row.method.name,
                    row.amountMinor ?: "",
                    row.currency,
                    row.cardLastDigits.orEmpty(),
                    escape(row.counterparty.orEmpty()),
                    escape(row.description.orEmpty()),
                    row.occurredAt,
                    row.parseStatus.name,
                    escape(row.tags.joinToString(" ")),
                    row.possibleDuplicate,
                ).joinToString(","),
            )
        }
        writeFile("bigcash-transactions.csv", builder.toString())
    }

    suspend fun exportJson(): File = withContext(Dispatchers.IO) {
        val array = JSONArray()
        ingestRepository.allTransactions().forEach { row ->
            array.put(
                JSONObject().apply {
                    put("id", row.id)
                    put("bankKey", row.bankKey)
                    put("packageName", row.packageName)
                    put("type", row.type.name)
                    put("method", row.method.name)
                    put("amountMinor", row.amountMinor ?: JSONObject.NULL)
                    put("currency", row.currency)
                    put("cardLastDigits", row.cardLastDigits ?: JSONObject.NULL)
                    put("counterparty", row.counterparty ?: JSONObject.NULL)
                    put("description", row.description ?: JSONObject.NULL)
                    put("occurredAt", row.occurredAt)
                    put("parseStatus", row.parseStatus.name)
                    put("tags", JSONArray(row.tags))
                    put("possibleDuplicate", row.possibleDuplicate)
                },
            )
        }
        writeFile("bigcash-backup.json", array.toString(2))
    }

    fun shareIntent(file: File, mimeType: String): Intent {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share BigCash export",
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun exportedFiles(): List<File> {
        val dir = File(context.cacheDir, "exports")
        return dir.listFiles()?.sortedByDescending { it.lastModified() }.orEmpty()
    }

    private fun writeFile(name: String, content: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, name)
        file.writeText(content)
        return file
    }

    private fun escape(value: String): String = "\"" + value.replace("\"", "'") + "\""
}
