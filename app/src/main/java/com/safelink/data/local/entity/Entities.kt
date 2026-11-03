package com.safelink.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "rule_pattern")
data class RulePatternEntity(
    @PrimaryKey val id: Long,
    val type: String,          // DELETE_PATH | DEVICE_LINK | OAUTH | TLD | BRAND
    val regex: String,
    val reasonCode: String,
    val version: Int,
    val enabled: Boolean = true,
)

@Entity(tableName = "rule_meta")
data class RuleMetaEntity(
    @PrimaryKey val id: Int = 1,
    val currentVersion: Int = 0,
    val lastSyncAt: Long = 0,
    val lastSyncStatus: String = "NONE",
)

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val host: String,          // host only, never the full URL
    val verdict: String,
    val reasonCode: String?,
    val sourceApp: String?,
    val timestamp: Long,
)

@Entity(tableName = "guide_platform")
data class GuidePlatformEntity(@PrimaryKey val id: String, val name: String, val icon: String? = null)

@Entity(
    tableName = "guide_section",
    foreignKeys = [ForeignKey(GuidePlatformEntity::class, ["id"], ["platformId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("platformId")],
)
data class GuideSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platformId: String,
    val type: String,          // PROTECT | WARNING_SIGNS | RECOVER
    val sortOrder: Int,
)

@Entity(
    tableName = "guide_step",
    foreignKeys = [ForeignKey(GuideSectionEntity::class, ["id"], ["sectionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("sectionId")],
)
data class GuideStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectionId: Long,
    val title: String,
    val body: String,
    val sortOrder: Int,
    val verifiedOn: String? = null,   // FR-GUIDE-08
)

@Entity(tableName = "allow_list")
data class AllowListEntity(@PrimaryKey val domain: String)

@Entity(tableName = "settings")
data class SettingEntity(@PrimaryKey val key: String, val value: String)
