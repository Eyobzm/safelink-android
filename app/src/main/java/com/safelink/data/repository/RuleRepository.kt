package com.safelink.data.repository

import android.util.Base64
import com.safelink.data.local.dao.RulePatternDao
import com.safelink.data.local.entity.RuleMetaEntity
import com.safelink.data.local.entity.RulePatternEntity
import com.safelink.data.remote.SafeLinkApi
import com.safelink.sync.SignatureVerifier
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncResult { APPLIED, UNCHANGED, REJECTED, RETRY }

@Singleton
class RuleRepository @Inject constructor(
    private val dao: RulePatternDao,
    private val api: SafeLinkApi,
    private val verifier: SignatureVerifier,
) {
    /** Verify the signature, then apply. A bad package does not replace the current rules. */
    suspend fun syncDelta(): SyncResult {
        val delta = try {
            val current = dao.meta()?.currentVersion ?: 0
            api.rulesDelta(current)
        } catch (e: IOException) {
            return SyncResult.RETRY
        } catch (e: HttpException) {
            return SyncResult.RETRY
        } catch (e: Exception) {
            return SyncResult.RETRY
        }

        val packaged = delta.packageB64 ?: return SyncResult.UNCHANGED
        val signature = delta.signature ?: return SyncResult.REJECTED
        return try {
            val pkgBytes = Base64.decode(packaged, Base64.DEFAULT)
            val sigBytes = Base64.decode(signature, Base64.DEFAULT)
            if (!verifier.verify(pkgBytes, sigBytes)) return SyncResult.REJECTED
            val rules = parseRules(decodePackage(pkgBytes), delta.version)
            if (rules.isEmpty()) return SyncResult.REJECTED
            dao.applyDelta(
                rules,
                RuleMetaEntity(
                    currentVersion = delta.version,
                    lastSyncAt = System.currentTimeMillis(),
                    lastSyncStatus = "OK",
                ),
            )
            SyncResult.APPLIED
        } catch (e: Exception) {
            SyncResult.REJECTED
        }
    }

    private fun decodePackage(bytes: ByteArray): String {
        val raw = if (bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()) {
            GZIPInputStream(bytes.inputStream()).use { it.readBytes() }
        } else {
            bytes
        }
        return raw.toString(Charsets.UTF_8)
    }

    private fun parseRules(json: String, version: Int): List<RulePatternEntity> {
        val arr = JSONObject(json).getJSONArray("rules")
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(
                    RulePatternEntity(
                        id = o.getLong("id"),
                        type = o.getString("type"),
                        regex = o.getString("regex"),
                        reasonCode = o.getString("reason_code"),
                        version = version,
                        enabled = o.optBoolean("enabled", true),
                    )
                )
            }
        }
    }
}
