package com.bigcash.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bigcash.app.R
import com.bigcash.app.data.SettingsRepository
import com.bigcash.app.ui.util.Formatters
import com.bigcash.core.model.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

/** Emits the app's own confirmation notifications. Templates are user configurable. */
@Singleton
class AppNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) {
    private var channelReady = false

    fun notifyRecorded(transaction: Transaction) {
        val settings = runBlocking { settingsRepository.current() } // called from a background scope
        if (!settings.notificationsEnabled) return
        ensureChannel(settings.appNotification.channelId, settings.appNotification.channelName, settings.appNotification.channelDescription)
        val body = render(
            template = settings.successTemplate,
            transaction = transaction,
            bankLabel = transaction.bankKey,
        )
        post(transaction.id.toInt(), body)
    }

    fun notifyParseFailure(bankKey: String) {
        val settings = runBlocking { settingsRepository.current() }
        if (!settings.notificationsEnabled) return
        ensureChannel(settings.appNotification.channelId, settings.appNotification.channelName, settings.appNotification.channelDescription)
        post(
            id = (bankKey.hashCode() and 0x7FFFFFFF),
            body = settings.failureTemplate.replace("{bank}", bankKey),
        )
    }

    private fun render(template: String, transaction: Transaction, bankLabel: String): String =
        template
            .replace("{amount}", Formatters.money(transaction.amountMinor, transaction.currency))
            .replace("{bank}", bankLabel)
            .replace("{method}", Formatters.methodLabel(transaction.method))
            .replace("{type}", transaction.type.name.lowercase())
            .replace("{counterparty}", transaction.counterparty.orEmpty())
            .replace("{card}", transaction.cardLastDigits.orEmpty())

    private fun post(id: Int, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val channelId = runBlocking { settingsRepository.current().appNotification.channelId }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun ensureChannel(channelId: String, name: String, description: String) {
        if (channelReady) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
            this.description = description
        }
        manager.createNotificationChannel(channel)
        channelReady = true
    }
}
