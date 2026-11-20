package com.safelink.ui

import androidx.annotation.StringRes
import com.safelink.R

@StringRes
fun messageRes(key: String): Int = when (key) {
    "msg_safe" -> R.string.msg_safe
    "msg_allow_listed" -> R.string.msg_allow_listed
    "msg_suspicious_redirect" -> R.string.msg_suspicious_redirect
    "msg_suspicious_lookalike" -> R.string.msg_suspicious_lookalike
    "msg_suspicious_tld" -> R.string.msg_suspicious_tld
    "msg_suspicious_notls" -> R.string.msg_suspicious_notls
    "msg_dangerous_hijack" -> R.string.msg_dangerous_hijack
    "msg_dangerous_delete" -> R.string.msg_dangerous_delete
    else -> R.string.msg_suspicious_generic
}
