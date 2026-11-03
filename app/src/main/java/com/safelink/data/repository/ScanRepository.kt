package com.safelink.data.repository

import com.safelink.data.local.dao.ScanHistoryDao
import com.safelink.data.local.entity.ScanHistoryEntity
import com.safelink.data.model.ScanResult
import com.safelink.data.model.Verdict
import com.safelink.data.remote.SafeLinkApi
import com.safelink.data.remote.ScanRequestDto
import com.safelink.engine.ImpactAreaEngine
import com.safelink.engine.UrlNormalizer
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

/** local engine -> backend -> fail-safe SUSPICIOUS (FR-VER-06, C-04). Never SAFE on failure. */
class ScanRepository @Inject constructor(
    private val engine: ImpactAreaEngine,
    private val api: SafeLinkApi,
    private val history: ScanHistoryDao,
) {
    suspend fun scan(url: String, sourceApp: String?): ScanResult {
        val local = engine.evaluate(url, sourceApp)
        val result = if (local.hardBlock) {
            // No network call on hard block (AC-02)
            ScanResult(Verdict.DANGEROUS, local.flags, local.reasons.first().messageKey)
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
        history.insert(
            ScanHistoryEntity(
                host = UrlNormalizer.host(UrlNormalizer.normalize(url)),
                verdict = result.verdict.name,
                reasonCode = result.reasons.firstOrNull(),
                sourceApp = sourceApp,
                timestamp = System.currentTimeMillis(),
            )
        )
        return result
    }

    private companion object { const val CLIENT_TIMEOUT_MS = 1800L }
}
