package com.example.miyank

import kotlinx.serialization.Serializable

/**
 * 3 Distinct Navigation Environments
 */
@Serializable
enum class AppEnvironment {
    USER,
    DARBARIS,
    DEVELOPER_ADMIN;

    fun getDisplayName(isHindi: Boolean = true): String {
        return when (this) {
            USER -> if (isHindi) "श्रद्धालु ऐप (Devotee App)" else "Devotee App"
            DARBARIS -> if (isHindi) "दरबारी व पुजारी सेवा (Darbari App)" else "Darbari Operations App"
            DEVELOPER_ADMIN -> if (isHindi) "डेवलपर एडमिन (Master Admin App)" else "Master Developer Admin App"
        }
    }
}

/**
 * 3-Panel System Application Roles
 */
@Serializable
enum class UserRole {
    USER,
    DARBARIS,
    DEVELOPER_ADMIN;

    fun getDisplayName(isHindi: Boolean = true): String {
        return when (this) {
            USER -> if (isHindi) "श्रद्धालु (Devotee)" else "Devotee (User)"
            DARBARIS -> if (isHindi) "दरबारी / पुजारी (Darbari)" else "Temple Darbari"
            DEVELOPER_ADMIN -> if (isHindi) "डेवलपर एडमिन (Master Admin)" else "Developer Admin"
        }
    }

    fun hasAccessToDarbarisPanel(): Boolean {
        return this == DARBARIS || this == DEVELOPER_ADMIN
    }

    fun hasAccessToAdminPanel(): Boolean {
        return this == DEVELOPER_ADMIN
    }
}

/**
 * Fine-grained Permission Model
 */
@Serializable
enum class Permission {
    VIEW_ANALYTICS,
    MANAGE_USERS,
    MANAGE_DARBARIS,
    MANAGE_ROLES,
    MANAGE_APP_SETTINGS,
    MANAGE_LIVE_STREAM,
    MANAGE_BHAJANS,
    MANAGE_PUJA,
    MANAGE_YATRA,
    MANAGE_STORE,
    MANAGE_ORDERS,
    MANAGE_DONATIONS,
    MANAGE_PARCHA,
    MANAGE_USER_QUERIES,
    SEND_NOTIFICATIONS,
    MANAGE_CALENDAR,
    MANAGE_EVENTS,
    MANAGE_BLOG,
    MANAGE_GALLERY,
    MANAGE_SUVICHAR,
    MANAGE_ANNOUNCEMENTS,
    VIEW_DARBAR_OPERATIONS;

    companion object {
        fun getDefaultPermissionsForRole(role: UserRole): Set<Permission> {
            return when (role) {
                UserRole.DEVELOPER_ADMIN -> Permission.entries.toSet()
                UserRole.DARBARIS -> setOf(
                    VIEW_DARBAR_OPERATIONS,
                    MANAGE_PARCHA,
                    MANAGE_USER_QUERIES,
                    MANAGE_PUJA,
                    MANAGE_YATRA,
                    MANAGE_ANNOUNCEMENTS,
                    MANAGE_EVENTS,
                    MANAGE_GALLERY,
                    MANAGE_SUVICHAR,
                    MANAGE_CALENDAR
                )
                UserRole.USER -> emptySet()
            }
        }
    }
}

/**
 * User Session State Model
 */
@Serializable
data class UserSession(
    val userId: String = "",
    val role: UserRole = UserRole.USER,
    val permissions: Set<Permission> = emptySet(),
    val isActive: Boolean = true
)

/**
 * Central Authorization & Role Guard Logic
 */
object RoleGuard {

    fun canAccessAdminPanel(userRole: UserRole): Boolean {
        return userRole == UserRole.DEVELOPER_ADMIN
    }

    fun canAccessDarbarisPanel(userRole: UserRole): Boolean {
        return userRole == UserRole.DARBARIS || userRole == UserRole.DEVELOPER_ADMIN
    }

    fun hasPermission(
        userRole: UserRole,
        userPermissions: Set<Permission>,
        requiredPermission: Permission
    ): Boolean {
        if (userRole == UserRole.DEVELOPER_ADMIN) return true
        if (userPermissions.contains(requiredPermission)) return true
        val defaultRolePermissions = Permission.getDefaultPermissionsForRole(userRole)
        return defaultRolePermissions.contains(requiredPermission)
    }
}
