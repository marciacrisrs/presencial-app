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

    private val dataStoreFlow = MutableStateFlow(preferencesOf())
    private lateinit var settingsDataStore: SettingsDataStore

    @BeforeEach
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns sharedPrefsEditor
        every { dataStore.data } returns dataStoreFlow
        coEvery { dataStore.updateData(any()) } coAnswers {
            @Suppress("UNCHECKED_CAST")
            val transform = args[0] as suspend (Preferences) -> Preferences
            transform(preferencesOf())
        }

        settingsDataStore = SettingsDataStore(context, dataStore)
    }

    @Test
    fun `when settings is observed, then return domain object from datastore`() = runTest {
        val prefs = preferencesOf(
            intPreferencesKey("required_percentage") to 60,
            booleanPreferencesKey("count_saturdays_as_workdays") to true
        )
        dataStoreFlow.value = prefs

        settingsDataStore.settings.test {
            val result = awaitItem()
            assertEquals(60, result.requiredPercentage)
            assertEquals(true, result.countSaturdaysAsWorkdays)
        }
    }

    @Test
    fun `when onboarding keys exist, then map completed and step`() = runTest {
        val prefs = preferencesOf(
            booleanPreferencesKey("onboarding_completed") to true,
            intPreferencesKey("onboarding_step") to 2
        )
        dataStoreFlow.value = prefs

        settingsDataStore.settings.test {
            val result = awaitItem()
            assertEquals(true, result.onboardingCompleted)
            assertEquals(2, result.onboardingStep)
        }
    }

    @Test
    fun `when updateRequiredPercentage with valid value, then datastore is updated`() = runTest {
        val percentage = 60
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        settingsDataStore.updateRequiredPercentage(percentage)

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
    fun `when updatePresencePolicy with preset chips, then persist selected percentage`() = runTest {
        val policy = PresencePolicy(
            freePercentageEnabled = false,
            freePercentage = 60
        )
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        settingsDataStore.updatePresencePolicy(policy)

        coVerify { sharedPrefsEditor.putInt("required_percentage", 60) }
    }

    @Test
    fun `when updateCountSaturdaysAsWorkdays, then datastore is updated`() = runTest {
        val count = true
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        settingsDataStore.updateCountSaturdaysAsWorkdays(count)

        coVerify { dataStore.updateData(any()) }
        coVerify { sharedPrefsEditor.putBoolean("count_saturdays_as_workdays", count) }
    }

    @Test
    fun `when updateOnboardingStep is out of range, then value is coerced`() = runTest {
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        settingsDataStore.updateOnboardingStep(99)

        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `when completeOnboarding, then persist completed and last step`() = runTest {
        mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
        val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(mutablePrefs)
            mutablePrefs
        }

        settingsDataStore.completeOnboarding()

        coVerify { dataStore.updateData(any()) }
    }
}
