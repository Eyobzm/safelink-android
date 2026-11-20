package com.safelink.ui.result

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.safelink.R
import com.safelink.alert.AlarmController
import com.safelink.data.model.ScanUiState
import com.safelink.data.model.Verdict
import com.safelink.data.repository.AlertPrefs
import com.safelink.ui.messageRes

private val AdvisoryBg = Color(0xFFFFF4CC)
private val AdvisoryInk = Color(0xFF5C4300)
private val CrimsonBg = Color(0xFF9B1028)

/**
 * Three-state enforcement (FR-UX). SAFE never blocks. SUSPICIOUS pauses.
 * DANGEROUS stops navigation and raises the alarm until the user exits.
 */
@Composable
fun ResultHost(
    state: ScanUiState,
    prefs: AlertPrefs,
    onForward: () -> Boolean,
    onExit: () -> Unit,
    onReport: () -> Unit,
) {
    when (state) {
        ScanUiState.Idle, ScanUiState.Loading -> Checking()
        ScanUiState.Bypassed -> ForwardOnce(
            toast = stringResource(R.string.protection_paused_forward),
            onForward = onForward,
            onExit = onExit,
        )
        is ScanUiState.Done -> when (state.result.verdict) {
            Verdict.SAFE -> ForwardOnce(
                toast = stringResource(messageRes(state.result.messageKey)),
                onForward = onForward,
                onExit = onExit,
            )
            Verdict.SUSPICIOUS -> Advisory(
                message = stringResource(messageRes(state.result.messageKey)),
                onSafe = onExit,
                onProceed = onForward,
                onReport = onReport,
            )
            Verdict.DANGEROUS -> Crimson(
                message = stringResource(messageRes(state.result.messageKey)),
                prefs = prefs,
                onExit = onExit,
            )
        }
    }
}

@Composable
private fun Checking() {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.checking_link), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ForwardOnce(toast: String, onForward: () -> Boolean, onExit: () -> Unit) {
    val context = LocalContext.current
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
        if (onForward()) onExit() else failed = true
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), Alignment.Center) {
        if (failed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.no_browser))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onExit) { Text(stringResource(R.string.exit_action)) }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CheckCircle, contentDescription = stringResource(R.string.cd_status_icon), tint = Color(0xFF0E6B63))
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.opening_link))
            }
        }
    }
}

@Composable
private fun Advisory(message: String, onSafe: () -> Unit, onProceed: () -> Boolean, onReport: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(AdvisoryBg).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Warning, contentDescription = stringResource(R.string.cd_status_icon), tint = AdvisoryInk)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.caution_title),
            color = AdvisoryInk,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
        Text(message, color = AdvisoryInk, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        Button(onClick = onSafe, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.return_to_safety))
        }
        TextButton(
            onClick = { if (onProceed()) onSafe() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.proceed_manual))
        }
        TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.report_problem))
        }
    }
}

@Composable
private fun Crimson(message: String, prefs: AlertPrefs, onExit: () -> Unit) {
    val context = LocalContext.current
    val alarm = remember { AlarmController(context) }
    DisposableEffect(prefs) {
        alarm.start(sound = prefs.sound, haptics = prefs.haptics, volumeCapPercent = prefs.volumeCap)
        onDispose { alarm.stop() }
    }
    Column(
        Modifier.fillMaxSize().background(CrimsonBg).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.GppBad, contentDescription = stringResource(R.string.cd_status_icon), tint = Color.White)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.blocked_title),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        Button(onClick = onExit, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(stringResource(R.string.exit_action))
        }
    }
}
