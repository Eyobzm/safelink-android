package com.safelink.engine

/** Local defense, runs before any network call (FR-IMP-01). Target < 50 ms. */
class ImpactAreaEngine(private val rules: List<Rule> = DefaultRules.all) {

    fun evaluate(rawUrl: String, sourceApp: String?): LocalVerdict {
        val url = UrlNormalizer.normalize(rawUrl)
        val host = UrlNormalizer.host(url)
        val hard = mutableListOf<ReasonCode>()
        val soft = mutableListOf<ReasonCode>()

        for (r in rules) {
            if (!r.regex.containsMatchIn(url)) continue
            // Device-link / OAuth only hard-block when the source is unknown (FR-IMP-03)
            val needsUnknownSource = r.type == RuleType.DEVICE_LINK || r.type == RuleType.OAUTH
            if (!needsUnknownSource || sourceApp.isNullOrBlank()) hard += r.reason
        }
        if (DomainValidator.isLookalike(host)) soft += ReasonCode.DOM_LOOKALIKE
        if (TldRiskChecker.isLowTrust(host)) soft += ReasonCode.DOM_LOW_TRUST_TLD
        if (url.startsWith("http://") && Regex("(login|signin|auth|account|verify)").containsMatchIn(url))
            soft += ReasonCode.NET_NO_TLS

        return LocalVerdict(hardBlock = hard.isNotEmpty(), reasons = (hard + soft).distinct())
    }
}
