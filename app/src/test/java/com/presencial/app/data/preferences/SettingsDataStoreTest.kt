package com.presencial.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SettingsDataStoreTest {

    private val context: Context = mockk()
    private val dataStore: DataStore<Preferences> = mockk()
    private val sharedPrefs: SharedPreferences = mockk()
    private val sharedPrefsEditor: SharedPreferences.Editor = mockk(relaxed = true)
    
    private lateinit var settingsDataStore: SettingsDataStore

    @BeforeEach
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns sharedPrefsEditor
        settingsDataStore = SettingsDataStore(context, dataStore)
    }

    @Test
    fun `when settings is observed, then return domain object from datastore`() = runTest {
        // Arrange
        val prefs: Preferences = mockk()
        every { prefs[any<Preferences.Key<Int>>()] } returns 60
        every { prefs[any<Preferences.Key<Boolean>>()] } returns true
        every { dataStore.data } returns flowOf(prefs)

        // Act & Assert
        settingsDataStore.settings.test {
            val result = awaitItem()
            assertEquals(60, result.requiredPercentage)
            assertEquals(true, result.countSaturdaysAsWorkdays)
            awaitComplete()
        }
    }

    @Test
    fun `when updateRequiredPercentage with valid value, then datastore is updated`() = runTest {
        // Arrange
        val percentage = 60
        coEvery { dataStore.edit(any()) } returns mockk()

        // Act
        settingsDataStore.updateRequiredPercentage(percentage)

        // Assert
        coVerify { dataStore.edit(any()) }
        coVerify { sharedPrefsEditor.putInt("required_percentage", percentage) }
    }

    @Test
    fun `when updateRequiredPercentage with invalid value, then default value is used`() = runTest {
        // Arrange
        val percentage = 50 // Invalid, allowed are 20, 40, 60
        coEvery { dataStore.edit(any()) } returns mockk()

        // Act
        settingsDataStore.updateRequiredPercentage(percentage)

        // Assert
        coVerify { dataStore.edit(any()) }
        coVerify { sharedPrefsEditor.putInt("required_percentage", 40) }
    }

    @Test
    fun `when updateCountSaturdaysAsWorkdays, then datastore is updated`() = runTest {
        // Arrange
        val count = true
        coEvery { dataStore.edit(any()) } returns mockk()

        // Act
        settingsDataStore.updateCountSaturdaysAsWorkdays(count)

        // Assert
        coVerify { dataStore.edit(any()) }
        coVerify { sharedPrefsEditor.putBoolean("count_saturdays_as_workdays", count) }
    }
}
