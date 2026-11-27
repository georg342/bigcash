package com.bigcash.app.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.bigcash.app.data.IngestResult
import com.bigcash.app.data.NotificationIngestRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Captures notifications from the monitored packages.
 * The raw content is always persisted before any parsing happens.
 */
@AndroidEntryPoint
class CashNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var ingestRepository: NotificationIngestRepository

    @Inject
    lateinit var appNotifier: AppNotifier

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        if (title.isNullOrBlank() && text.isNullOrBlank() && bigText.isNullOrBlank()) return

        val packageName = sbn.packageName
        val postedAt = sbn.postTime

        scope.launch {
            when (val result = ingestRepository.ingest(packageName, title, text, bigText, postedAt)) {
                is IngestResult.Recorded -> appNotifier.notifyRecorded(result.transaction)
                is IngestResult.Failed -> appNotifier.notifyParseFailure(result.raw.bankKey)
                is IngestResult.Partial -> Unit
                is IngestResult.Duplicate -> Unit
                is IngestResult.NotMonitored -> Unit
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
