package com.safelink.data

/** Drops the query string and fragment so tokens are not uploaded (FR-FB-03). */
object UrlPrivacy {
    fun stripQuery(url: String): String = url.substringBefore('#').substringBefore('?')
}
