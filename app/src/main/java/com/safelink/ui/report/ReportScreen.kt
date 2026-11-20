package com.safelink.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.R
import com.safelink.data.repository.ReportRepository
import com.safelink.ui.components.SafeScaffold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReportStatus {
    data object Idle : ReportStatus
    data object Sending : ReportStatus
    data object Sent : ReportStatus
    data object Failed : ReportStatus
}

@HiltViewModel
class ReportViewModel @Inject constructor(private val repo: ReportRepository) : ViewModel() {
    private val _status = MutableStateFlow<ReportStatus>(ReportStatus.Idle)
    val status: StateFlow<ReportStatus> = _status

    fun submit(url: String, type: String, note: String, stripQuery: Boolean) {
        if (url.isBlank()) {
            _status.value = ReportStatus.Failed
            return
        }
        viewModelScope.launch {
            _status.value = ReportStatus.Sending
            _status.value = if (repo.submit(url, type, note, stripQuery)) ReportStatus.Sent else ReportStatus.Failed
        }
    }
}

@Composable
fun ReportScreen(initialUrl: String, onBack: () -> Unit, vm: ReportViewModel = hiltViewModel()) {
    SafeScaffold(title = stringResource(R.string.report_title), onBack = onBack) { padding ->
        ReportForm(initialUrl, Modifier.padding(padding), onClose = onBack, vm = vm)
    }
}

@Composable
fun ReportDialog(initialUrl: String, onDismiss: () -> Unit, vm: ReportViewModel = hiltViewModel()) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            SafeScaffold(title = stringResource(R.string.report_title), onBack = onDismiss) { padding ->
                ReportForm(initialUrl, Modifier.padding(padding), onClose = onDismiss, vm = vm)
            }
        }
    }
}

@Composable
private fun ReportForm(
    initialUrl: String,
    modifier: Modifier,
    onClose: () -> Unit,
    vm: ReportViewModel,
) {
    var url by remember { mutableStateOf(initialUrl) }
    var note by remember { mutableStateOf("") }
    var strip by remember { mutableStateOf(true) }
    var missed by remember { mutableStateOf(false) }
    val status by vm.status.collectAsState()
    Column(
        modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.report_url)) },
        )
        RowChoice(
            selected = strip,
            label = stringResource(R.string.report_strip),
            onClick = { strip = !strip },
            checkbox = true,
        )
        Text(stringResource(R.string.report_strip_help), style = MaterialTheme.typography.bodySmall)
        RowChoice(
            selected = !missed,
            label = stringResource(R.string.report_false_positive),
            onClick = { missed = false },
            checkbox = false,
        )
        RowChoice(
            selected = missed,
            label = stringResource(R.string.report_missed),
            onClick = { missed = true },
            checkbox = false,
        )
        OutlinedTextField(
            value = note,
            onValueChange = { if (it.length <= 1000) note = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.report_note)) },
            minLines = 3,
        )
        Button(
            onClick = {
                vm.submit(
                    url = url,
                    type = if (missed) "missed_threat" else "false_positive",
                    note = note,
                    stripQuery = strip,
                )
            },
            enabled = status != ReportStatus.Sending && url.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.report_send)) }
        when (status) {
            ReportStatus.Sent -> {
                Text(stringResource(R.string.report_sent))
                TextButton(onClick = onClose) { Text(stringResource(R.string.exit_action)) }
            }
            ReportStatus.Failed -> Text(
                stringResource(if (url.isBlank()) R.string.report_need_url else R.string.report_failed),
                color = MaterialTheme.colorScheme.error,
            )
            else -> Unit
        }
    }
}

@Composable
private fun RowChoice(selected: Boolean, label: String, onClick: () -> Unit, checkbox: Boolean) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (checkbox) Checkbox(checked = selected, onCheckedChange = { onClick() })
        else RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.weight(1f))
    }
}
