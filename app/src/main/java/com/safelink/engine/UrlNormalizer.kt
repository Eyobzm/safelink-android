package com.safelink.engine

import java.net.URLDecoder

/** FR-IMP-09: decode (multiple layers) and lowercase before matching. */
object UrlNormalizer {
    fun normalize(raw: String): String {
        var s = raw.trim()
        repeat(3) {
            val decoded = try { URLDecoder.decode(s.replace("+", "%2B"), "UTF-8") } catch (e: Exception) { s }
            if (decoded == s) return@repeat
            s = decoded
        }
        return s.lowercase()
    }

    fun host(url: String): String =
        url.substringAfter("://", url).substringBefore('/').substringBefore('?')
            .substringBefore('#').substringAfter('@').substringBefore(':')
}
