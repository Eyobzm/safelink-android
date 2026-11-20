package com.safelink.engine

/** Maps to string resources via messageKey (FR-IMP-08, SRS Appendix A). */
enum class ReasonCode(val messageKey: String) {
    IMP_DELETE_PATH("msg_dangerous_delete"),
    IMP_DEVICE_LINK("msg_dangerous_hijack"),
    IMP_OAUTH_REDIRECT("msg_dangerous_hijack"),
    DOM_LOOKALIKE("msg_suspicious_lookalike"),
    DOM_LOW_TRUST_TLD("msg_suspicious_tld"),
    NET_NO_TLS("msg_suspicious_notls"),
    NET_REDIRECT_CHAIN("msg_suspicious_redirect"),
    TI_GSB("msg_dangerous_hijack"),
    TI_VT("msg_suspicious_lookalike"),
    TI_PT_VERIFIED("msg_dangerous_hijack"),
    TI_PT_UNVERIFIED("msg_suspicious_lookalike"),
    SCAN_DEGRADED("msg_suspicious_generic");

    companion object {
        fun fromOrNull(s: String) = entries.firstOrNull { it.name == s }
    }
}
