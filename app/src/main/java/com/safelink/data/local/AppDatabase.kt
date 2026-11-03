package com.safelink.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.safelink.data.local.dao.*
import com.safelink.data.local.entity.*

@Database(
    entities = [
        RulePatternEntity::class, RuleMetaEntity::class, ScanHistoryEntity::class,
        GuidePlatformEntity::class, GuideSectionEntity::class, GuideStepEntity::class,
        AllowListEntity::class, SettingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rules(): RulePatternDao
    abstract fun history(): ScanHistoryDao
    abstract fun guides(): GuideDao
    abstract fun settings(): SettingsDao
}
