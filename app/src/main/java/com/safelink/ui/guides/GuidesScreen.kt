package com.safelink.ui.guides

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safelink.R
import com.safelink.data.local.dao.GuideSearchHit
import com.safelink.data.local.entity.GuidePlatformEntity
import com.safelink.data.repository.GuideDetail
import com.safelink.data.repository.GuideRepository
import com.safelink.ui.components.SafeScaffold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuidesViewModel @Inject constructor(private val repo: GuideRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    private val _platforms = MutableStateFlow<List<GuidePlatformEntity>>(emptyList())
    val platforms: StateFlow<List<GuidePlatformEntity>> = _platforms
    private val _hits = MutableStateFlow<List<GuideSearchHit>>(emptyList())
    val hits: StateFlow<List<GuideSearchHit>> = _hits

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
            _platforms.value = repo.platforms()
        }
    }

    fun onQuery(value: String) {
        _query.value = value
        viewModelScope.launch { _hits.value = repo.search(value) }
    }
}

@HiltViewModel
class GuideDetailViewModel @Inject constructor(
    repo: GuideRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val _detail = MutableStateFlow<GuideDetail?>(null)
    val detail: StateFlow<GuideDetail?> = _detail

    init {
        val id = savedState.get<String>("platformId").orEmpty()
        viewModelScope.launch {
            repo.ensureSeeded()
            _detail.value = repo.detail(id)
        }
    }
}

@Composable
fun GuidesScreen(onBack: () -> Unit, onOpen: (String) -> Unit, vm: GuidesViewModel = hiltViewModel()) {
    val query by vm.query.collectAsState()
    val platforms by vm.platforms.collectAsState()
    val hits by vm.hits.collectAsState()
    SafeScaffold(title = stringResource(R.string.guides), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(stringResource(R.string.guides_offline_note), style = MaterialTheme.typography.bodyMedium)
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::onQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.guides_search)) },
                    singleLine = true,
                )
            }
            if (query.trim().length >= 2) {
                if (hits.isEmpty()) {
                    item { Text(stringResource(R.string.guides_empty)) }
                } else {
                    items(hits, key = { it.platformId + it.sectionType + it.title }) { hit ->
                        Card(onClick = { onOpen(hit.platformId) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(hit.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${hit.platformId.replaceFirstChar { it.uppercase() }} · ${stringResource(sectionLabel(hit.sectionType))}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(hit.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            } else {
                items(platforms, key = { it.id }) { platform ->
                    Card(onClick = { onOpen(platform.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(platform.name, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuideDetailScreen(onBack: () -> Unit, vm: GuideDetailViewModel = hiltViewModel()) {
    val detail by vm.detail.collectAsState()
    SafeScaffold(title = detail?.name ?: stringResource(R.string.guides), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(stringResource(R.string.guides_offline_note), style = MaterialTheme.typography.bodyMedium)
            }
            detail?.sections?.forEach { section ->
                item(key = section.type) {
                    Text(stringResource(sectionLabel(section.type)), style = MaterialTheme.typography.titleLarge)
                }
                items(section.steps, key = { section.type + it.title }) { step ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(step.title, style = MaterialTheme.typography.titleMedium)
                            Text(step.body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@StringRes
private fun sectionLabel(type: String): Int = when (type) {
    "PROTECT" -> R.string.section_protect
    "WARNING_SIGNS" -> R.string.section_warning
    "RECOVER" -> R.string.section_recover
    else -> R.string.guides
}
