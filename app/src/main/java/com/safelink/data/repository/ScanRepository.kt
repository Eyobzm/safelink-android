package com.safelink.data.repository

import com.safelink.data.local.dao.RulePatternDao
import com.safelink.data.local.dao.ScanHistoryDao
import com.safelink.data.local.entity.RulePatternEntity
import com.safelink.data.local.entity.ScanHistoryEntity
import com.safelink.data.model.ScanResult
import com.safelink.data.model.Verdict
import com.safelink.data.remote.SafeLinkApi
import com.safelink.data.remote.ScanRequestDto
import com.safelink.engine.AllowListMatcher
import com.safelink.engine.DefaultRules
import com.safelink.engine.ImpactAreaEngine
import com.safelink.engine.ReasonCode
import com.safelink.engine.Rule
import com.safelink.engine.RuleType
import com.safelink.engine.UrlNormalizer
import com.safelink.engine.mergeRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/** local engine -> backend -> fail-safe SUSPICIOUS (FR-VER-06, C-04). Never SAFE on failure. */
@Singleton
class ScanRepository @Inject constructor(
    private val engine: ImpactAreaEngine,
    private val api: SafeLinkApi,
    private val historyDao: ScanHistoryDao,
    private val rules: RulePatternDao,
    private val settings: SettingsRepository,
) {
    fun history(): Flow<List<ScanHistoryEntity>> = historyDao.recent()

    suspend fun clearHistory() = historyDao.clear()

    suspend fun scan(url: String, sourceApp: String?): ScanResult {
        val local = activeEngine().evaluate(url, sourceApp)
        val host = runCatching { UrlNormalizer.host(UrlNormalizer.normalize(url)) }.getOrDefault("")
        val result = if (local.hardBlock) {
            // No network call on hard block (AC-02). Allow-list cannot override this (FR-SET-03).
            val reason = local.reasons.first()
            ScanResult(Verdict.DANGEROUS, local.flags, reason.messageKey)
        } else if (AllowListMatcher.matches(host, settings.allowedDomains())) {
            ScanResult(Verdict.SAFE, listOf("ALLOW_LIST"), "msg_allow_listed")
        } else {
            try {
                val r = withTimeout(CLIENT_TIMEOUT_MS) {
                    api.scan(ScanRequestDto(url, sourceApp, "1.0.0", local.flags))
                }
                ScanResult(Verdict.valueOf(r.verdict), r.reasons, r.message_key, r.degraded)
            } catch (e: Exception) {
                ScanResult(Verdict.SUSPICIOUS, listOf("SCAN_DEGRADED"), "msg_suspicious_generic", degraded = true)
            }
        }
        if (host.isNotBlank()) {
            historyDao.insert(
                ScanHistoryEntity(
                    host = host,
                    verdict = result.verdict.name,
                    reasonCode = result.reasons.firstOrNull(),
                    sourceApp = sourceApp,
                    timestamp = System.currentTimeMillis(),
                )
            )
        }
        return result
    }

    private suspend fun activeEngine(): ImpactAreaEngine {
        val rows = rules.all()
        if (rows.isEmpty()) return engine
        val enabled = rows.filter { it.enabled }.mapNotNull { toRule(it) }
        val disabled = rows.filter { !it.enabled }.map { it.id }.toSet()
        val merged = mergeRules(DefaultRules.all, enabled, disabled)
        return if (merged.isEmpty()) engine else ImpactAreaEngine(merged)
    }

    private companion object {
        const val CLIENT_TIMEOUT_MS = 1800L
    }
}

private fun toRule(row: RulePatternEntity): Rule? {
    val type = runCatching { RuleType.valueOf(row.type) }.getOrNull() ?: return null
    val reason = ReasonCode.fromOrNull(row.reasonCode) ?: return null
    val regex = runCatching { Regex(row.regex) }.getOrNull() ?: return null
    return Rule(row.id, type, regex, reason)
}
