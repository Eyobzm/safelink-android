package com.safelink.engine

enum class RuleType { DELETE_PATH, DEVICE_LINK, OAUTH, TLD, BRAND }

data class Rule(val id: Long, val type: RuleType, val regex: Regex, val reason: ReasonCode)

/** Built-in fallback rules; delta updates add/modify them (FR-IMP-07). */
object DefaultRules {
    val all: List<Rule> = listOf(
        Rule(1, RuleType.DELETE_PATH, Regex("/(delete-video|remove-content|delete-account|confirm-termination|wipe)(\\b|/|\\?|$)"), ReasonCode.IMP_DELETE_PATH),
        Rule(2, RuleType.DEVICE_LINK, Regex("(tg://(login|auth)|whatsapp://proxy)"), ReasonCode.IMP_DEVICE_LINK),
        Rule(3, RuleType.OAUTH, Regex("[?&]redirect_uri=https?://"), ReasonCode.IMP_OAUTH_REDIRECT),
    )
}
