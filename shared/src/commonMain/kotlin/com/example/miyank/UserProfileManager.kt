package com.example.miyank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Devotee User Profile Model with 3-Panel RBAC Support
 */
@Serializable
data class UserProfile(
    val fullName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val cityName: String = "पांवटा साहिब",
    val stateName: String = "हिमाचल प्रदेश",
    val gotra: String = "",
    val avatarImageUrl: String = "",
    val role: UserRole = UserRole.USER,
    val permissions: Set<Permission> = emptySet(),
    val updatedAtTimestamp: Long = 0L
) {
    fun getDisplayName(isHindi: Boolean = true): String {
        return fullName.ifBlank { if (isHindi) "श्रद्धालु" else "Devotee" }
    }

    fun getFormattedLocation(): String {
        val parts = listOf(cityName, stateName).filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "पांवटा साहिब, हिमाचल प्रदेश"
    }

    fun isComplete(): Boolean {
        return fullName.isNotBlank() && mobileNumber.isNotBlank()
    }

    fun isDeveloperAdmin(): Boolean {
        return role == UserRole.DEVELOPER_ADMIN
    }

    fun isDarbariOrAdmin(): Boolean {
        return role == UserRole.DARBARIS || role == UserRole.DEVELOPER_ADMIN
    }

    fun hasPermission(permission: Permission): Boolean {
        return RoleGuard.hasPermission(role, permissions, permission)
    }
}

/**
 * User Profile Manager
 */
object UserProfileManager {

    private val settingsStorage = SettingsManager()
    private const val KEY_USER_PROFILE_JSON = "user_profile_json_v1"
    private const val KEY_ACTIVE_ENV_KEY = "active_app_environment_v1"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _activeEnvironment = MutableStateFlow(loadActiveEnvironment())
    val activeEnvironment: StateFlow<AppEnvironment?> = _activeEnvironment.asStateFlow()

    private fun loadProfile(): UserProfile {
        val saved = settingsStorage.getString(KEY_USER_PROFILE_JSON, "")
        if (saved.isNotBlank()) {
            try {
                return json.decodeFromString(saved)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return UserProfile()
    }

    private fun loadActiveEnvironment(): AppEnvironment? {
        val saved = settingsStorage.getString(KEY_ACTIVE_ENV_KEY, "")
        return if (saved.isNotBlank()) {
            try {
                AppEnvironment.valueOf(saved)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun setActiveEnvironment(env: AppEnvironment?) {
        _activeEnvironment.value = env
        settingsStorage.setString(KEY_ACTIVE_ENV_KEY, env?.name ?: "")
    }

    fun clearActiveEnvironment() {
        setActiveEnvironment(null)
    }

    fun saveProfile(profile: UserProfile) {
        val updated = profile.copy(updatedAtTimestamp = getEpochMillis())
        _userProfile.value = updated
        try {
            settingsStorage.setString(KEY_USER_PROFILE_JSON, json.encodeToString(updated))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateUserRole(
        newRole: UserRole,
        newPermissions: Set<Permission> = Permission.getDefaultPermissionsForRole(newRole),
        actorName: String = "एडमिन"
    ) {
        val current = _userProfile.value
        val updated = current.copy(
            role = newRole,
            permissions = newPermissions
        )
        saveProfile(updated)

        AuditLogManager.logAction(
            actorUserId = current.mobileNumber.ifBlank { "system" },
            actorName = actorName,
            actorRole = current.role,
            action = "USER_ROLE_CHANGED",
            targetType = "USER",
            targetId = current.mobileNumber.ifBlank { "current_user" },
            details = "Role updated to ${newRole.name} with ${newPermissions.size} permissions"
        )
    }

    fun updateAvatar(imagePath: String) {
        val current = _userProfile.value
        saveProfile(current.copy(avatarImageUrl = imagePath))
    }

    fun clearProfile() {
        _userProfile.value = UserProfile()
        settingsStorage.setString(KEY_USER_PROFILE_JSON, "")
        clearActiveEnvironment()
    }
}
