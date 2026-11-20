package com.safelink.engine

object AllowListMatcher {
    /** Accepts "Example.com", "https://example.com/path", or "www.example.com". */
    fun normalizeDomain(raw: String): String? {
        var s = raw.trim().lowercase()
        s = s.removePrefix("https://").removePrefix("http://")
        s = s.substringBefore('/').substringBefore('?').substringBefore('#')
        s = s.substringAfter('@').substringBefore(':')
        s = s.removePrefix("www.").trim('.')
        if (s.isBlank() || !s.contains('.') || s.any { it.isWhitespace() || it == '/' }) return null
        return s
    }

    fun matches(host: String, domains: Collection<String>): Boolean {
        val h = host.lowercase().removePrefix("www.")
        if (h.isBlank()) return false
        return domains.any { domain ->
            val d = domain.lowercase().removePrefix("www.")
            h == d || h.endsWith(".$d")
        }
    }
}
