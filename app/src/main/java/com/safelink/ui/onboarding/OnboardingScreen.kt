package com.safelink.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.R
import com.safelink.data.repository.SettingsRepository
import com.safelink.ui.system.SystemIntents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {
    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            settings.setOnboardingDone()
            onDone()
        }
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit, vm: OnboardingViewModel = hiltViewModel()) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val title = stringResource(
        when (step) {
            0 -> R.string.onboarding_welcome_title
            1 -> R.string.onboarding_privacy_title
            else -> R.string.onboarding_handler_title
        }
    )
    val body = stringResource(
        when (step) {
            0 -> R.string.onboarding_welcome_body
            1 -> R.string.onboarding_privacy_body
            else -> R.string.onboarding_handler_body
        }
    )
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("SafeLink", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(16.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        if (step == 2) {
            OutlinedButton(
                onClick = { SystemIntents.openLinkHandlerSettings(context) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.open_link_settings)) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { vm.finish(onDone) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.onboarding_start))
            }
        } else {
            Button(onClick = { step += 1 }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.onboarding_next))
            }
        }
    }
}
