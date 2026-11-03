package com.safelink.gateway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.safelink.ui.result.ResultHost
import com.safelink.ui.scan.ScanViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LinkInterceptActivity : ComponentActivity() {
    private val vm: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = UrlExtractor.from(intent)
        if (url == null) { finish(); return }
        if (savedInstanceState == null) vm.scan(url, SourceAppResolver.resolve(this))

        setContent {
            val state by vm.state.collectAsState()
            ResultHost(
                state = state,
                onForward = { BrowserForwarder.forward(this, url); finish() },
                onExit = { finish() },
                onReport = { /* open ReportActivity / sheet (FR-FB-01) */ },
            )
        }
    }
}
