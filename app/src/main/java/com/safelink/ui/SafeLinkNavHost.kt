package com.safelink.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safelink.gateway.BrowserForwarder
import com.safelink.ui.guides.GuideDetailScreen
import com.safelink.ui.guides.GuidesScreen
import com.safelink.ui.history.HistoryScreen
import com.safelink.ui.home.HomeScreen
import com.safelink.ui.onboarding.OnboardingScreen
import com.safelink.ui.report.ReportDialog
import com.safelink.ui.report.ReportScreen
import com.safelink.ui.result.ResultHost
import com.safelink.ui.scan.ScanViewModel
import com.safelink.ui.settings.PrivacyScreen
import com.safelink.ui.settings.SettingsScreen
import com.safelink.ui.theme.SafeLinkTheme
import androidx.hilt.navigation.compose.hiltViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val GUIDES = "guides"
    const val GUIDE = "guides/{platformId}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
    const val REPORT = "report?url={url}"
    const val SCAN = "scan?url={url}"

    fun guide(id: String) = "guides/$id"
    fun report(url: String) = "report?url=${Uri.encode(url)}"
    fun scan(url: String) = "scan?url=${Uri.encode(url)}"
}

@Composable
fun SafeLinkNavHost(startDestination: String) {
    SafeLinkTheme {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = startDestination) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onDone = {
                    nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onGuides = { nav.navigate(Routes.GUIDES) },
                    onHistory = { nav.navigate(Routes.HISTORY) },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                    onScan = { nav.navigate(Routes.scan(it)) },
                )
            }
            composable(Routes.GUIDES) {
                GuidesScreen(onBack = { nav.popBackStack() }, onOpen = { nav.navigate(Routes.guide(it)) })
            }
            composable(
                route = Routes.GUIDE,
                arguments = listOf(navArgument("platformId") { type = NavType.StringType }),
            ) {
                GuideDetailScreen(onBack = { nav.popBackStack() })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    onBack = { nav.popBackStack() },
                    onReport = { nav.navigate(Routes.report(it)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { nav.popBackStack() }, onPrivacy = { nav.navigate(Routes.PRIVACY) })
            }
            composable(Routes.PRIVACY) { PrivacyScreen(onBack = { nav.popBackStack() }) }
            composable(
                route = Routes.REPORT,
                arguments = listOf(navArgument("url") { type = NavType.StringType; defaultValue = "" }),
            ) { entry ->
                ReportScreen(
                    initialUrl = entry.arguments?.getString("url").orEmpty(),
                    onBack = { nav.popBackStack() },
                )
            }
            composable(
                route = Routes.SCAN,
                arguments = listOf(navArgument("url") { type = NavType.StringType; defaultValue = "" }),
            ) { entry ->
                val url = entry.arguments?.getString("url").orEmpty()
                ManualScanScreen(url = url, onExit = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun ManualScanScreen(url: String, onExit: () -> Unit, vm: ScanViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var reporting by remember { mutableStateOf(false) }
    LaunchedEffect(url) { vm.scan(url, sourceApp = null) }
    val state by vm.state.collectAsState()
    val prefs by vm.alertPrefs.collectAsState()
    ResultHost(
        state = state,
        prefs = prefs,
        onForward = { BrowserForwarder.forward(context, url) },
        onExit = onExit,
        onReport = { reporting = true },
    )
    if (reporting) ReportDialog(initialUrl = url, onDismiss = { reporting = false })
}
