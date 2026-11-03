package com.safelink.gateway

import android.app.Activity
import android.net.Uri
import android.os.Build

/** FR-INT-05: best-effort. Android often hides the caller; null means unknown. */
object SourceAppResolver {
    fun resolve(activity: Activity): String? {
        val pkg = activity.callingPackage
        if (pkg != null) return pkg
        if (Build.VERSION.SDK_INT >= 22) {
            val ref: Uri? = activity.referrer
            if (ref != null && ref.scheme == "android-app") return ref.host
        }
        return null
    }
}
