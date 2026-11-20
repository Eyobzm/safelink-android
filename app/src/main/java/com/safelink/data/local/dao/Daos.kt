package com.safelink.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.safelink.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RulePatternDao {
    @Query("SELECT * FROM rule_pattern WHERE enabled = 1") suspend fun enabled(): List<RulePatternEntity>
    @Query("SELECT * FROM rule_pattern") suspend fun all(): List<RulePatternEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(rules: List<RulePatternEntity>)
    @Query("SELECT * FROM rule_meta WHERE id = 1") suspend fun meta(): RuleMetaEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun setMeta(m: RuleMetaEntity)

    /** FR-SYNC-04: all-or-nothing apply; a failure leaves the previous rule set. */
    @Transaction
    suspend fun applyDelta(rules: List<RulePatternEntity>, meta: RuleMetaEntity) {
        upsertAll(rules); setMeta(meta)
    }
}

@Dao
interface ScanHistoryDao {
    @Insert suspend fun insert(e: ScanHistoryEntity)
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT 200") fun recent(): Flow<List<ScanHistoryEntity>>
    @Query("DELETE FROM scan_history") suspend fun clear()   // user-clearable (FR-SET-01)
}

@Dao
interface GuideDao {
    @Query("SELECT * FROM guide_platform") suspend fun platforms(): List<GuidePlatformEntity>
    @Query("SELECT * FROM guide_section WHERE platformId = :p ORDER BY sortOrder") suspend fun sections(p: String): List<GuideSectionEntity>
    @Query("SELECT * FROM guide_step WHERE sectionId = :s ORDER BY sortOrder") suspend fun steps(s: Long): List<GuideStepEntity>
    @Query("SELECT * FROM guide_step WHERE title LIKE '%' || :q || '%' OR body LIKE '%' || :q || '%'")
    suspend fun search(q: String): List<GuideStepEntity>   // swap for FTS4 later (FR-GUIDE-07)

    @Query(
        """
        SELECT guide_step.title AS title, guide_step.body AS body,
               guide_section.platformId AS platformId, guide_section.type AS sectionType
        FROM guide_step
        INNER JOIN guide_section ON guide_step.sectionId = guide_section.id
        WHERE guide_step.title LIKE '%' || :q || '%' OR guide_step.body LIKE '%' || :q || '%'
        ORDER BY guide_section.platformId, guide_step.sortOrder
        """
    )
    suspend fun searchHits(q: String): List<GuideSearchHit>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertPlatform(p: GuidePlatformEntity)
    @Insert suspend fun insertSection(s: GuideSectionEntity): Long
    @Insert suspend fun insertStep(s: GuideStepEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM settings WHERE key = :k") suspend fun get(k: String): String?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(s: SettingEntity)
    @Query("SELECT * FROM settings") fun observe(): Flow<List<SettingEntity>>
    @Query("SELECT * FROM allow_list") suspend fun allowList(): List<AllowListEntity>
    @Query("SELECT * FROM allow_list ORDER BY domain") fun observeAllow(): Flow<List<AllowListEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun allow(a: AllowListEntity)
    @Query("DELETE FROM allow_list WHERE domain = :domain") suspend fun removeAllow(domain: String)
}

data class GuideSearchHit(
    val title: String,
    val body: String,
    val platformId: String,
    val sectionType: String,
)
