package com.safelink.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.R
import com.safelink.data.repository.Prefs
import com.safelink.data.repository.SettingsRepository
import com.safelink.data.repository.SettingsUi
import com.safelink.ui.components.SafeScaffold
import com.safelink.ui.system.SystemIntents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    val state = repo.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUi())

    fun setSound(value: Boolean) = write { repo.setBool(Prefs.ALARM_SOUND, value) }
    fun setHaptics(value: Boolean) = write { repo.setBool(Prefs.HAPTICS, value) }
    fun setPaused(value: Boolean) = write { repo.setBool(Prefs.PROTECTION_PAUSED, value) }
    fun setVolume(value: Int) = write { repo.setVolumeCap(value) }
    fun addDomain(raw: String, onResult: (Boolean) -> Unit) = write {
        onResult(repo.addAllowedDomain(raw))
    }
    fun removeDomain(domain: String) = write { repo.removeAllowedDomain(domain) }

    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onPrivacy: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var domain by remember { mutableStateOf("") }
    var domainError by remember { mutableStateOf(false) }
    SafeScaffold(title = stringResource(R.string.settings), onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ToggleRow(stringResource(R.string.settings_sound), state.alarmSound, vm::setSound)
            Text(stringResource(R.string.settings_sound_help), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.settings_volume))
            Slider(
                value = state.volumeCap.toFloat(),
                onValueChange = { vm.setVolume(it.toInt()) },
                valueRange = 0f..100f,
            )
            ToggleRow(stringResource(R.string.settings_haptics), state.haptics, vm::setHaptics)
            ToggleRow(stringResource(R.string.settings_pause), state.paused, vm::setPaused)
            Text(stringResource(R.string.settings_pause_help), style = MaterialTheme.typography.bodySmall)

            Text(stringResource(R.string.settings_allow), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_allow_help), style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it; domainError = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_allow_hint)) },
                isError = domainError,
                supportingText = if (domainError) {
                    { Text(stringResource(R.string.settings_allow_invalid)) }
                } else null,
                singleLine = true,
            )
            Button(
                onClick = {
                    vm.addDomain(domain) { ok ->
                        domainError = !ok
                        if (ok) domain = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_allow_add)) }
            state.allowList.forEach { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(item, modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.removeDomain(item) }) {
                        Text(stringResource(R.string.settings_remove, item))
                    }
                }
            }

            Text(stringResource(R.string.settings_disable_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_disable_body), style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = { SystemIntents.openLinkHandlerSettings(context) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.open_link_settings)) }
            OutlinedButton(
                onClick = { SystemIntents.uninstall(context) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.uninstall)) }
            TextButton(onClick = onPrivacy, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.privacy_policy))
            }
        }
    }
}

@Composable
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    SafeScaffold(title = stringResource(R.string.privacy_policy), onBack = onBack) { padding ->
        Text(
            stringResource(R.string.onboarding_privacy_body),
            modifier = Modifier.padding(padding).padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
