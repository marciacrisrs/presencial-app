package com.presencial.app.data.backup

import android.content.Context
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import com.presencial.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkInDao: CheckInDao,
    private val monthlySummaryDao: MonthlySummaryDao,
    private val settingsRepository: SettingsRepository
) {

    suspend fun exportToStream(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = createBackupJson()
            outputStream.use { it.write(json.toString(2).toByteArray()) }
        }
    }

    suspend fun exportToFile(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = createBackupJson()
            file.writeText(json.toString(2))
        }
    }

    private suspend fun createBackupJson(): JSONObject {
        val settings = settingsRepository.settings.first()
        val checkIns = checkInDao.observeAll().first()
        val summaries = monthlySummaryDao.observeAll().first()

        return JSONObject().apply {
            put("version", 1)
            put("requiredPercentage", settings.requiredPercentage)
            put("countSaturdaysAsWorkdays", settings.countSaturdaysAsWorkdays)
            put("checkIns", JSONArray().apply {
                checkIns.forEach { ci ->
                    put(JSONObject().apply {
                        put("dateEpochDay", ci.dateEpochDay)
                        put("status", ci.status)
                        put("updatedAt", ci.updatedAt)
                    })
                }
            })
            put("summaries", JSONArray().apply {
                summaries.forEach { s ->
                    put(JSONObject().apply {
                        put("yearMonthKey", s.yearMonthKey)
                        put("workdays", s.workdays)
                        put("requiredDays", s.requiredDays)
                        put("completedDays", s.completedDays)
                        put("homeOfficeDays", s.homeOfficeDays)
                        put("requiredPercentage", s.requiredPercentage)
                        put("achievedPercentage", s.achievedPercentage.toDouble())
                    })
                }
            })
        }
    }

    suspend fun importFromFile(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = JSONObject(file.readText())
            checkInDao.deleteAll()
            monthlySummaryDao.deleteAll()

            val checkInsArray = json.getJSONArray("checkIns")
            val checkInEntities = (0 until checkInsArray.length()).map { i ->
                val obj = checkInsArray.getJSONObject(i)
                CheckInEntity(
                    dateEpochDay = obj.getLong("dateEpochDay"),
                    status = obj.getString("status"),
                    updatedAt = obj.getLong("updatedAt")
                )
            }
            checkInDao.insertAll(checkInEntities)

            val summariesArray = json.getJSONArray("summaries")
            val summaryEntities = (0 until summariesArray.length()).map { i ->
                val obj = summariesArray.getJSONObject(i)
                MonthlySummaryEntity(
                    yearMonthKey = obj.getString("yearMonthKey"),
                    workdays = obj.getInt("workdays"),
                    requiredDays = obj.getInt("requiredDays"),
                    completedDays = obj.getInt("completedDays"),
                    homeOfficeDays = obj.getInt("homeOfficeDays"),
                    requiredPercentage = obj.getInt("requiredPercentage"),
                    achievedPercentage = obj.getDouble("achievedPercentage").toFloat()
                )
            }
            monthlySummaryDao.insertAll(summaryEntities)

            settingsRepository.updateRequiredPercentage(json.getInt("requiredPercentage"))
            settingsRepository.updateCountSaturdaysAsWorkdays(json.getBoolean("countSaturdaysAsWorkdays"))
        }
    }
}
