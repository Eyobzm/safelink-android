package com.safelink.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ScanRequestDto(
    val url: String,
    val source_app: String?,
    val client_version: String,
    val local_flags: List<String>,
)

data class ScanResponseDto(
    val verdict: String,
    val score: Int,
    val reasons: List<String>,
    val message_key: String,
    val engines: Map<String, String>,
    val degraded: Boolean,
    val latency_ms: Int,
)

data class ReportDto(val url: String, val report_type: String, val note: String?)

data class RuleDeltaDto(
    val version: Int = 0,
    @SerializedName("package") val packageB64: String? = null,
    val signature: String? = null,
)

interface SafeLinkApi {
    @POST("v1/scan") suspend fun scan(@Body body: ScanRequestDto): ScanResponseDto
    @POST("v1/report") suspend fun report(@Body body: ReportDto)
    @GET("v1/rules/delta") suspend fun rulesDelta(@Query("since") since: Int): RuleDeltaDto
}
