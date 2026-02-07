package com.afya.assistant.messaging

/**
 * Abstraction for sending SMS reminders (e.g. via Africa's Talking, Twilio, or national gateway).
 * Implementation is injected; credentials and base URL come from config, not source.
 * Consent must be checked by caller before send.
 */
interface ReminderGateway {

    /**
     * Send a single SMS. Phone in E.164 or national format.
     * Message must be from a template; no free-form clinical content.
     *
     * @return true if accepted for delivery (or delivered); false on failure
     */
    suspend fun sendSms(phone: String, message: String): Result<Unit>
}
