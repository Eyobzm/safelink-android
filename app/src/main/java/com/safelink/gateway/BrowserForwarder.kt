package com.safelink.gateway

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * FR-INT-07/08. Targets a specific package so the forwarded intent cannot
 * match SafeLink's own filter again.
 */
object BrowserForwarder {
    fun forward(context: Context, url: String, preferredPackage: String? = null): Boolean {
        val uri = Uri.parse(url)
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            return forwardToOtherApp(context, uri)
        }
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        val browsers = context.packageManager
            .queryIntentActivities(probe, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != context.packageName }
            .distinct()
        val target = preferredPackage?.takeIf { it in browsers } ?: browsers.firstOrNull() ?: return false
        val intent = Intent(Intent.ACTION_VIEW, uri)
            .setPackage(target)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun forwardToOtherApp(context: Context, uri: Uri): Boolean {
        val probe = Intent(Intent.ACTION_VIEW, uri)
        val handler = context.packageManager
            .queryIntentActivities(probe, PackageManager.MATCH_ALL)
            .map { it.activityInfo }
            .firstOrNull { it.packageName != context.packageName }
            ?: return false
        val intent = Intent(Intent.ACTION_VIEW, uri)
            .setClassName(handler.packageName, handler.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
