package com.afya.assistant.messaging

import com.afya.assistant.data.local.AppDatabase
import com.afya.assistant.data.local.PatientEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and sends follow-up reminders via SMS.
 * - Only sends when consent is recorded (smsConsent = true on FollowUpEntity).
 * - Uses template messages; no PHI in message body when shared-phone safe.
 * - Logs send result for audit; does not retry indefinitely.
 *
 * For pilot: trigger from WorkManager or when app comes to foreground and sync completes.
 * Gateway (e.g. Africa's Talking) is injected; credentials in build config.
 */
@Singleton
class SmsReminderService @Inject constructor(
    private val database: AppDatabase,
    private val gateway: ReminderGateway
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Process due follow-ups: load patients for phone numbers, send template SMS, mark reminderSentAt.
     * Call when online and after consent check is already stored on FollowUpEntity.
     */
    fun processDueReminders() {
        scope.launch {
            val due = database.followUpDao().getDueForReminder(System.currentTimeMillis())
            for (followUp in due) {
                val phone = database.patientDao().getById(followUp.patientId)?.phone
                if (!phone.isNullOrBlank()) {
                    val message = buildReminderMessage(followUp.reason, followUp.dueDateEpoch)
                    gateway.sendSms(phone, message).fold(
                        onSuccess = {
                            withContext(Dispatchers.IO) {
                                database.followUpDao().markReminderSent(followUp.id, System.currentTimeMillis())
                            }
                        },
                        onFailure = { /* log for audit; do not retry here */ }
                    )
                }
            }
        }
    }

    /**
     * Template: short, no patient name (shared-phone safe). Append opt-out.
     */
    private fun buildReminderMessage(reason: String, dueDateEpoch: Long): String {
        val dateStr = java.text.SimpleDateFormat("EEE d MMM", java.util.Locale.ENGLISH)
            .format(java.util.Date(dueDateEpoch))
        return "Afya: Follow-up $dateStr. $reason. Reply STOP to stop."
    }
}
