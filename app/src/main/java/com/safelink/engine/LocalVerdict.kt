package com.safelink.engine

data class LocalVerdict(
    val hardBlock: Boolean,
    val reasons: List<ReasonCode>,
) {
    /** Flags forwarded to the backend as local_flags. */
    val flags: List<String> get() = reasons.map { it.name }
}
