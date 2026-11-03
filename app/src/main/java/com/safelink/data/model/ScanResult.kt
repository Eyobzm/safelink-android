package com.safelink.data.model

enum class Verdict { SAFE, SUSPICIOUS, DANGEROUS }

data class ScanResult(
    val verdict: Verdict,
    val reasons: List<String>,
    val messageKey: String,
    val degraded: Boolean = false,
)

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Loading : ScanUiState
    data class Done(val result: ScanResult) : ScanUiState
}
