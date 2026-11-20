package com.safelink.gateway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.safelink.ui.report.ReportDialog
import com.safelink.ui.result.ResultHost
import com.safelink.ui.scan.ScanViewModel
import com.safelink.ui.theme.SafeLinkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LinkInterceptActivity : ComponentActivity() {
    private val vm: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = UrlExtractor.from(intent)
        if (url == null) {
            finish()
            return
        }
        if (savedInstanceState == null) vm.scan(url, SourceAppResolver.resolve(this))

        setContent {
            SafeLinkTheme {
                val state by vm.state.collectAsState()
                val prefs by vm.alertPrefs.collectAsState()
                var reporting by remember { mutableStateOf(false) }
                ResultHost(
                    state = state,
                    prefs = prefs,
                    onForward = { BrowserForwarder.forward(this, url) },
                    onExit = { finish() },
                    onReport = { reporting = true },
                )
                if (reporting) ReportDialog(initialUrl = url, onDismiss = { reporting = false })
            }
        }
    }
}
