package com.safelink.gateway

import android.content.Intent

/** FR-INT-02: full URL (scheme, host, path, query, fragment). */
object UrlExtractor {
    fun from(intent: Intent): String? = intent.dataString?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
}
