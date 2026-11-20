package com.safelink.gateway

import android.content.Intent

/** FR-INT-02: full URL (scheme, host, path, query, fragment). */
object UrlExtractor {
    fun from(intent: Intent): String? {
        val raw = intent.dataString ?: return null
        val lower = raw.lowercase()
        val allowed = lower.startsWith("http://") || lower.startsWith("https://") ||
            lower.startsWith("tg://login") || lower.startsWith("tg://auth") ||
            lower.startsWith("whatsapp://proxy")
        return raw.takeIf { allowed }
    }
}
