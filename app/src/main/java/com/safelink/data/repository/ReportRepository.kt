package com.safelink.data.repository

import com.safelink.data.UrlPrivacy
import com.safelink.data.remote.ReportDto
import com.safelink.data.remote.SafeLinkApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(private val api: SafeLinkApi) {
    suspend fun submit(url: String, type: String, note: String, stripQuery: Boolean): Boolean = try {
        val clean = if (stripQuery) UrlPrivacy.stripQuery(url.trim()) else url.trim()
        api.report(ReportDto(clean, type, note.trim().ifBlank { null }))
        true
    } catch (e: Exception) {
        false
    }
}
