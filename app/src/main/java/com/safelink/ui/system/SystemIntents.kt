package com.safelink.ui.system

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object SystemIntents {
    fun openLinkHandlerSettings(context: Context) {
        val pkg = Uri.parse("package:${context.packageName}")
        val primary = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS).setData(pkg)
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(pkg)
        for (intent in listOf(primary, fallback)) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: ActivityNotFoundException) {
                continue
            }
        }
    }

    fun uninstall(context: Context) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun clipboardText(context: Context): String? {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = cm.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).coerceToText(context)?.toString()?.trim()?.ifBlank { null }
    }
}

fun looksLikeLink(text: String): Boolean {
    val t = text.trim().lowercase()
    return t.startsWith("http://") || t.startsWith("https://") ||
        t.startsWith("tg://") || t.startsWith("whatsapp://")
}
