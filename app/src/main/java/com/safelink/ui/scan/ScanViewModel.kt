package com.safelink.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.data.model.ScanUiState
import com.safelink.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(private val repo: ScanRepository) : ViewModel() {
    private val _state = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val state: StateFlow<ScanUiState> = _state

    fun scan(url: String, sourceApp: String?) {
        _state.value = ScanUiState.Loading
        viewModelScope.launch { _state.value = ScanUiState.Done(repo.scan(url, sourceApp)) }
    }
}
