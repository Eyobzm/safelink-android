package com.safelink.data.repository

import com.safelink.data.local.dao.RulePatternDao
import com.safelink.data.remote.SafeLinkApi
import com.safelink.sync.SignatureVerifier
import javax.inject.Inject

class RuleRepository @Inject constructor(
    private val dao: RulePatternDao,
    private val api: SafeLinkApi,
    private val verifier: SignatureVerifier,
) {
    /** TODO: decode base64 package, gunzip, verify signature, parse JSON, dao.applyDelta(...). */
    suspend fun syncDelta(): Boolean = try {
        val current = dao.meta()?.currentVersion ?: 0
        val delta = api.rulesDelta(current)
        delta["package"] == null // nothing new => success; real apply is a TODO for Phase 4
    } catch (e: Exception) { false }
}
