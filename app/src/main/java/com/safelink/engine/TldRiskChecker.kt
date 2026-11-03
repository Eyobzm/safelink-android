package com.safelink.engine

object TldRiskChecker {
    private val risky = listOf(".xyz", ".top", ".click", ".eu.cc", ".tk", ".gq")
    fun isLowTrust(host: String) = risky.any { host.endsWith(it) }
}
