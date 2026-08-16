package com.presencial.app.data.backup

import com.presencial.app.data.local.dao.AbsenceDao
import com.presencial.app.data.local.dao.BackupDao
import com.presencial.app.data.local.dao.CheckInDao
import com.presencial.app.data.local.dao.MonthlySummaryDao
import com.presencial.app.data.local.dao.WorkAddressDao
import com.presencial.app.data.local.entity.AbsenceEntity
import com.presencial.app.data.local.entity.CheckInEntity
import com.presencial.app.data.local.entity.MonthlySummaryEntity
import com.presencial.app.data.local.entity.WorkAddressEntity
import com.presencial.app.data.preferences.PresencePolicyMapper
import com.presencial.app.di.IoDispatcher
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
    private val absenceDao: AbsenceDao,
    private val backupDao: BackupDao,
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
        val absences = absenceDao.getAllAbsences().first()

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
            put("workAddresses", buildWorkAddressesArray(workAddresses))
            put("absences", buildAbsencesArray(absences))
        }
    }

    private fun buildWorkAddressesArray(workAddresses: List<WorkAddressEntity>): JSONArray =
        JSONArray().apply {
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
        }

    private fun buildAbsencesArray(absences: List<AbsenceEntity>): JSONArray =
        JSONArray().apply {
            absences.forEach { absence ->
                put(JSONObject().apply {
                    put("id", absence.id)
                    put("type", absence.type)
                    put("startDateEpochDay", absence.startDateEpochDay)
                    put("endDateEpochDay", absence.endDateEpochDay)
                    put("isFullDay", absence.isFullDay)
                    put("hours", absence.hours.toDouble())
                    absence.notes?.let { put("notes", it) }
                    put("isCounted", absence.isCounted)
                })
            }
        }

    suspend fun importFromFile(file: File): Result<Unit> = withContext(ioDispatcher) {
        runCatching { restoreFromJson(JSONObject(file.readText())) }
    }

    suspend fun importFromBytes(bytes: ByteArray): Result<Unit> = withContext(ioDispatcher) {
        runCatching { restoreFromJson(JSONObject(bytes.toString(Charsets.UTF_8))) }
    }

    private suspend fun restoreFromJson(json: JSONObject) {
        val data = parseAndValidateRestoreData(json)
        val previousSettings = settingsRepository.settings.first()

        settingsRepository.restoreBackupSettings(
            requiredPercentage = data.requiredPercentage,
            countSaturdaysAsWorkdays = data.countSaturdaysAsWorkdays,
            presencePolicy = data.presencePolicy
        )

        val restoreResult = runCatching {
            backupDao.restoreAll(
                checkIns = data.checkIns,
                summaries = data.summaries,
                workAddresses = data.workAddresses,
                absences = data.absences
            )
        }

        restoreResult.onFailure { restoreFailure ->
            val rollbackResult = runCatching {
                settingsRepository.restoreBackupSettings(
                    requiredPercentage = previousSettings.requiredPercentage,
                    countSaturdaysAsWorkdays = previousSettings.countSaturdaysAsWorkdays,
                    presencePolicy = previousSettings.presencePolicy
                )
            }
            rollbackResult.onFailure { rollbackFailure ->
                restoreFailure.addSuppressed(rollbackFailure)
            }
        }

        restoreResult.getOrThrow()
    }

    private fun parseAndValidateRestoreData(json: JSONObject): RestoreData {
        validateBackupVersion(json)

        val requiredPercentage = json.getInt("requiredPercentage").also {
            require(it in 0..100) { "Percentual obrigatório inválido" }
        }
        val countSaturdaysAsWorkdays = json.getBoolean("countSaturdaysAsWorkdays")
        val presencePolicy = json.optJSONObject("presencePolicy")?.let {
            PresencePolicyMapper.fromJson(it.toString(), requiredPercentage)
        }

        val checkIns = parseCheckIns(json.getJSONArray("checkIns"))
        val summaries = parseSummaries(json.getJSONArray("summaries"))
        val workAddresses = parseWorkAddresses(json)
        val absences = parseAbsences(json)

        return RestoreData(
            requiredPercentage = requiredPercentage,
            countSaturdaysAsWorkdays = countSaturdaysAsWorkdays,
            presencePolicy = presencePolicy,
            checkIns = checkIns,
            summaries = summaries,
            workAddresses = workAddresses,
            absences = absences
        )
    }

    private fun validateBackupVersion(json: JSONObject) {
        val version = json.optInt("version", UNSUPPORTED_VERSION)
        require(version in SUPPORTED_VERSIONS) {
            "Versão de backup não suportada"
        }
    }

    private fun parseCheckIns(checkInsArray: JSONArray): List<CheckInEntity> =
        (0 until checkInsArray.length()).map { i ->
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

    private fun parseSummaries(summariesArray: JSONArray): List<MonthlySummaryEntity> =
        (0 until summariesArray.length()).map { i ->
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

    private fun parseWorkAddresses(json: JSONObject): List<WorkAddressEntity> {
        if (!json.has("workAddresses")) return emptyList()
        val workAddressesArray = json.getJSONArray("workAddresses")
        return (0 until workAddressesArray.length()).map { i ->
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
    }

    private fun parseAbsences(json: JSONObject): List<AbsenceEntity> {
        if (!json.has("absences")) return emptyList()
        val absencesArray = json.getJSONArray("absences")
        return (0 until absencesArray.length()).map { i ->
            val obj = absencesArray.getJSONObject(i)
            AbsenceEntity(
                id = obj.getLong("id"),
                type = obj.getString("type"),
                startDateEpochDay = obj.getLong("startDateEpochDay"),
                endDateEpochDay = obj.getLong("endDateEpochDay"),
                isFullDay = obj.optBoolean("isFullDay", true),
                hours = obj.optDouble("hours", DEFAULT_ABSENCE_HOURS).toFloat(),
                notes = obj.optString("notes").takeIf { it.isNotBlank() },
                isCounted = obj.optBoolean("isCounted", false)
            )
        }
    }

    private data class RestoreData(
        val requiredPercentage: Int,
        val countSaturdaysAsWorkdays: Boolean,
        val presencePolicy: PresencePolicy?,
        val checkIns: List<CheckInEntity>,
        val summaries: List<MonthlySummaryEntity>,
        val workAddresses: List<WorkAddressEntity>,
        val absences: List<AbsenceEntity>
    )

    companion object {
        const val BACKUP_VERSION = 4
        private const val UNSUPPORTED_VERSION = 0
        private val SUPPORTED_VERSIONS = setOf(3, 4)
        private const val DEFAULT_ABSENCE_HOURS = 8.0
    }
}
