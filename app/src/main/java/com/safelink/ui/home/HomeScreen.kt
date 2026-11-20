package com.safelink.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.R
import com.safelink.data.repository.SettingsRepository
import com.safelink.ui.components.SafeScaffold
import com.safelink.ui.system.SystemIntents
import com.safelink.ui.system.looksLikeLink
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
    val paused = settings.observe()
        .map { it.paused }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}

@Composable
fun HomeScreen(
    onGuides: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onScan: (String) -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val paused by vm.paused.collectAsState()
    var link by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardMissing = stringResource(R.string.clipboard_empty)
    SafeScaffold(title = stringResource(R.string.home_title), actions = {
        androidx.compose.material3.IconButton(onClick = onSettings) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
        }
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(if (paused) R.string.home_protection_off else R.string.home_protection_on),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.paste_link_hint)) },
                singleLine = true,
            )
            Button(
                onClick = { onScan(link.trim()) },
                enabled = looksLikeLink(link),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.check_link)) }
            OutlinedButton(
                onClick = {
                    val clip = SystemIntents.clipboardText(context)
                    if (clip != null && looksLikeLink(clip)) onScan(clip.trim())
                    else android.widget.Toast.makeText(context, clipboardMissing, android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.check_clipboard)) }
            Spacer(Modifier.height(4.dp))
            HomeLink(Icons.Filled.MenuBook, stringResource(R.string.guides), stringResource(R.string.home_guides_blurb), onGuides)
            HomeLink(Icons.Filled.History, stringResource(R.string.history), stringResource(R.string.home_history_blurb), onHistory)
        }
    }
}

@Composable
private fun HomeLink(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
