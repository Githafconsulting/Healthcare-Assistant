package com.afya.assistant.messaging

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder SMS gateway for pilot when no real gateway is configured.
 * Logs the message; does not send. Replace with real implementation (e.g. Africa's Talking API client)
 * and provide via DI. Never log full message content in production if it contains PHI.
 */
@Singleton
class PlaceholderReminderGateway @Inject constructor() : ReminderGateway {

    override suspend fun sendSms(phone: String, message: String): Result<Unit> {
        Log.w(TAG, "PlaceholderReminderGateway: would send to ${phone.take(4)}*** length=${message.length}")
        return Result.success(Unit)
    }

    companion object {
        private const val TAG = "ReminderGateway"
    }
}
