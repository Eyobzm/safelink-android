package com.safelink.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.safelink.alert.AlarmController
import com.safelink.data.model.ScanUiState
import com.safelink.data.model.Verdict

/** Placeholder 3-state UI (FR-UX-01..03). Replace strings with stringResource(messageKey) lookups. */
@Composable
fun ResultHost(state: ScanUiState, onForward: () -> Unit, onExit: () -> Unit, onReport: () -> Unit) {
    when (state) {
        ScanUiState.Idle, ScanUiState.Loading ->
            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() } // "Checking Link…"
        is ScanUiState.Done -> when (state.result.verdict) {
            Verdict.SAFE -> LaunchedEffect(Unit) { onForward() }
            Verdict.SUSPICIOUS -> Advisory(state.result.messageKey, onExit, onForward)
            Verdict.DANGEROUS -> Crimson(state.result.messageKey, onExit)
        }
    }
}

@Composable
private fun Advisory(messageKey: String, onSafe: () -> Unit, onProceed: () -> Unit) =
    Column(Modifier.fillMaxSize().background(Color(0xFFFFF3C4)).padding(24.dp), Arrangement.Center) {
        Text("⚠ Caution", style = MaterialTheme.typography.headlineMedium)
        Text(messageKey) // TODO: resolve to string resource
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSafe, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Return to Safety") }
        TextButton(onClick = onProceed) { Text("Proceed Manual") }
    }

@Composable
private fun Crimson(messageKey: String, onExit: () -> Unit) {
    val ctx = LocalContext.current
    val alarm = AlarmController(ctx)
    DisposableEffect(Unit) { alarm.start(); onDispose { alarm.stop() } }
    Column(Modifier.fillMaxSize().background(Color(0xFFB00020)).padding(24.dp), Arrangement.Center) {
        Text("⛔ Blocked", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text(messageKey, color = Color.White) // TODO: resolve to string resource
        Spacer(Modifier.height(24.dp))
        Button(onClick = onExit, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Exit") }
    }
}
