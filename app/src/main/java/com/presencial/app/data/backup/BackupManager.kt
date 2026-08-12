package com.presencial.app.data.backup

import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import com.presencial.app.data.local.entity.WorkAddressEntity
import com.presencial.app.di.IoDispatcher
import com.presencial.app.data.preferences.PresencePolicyMapper
import com.presencial.app.domain.model.CheckInSource
import com.presencial.app.domain.model.PresencePolicy
import com.presencial.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
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
    private val checkInDao: CheckInDao,
    private val monthlySummaryDao: MonthlySummaryDao,
    private val workAddressDao: WorkAddressDao,
    private val settingsRepository: SettingsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun exportToStream(outputStream: OutputStream): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val json = createBackupJson()
            outputStream.use { it.write(json.toString(2).toByteArray()) }
        }
    }

    suspend fun exportToBytes(): Result<ByteArray> = withContext(ioDispatcher) {
        runCatching {
            createBackupJson().toString(2).toByteArray(Charsets.UTF_8)
        }
    }

    private suspend fun createBackupJson(): JSONObject {
        val settings = settingsRepository.settings.first()
        val checkIns = checkInDao.observeAll().first()
        val summaries = monthlySummaryDao.observeAll().first()
        val workAddresses = workAddressDao.getAllAddressesSync()

        return JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("requiredPercentage", settings.requiredPercentage)
            put("countSaturdaysAsWorkdays", settings.countSaturdaysAsWorkdays)
            put("presencePolicy", JSONObject(PresencePolicyMapper.toJson(settings.presencePolicy)))
            put("checkIns", JSONArray().apply {
                checkIns.forEach { ci ->
                    put(JSONObject().apply {
                        put("dateEpochDay", ci.dateEpochDay)
                        put("status", ci.status)
                        put("updatedAt", ci.updatedAt)
                        put("source", ci.source)
                        ci.workAddressId?.let { put("workAddressId", it) }
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
            put("workAddresses", JSONArray().apply {
                workAddresses.forEach { wa ->
                    put(JSONObject().apply {
                        put("id", wa.id)
                        put("name", wa.name)
                        put("addressText", wa.addressText)
                        put("latitude", wa.latitude)
                        put("longitude", wa.longitude)
                        put("radius", wa.radius.toDouble())
                        put("isActive", wa.isActive)
                        wa.stateCode?.let { put("stateCode", it) }
                        wa.cityName?.let { put("cityName", it) }
                    })
                }
            })
        }
    }

    suspend fun importFromFile(file: File): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            importFromJson(JSONObject(file.readText()))
        }
    }

    suspend fun importFromBytes(bytes: ByteArray): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            importFromJson(JSONObject(bytes.toString(Charsets.UTF_8)))
        }
    }

    private suspend fun importFromJson(json: JSONObject) {
        checkInDao.deleteAll()
        monthlySummaryDao.deleteAll()
        workAddressDao.deleteAll()
        restoreCheckIns(json.getJSONArray("checkIns"))
        restoreSummaries(json.getJSONArray("summaries"))
        restoreWorkAddresses(json)
        restoreSettings(json)
    }

    private suspend fun restoreCheckIns(checkInsArray: org.json.JSONArray) {
        val checkInEntities = (0 until checkInsArray.length()).map { i ->
            val obj = checkInsArray.getJSONObject(i)
            CheckInEntity(
                dateEpochDay = obj.getLong("dateEpochDay"),
                status = obj.getString("status"),
                updatedAt = obj.getLong("updatedAt"),
                source = obj.optString("source", CheckInSource.MANUAL),
                workAddressId = if (obj.has("workAddressId") && !obj.isNull("workAddressId")) {
                    obj.getLong("workAddressId")
                } else {
                    null
                }
            )
        }
        checkInDao.insertAll(checkInEntities)
    }

    private suspend fun restoreSummaries(summariesArray: org.json.JSONArray) {
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
    }

    private suspend fun restoreWorkAddresses(json: JSONObject) {
        if (!json.has("workAddresses")) return
        val workAddressesArray = json.getJSONArray("workAddresses")
        val workAddressEntities = (0 until workAddressesArray.length()).map { i ->
            val obj = workAddressesArray.getJSONObject(i)
            WorkAddressEntity(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                addressText = obj.optString("addressText", ""),
                latitude = obj.getDouble("latitude"),
                longitude = obj.getDouble("longitude"),
                radius = obj.getDouble("radius").toFloat(),
                isActive = obj.optBoolean("isActive", true),
                stateCode = obj.optString("stateCode").takeIf { it.isNotBlank() },
                cityName = obj.optString("cityName").takeIf { it.isNotBlank() }
            )
        }
        workAddressDao.insertAll(workAddressEntities)
    }

    private suspend fun restoreSettings(json: JSONObject) {
        settingsRepository.updateRequiredPercentage(json.getInt("requiredPercentage"))
        settingsRepository.updateCountSaturdaysAsWorkdays(json.getBoolean("countSaturdaysAsWorkdays"))
        if (json.has("presencePolicy")) {
            val policyJson = json.getJSONObject("presencePolicy")
            settingsRepository.updatePresencePolicy(
                PresencePolicyMapper.fromJson(policyJson.toString(), json.getInt("requiredPercentage"))
            )
        }
    }

    companion object {
        const val BACKUP_VERSION = 3
    }
}
