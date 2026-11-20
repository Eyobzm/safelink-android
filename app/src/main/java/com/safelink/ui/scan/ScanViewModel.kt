package com.safelink.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.data.model.ScanUiState
import com.safelink.data.repository.AlertPrefs
import com.safelink.data.repository.ScanRepository
import com.safelink.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repo: ScanRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state

    private val _prefs = MutableStateFlow(AlertPrefs())
    val alertPrefs: StateFlow<AlertPrefs> = _prefs

    fun scan(url: String, sourceApp: String?) {
        _state.value = ScanUiState.Loading
        viewModelScope.launch {
            _prefs.value = settings.alertPrefs()
            _state.value = if (settings.isPaused()) {
                ScanUiState.Bypassed
            } else {
                ScanUiState.Done(repo.scan(url, sourceApp))
            }
        }
    }
}
