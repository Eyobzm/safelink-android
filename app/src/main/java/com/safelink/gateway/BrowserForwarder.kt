package com.safelink.gateway

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * FR-INT-07/08. Targets a specific browser package so the forwarded intent can never
 * re-trigger our own intent filter (no redirect loop). Spike this on real devices first.
 */
object BrowserForwarder {
    fun forward(context: Context, url: String, preferredPackage: String? = null): Boolean {
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")).addCategory(Intent.CATEGORY_BROWSABLE)
        val browsers = context.packageManager
            .queryIntentActivities(probe, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != context.packageName }
            .distinct()
        val target = preferredPackage?.takeIf { it in browsers } ?: browsers.firstOrNull() ?: return false

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)) // original URL, unchanged
            .setPackage(target)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }
}
