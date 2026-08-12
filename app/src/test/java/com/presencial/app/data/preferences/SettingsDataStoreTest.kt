package com.presencial.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import com.presencial.app.domain.model.PresencePolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("WildcardImport")
class SettingsDataStoreTest {

    private val context: Context = mockk()
    private val dataStore: DataStore<Preferences> = mockk()
    private val sharedPrefs: SharedPreferences = mockk()
    private val sharedPrefsEditor: SharedPreferences.Editor = mockk(relaxed = true)
    private val openAiApiKeyStore: OpenAiApiKeyStore = mockk(relaxed = true)
    
    private val dataStoreFlow = MutableStateFlow(preferencesOf())
    private lateinit var settingsDataStore: SettingsDataStore

    @BeforeEach
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns sharedPrefsEditor
        every { dataStore.data } returns dataStoreFlow
        every { openAiApiKeyStore.read() } returns ""
        
        settingsDataStore = SettingsDataStore(context, dataStore, openAiApiKeyStore)
    }

    @Test
    fun `when settings is observed, then return domain object from datastore`() = runTest {
        // Arrange
        val prefs = preferencesOf(
            intPreferencesKey("required_percentage") to 60,
            booleanPreferencesKey("count_saturdays_as_workdays") to true
        )
        dataStoreFlow.value = prefs

        // Act & Assert
        settingsDataStore.settings.test {
            val result = awaitItem()
            assertEquals(60, result.requiredPercentage)
            assertEquals(true, result.countSaturdaysAsWorkdays)
        }
    }

    @Test
    fun `when updateRequiredPercentage with valid value, then datastore is updated`() = runTest {
        // Arrange
        val percentage = 60
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        // Act
        settingsDataStore.updateRequiredPercentage(percentage)

        // Assert
        coVerify { dataStore.updateData(any()) }
        coVerify { sharedPrefsEditor.putInt("required_percentage", percentage) }
    }

    @Test
    fun `when updateRequiredPercentage with out of range value, then value is coerced`() = runTest {
        val percentage = 150
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        settingsDataStore.updateRequiredPercentage(percentage)

        coVerify { sharedPrefsEditor.putInt("required_percentage", 100) }
    }

    @Test
    fun `when updatePresencePolicy, then persist policy json`() = runTest {
        val policy = PresencePolicy(
            companyName = "Acme",
            freePercentageEnabled = true,
            freePercentage = 55
        )
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        settingsDataStore.updatePresencePolicy(policy)

        coVerify { dataStore.updateData(any()) }
        coVerify { sharedPrefsEditor.putInt("required_percentage", 55) }
    }

    @Test
    fun `when updateCountSaturdaysAsWorkdays, then datastore is updated`() = runTest {
        // Arrange
        val count = true
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        // Act
        settingsDataStore.updateCountSaturdaysAsWorkdays(count)

        // Assert
        coVerify { dataStore.updateData(any()) }
        coVerify { sharedPrefsEditor.putBoolean("count_saturdays_as_workdays", count) }
    }
}
