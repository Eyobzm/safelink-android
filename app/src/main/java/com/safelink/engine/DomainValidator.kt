package com.safelink.engine

/**
 * FR-IMP-04. Parses host right-to-left.
 * NOTE: the registrable-domain logic here is naive (last two labels). Use the Public Suffix List
 * (e.g. OkHttp's PublicSuffixDatabase) before release.
 */
object DomainValidator {
    private val brands = listOf("paypal", "telegram", "whatsapp", "facebook", "instagram", "google", "microsoft", "apple")

    fun isLookalike(host: String): Boolean {
        if (host.isEmpty()) return false
        if (host.split('.').any { it.startsWith("xn--") }) return true         // punycode / homoglyph
        if (host.any { it.code > 127 }) return true                            // non-ASCII
        val labels = host.split('.')
        if (labels.size > 4) return true                                       // excessive subdomains
        val registrable = labels.takeLast(2).joinToString(".")
        val subdomains = labels.dropLast(2).joinToString(".")
        // brand placed in a subdomain of an unrelated domain
        if (brands.any { subdomains.contains(it) && !registrable.startsWith(it) }) return true
        // digit/char substitution in the registrable label (paypa1, faceb00k)
        val label = labels.getOrNull(labels.size - 2) ?: return false
        val deSubbed = label.replace('0', 'o').replace('1', 'l').replace('3', 'e').replace('5', 's')
        return brands.any { it != label && deSubbed == it }
    }
}
