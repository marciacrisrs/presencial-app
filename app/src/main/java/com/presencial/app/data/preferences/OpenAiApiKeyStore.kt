package com.presencial.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiApiKeyStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun read(): String = encryptedPrefs.getString(KEY_OPENAI_API, "").orEmpty()

    fun save(apiKey: String) {
        encryptedPrefs.edit { putString(KEY_OPENAI_API, apiKey.trim()) }
    }

    fun clear() {
        encryptedPrefs.edit { remove(KEY_OPENAI_API) }
    }

    companion object {
        private const val PREFS_FILE = "presencial_secure_prefs"
        private const val KEY_OPENAI_API = "openai_api_key"
    }
}
