package com.safelink.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.R
import com.safelink.data.repository.ScanRepository
import com.safelink.ui.components.SafeScaffold
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(private val repo: ScanRepository) : ViewModel() {
    val items = repo.history().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clear() {
        viewModelScope.launch { repo.clearHistory() }
    }
}

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onReport: (String) -> Unit,
    vm: HistoryViewModel = hiltViewModel(),
) {
    val items by vm.items.collectAsState()
    var confirm by remember { mutableStateOf(false) }
    val format = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
    SafeScaffold(
        title = stringResource(R.string.history),
        onBack = onBack,
        actions = {
            if (items.isNotEmpty()) {
                IconButton(onClick = { confirm = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.history_clear))
                }
            }
        },
    ) { padding ->
        if (items.isEmpty()) {
            Text(
                stringResource(R.string.history_empty),
                modifier = Modifier.padding(padding).padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items, key = { it.id }) { row ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(row.host, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Text(verdictLabel(row.verdict), color = MaterialTheme.colorScheme.primary)
                            }
                            Text(format.format(Date(row.timestamp)), style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { onReport("https://${row.host}") }) {
                                Text(stringResource(R.string.report_domain))
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = { Text(stringResource(R.string.history_clear_body)) },
            confirmButton = {
                TextButton(onClick = { confirm = false; vm.clear() }) {
                    Text(stringResource(R.string.history_clear_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun verdictLabel(verdict: String): String = stringResource(
    when (verdict) {
        "SAFE" -> R.string.verdict_safe
        "DANGEROUS" -> R.string.verdict_dangerous
        else -> R.string.verdict_suspicious
    }
)
