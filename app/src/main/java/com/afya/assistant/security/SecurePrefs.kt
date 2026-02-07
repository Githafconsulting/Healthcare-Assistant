package com.afya.assistant.security

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage for CHW id and tokens (no patient data).
 * Uses app-private SharedPreferences. For production pilot, consider
 * EncryptedSharedPreferences (androidx.security:security-crypto).
 */
@Singleton
class SecurePrefs @Inject constructor(
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("afya_secure", Context.MODE_PRIVATE)

    fun getChwId(): String? = prefs.getString(KEY_CHW_ID, null)
    fun setChwId(id: String) = prefs.edit().putString(KEY_CHW_ID, id).apply()

    fun getSyncToken(): String? = prefs.getString(KEY_SYNC_TOKEN, null)
    fun setSyncToken(token: String) = prefs.edit().putString(KEY_SYNC_TOKEN, token).apply()

    companion object {
        private const val KEY_CHW_ID = "chw_id"
        private const val KEY_SYNC_TOKEN = "sync_token"
    }
}
