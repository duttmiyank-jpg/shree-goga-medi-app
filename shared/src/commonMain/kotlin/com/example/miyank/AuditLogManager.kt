package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * System Audit Log Model
 */
@Serializable
data class AuditLog(
    val id: String = "",
    val actorUserId: String = "",
    val actorName: String = "",
    val actorRole: UserRole = UserRole.USER,
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val details: String = "",
    val timestamp: Long = 0L
)

/**
 * Central Audit Log Manager
 */
object AuditLogManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_AUDIT_LOGS_JSON = "system_audit_logs_v1"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _auditLogs = MutableStateFlow(loadLogs())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private fun loadLogs(): List<AuditLog> {
        val savedJson = settingsStorage.getString(KEY_AUDIT_LOGS_JSON, "")
        if (savedJson.isNotEmpty()) {
            try {
                return json.decodeFromString<List<AuditLog>>(savedJson).sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultAuditLogs()
    }

    private fun saveLogs(list: List<AuditLog>) {
        val sorted = list.sortedByDescending { it.timestamp }
        _auditLogs.value = sorted
        try {
            val encoded = json.encodeToString(sorted)
            settingsStorage.setString(KEY_AUDIT_LOGS_JSON, encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logAction(
        actorUserId: String,
        actorName: String,
        actorRole: UserRole,
        action: String,
        targetType: String = "",
        targetId: String = "",
        details: String = ""
    ) {
        val now = getEpochMillis()
        val current = _auditLogs.value.toMutableList()
        val newLog = AuditLog(
            id = "audit_$now",
            actorUserId = actorUserId.ifBlank { "system_admin" },
            actorName = actorName.ifBlank { "एडमिन" },
            actorRole = actorRole,
            action = action,
            targetType = targetType,
            targetId = targetId,
            details = details,
            timestamp = now
        )
        current.add(0, newLog)
        // Keep last 200 logs to optimize storage
        if (current.size > 200) {
            current.removeAt(current.size - 1)
        }
        saveLogs(current)
    }

    fun clearLogs() {
        saveLogs(emptyList())
    }

    private fun getDefaultAuditLogs(): List<AuditLog> {
        val now = getEpochMillis()
        return listOf(
            AuditLog(
                id = "audit_init",
                actorUserId = "system_dev",
                actorName = "डेवलपर एडमिन",
                actorRole = UserRole.DEVELOPER_ADMIN,
                action = "3-Panel System Initialized",
                targetType = "SYSTEM",
                targetId = "RBAC_V1",
                details = "User, Darbaris & Developer Admin Role Architecture Active",
                timestamp = now
            )
        )
    }
}
