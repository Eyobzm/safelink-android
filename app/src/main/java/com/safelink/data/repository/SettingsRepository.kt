package com.safelink.data.repository

import com.safelink.data.local.dao.SettingsDao
import com.safelink.data.local.entity.AllowListEntity
import com.safelink.data.local.entity.SettingEntity
import com.safelink.engine.AllowListMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

data class AlertPrefs(val sound: Boolean = true, val haptics: Boolean = true, val volumeCap: Int = 100)

data class SettingsUi(
    val alarmSound: Boolean = true,
    val haptics: Boolean = true,
    val volumeCap: Int = 100,
    val paused: Boolean = false,
    val allowList: List<String> = emptyList(),
)

object Prefs {
    const val ONBOARDING_DONE = "onboarding_done"
    const val ALARM_SOUND = "alarm_sound"
    const val HAPTICS = "haptics"
    const val VOLUME_CAP = "volume_cap"
    const val PROTECTION_PAUSED = "protection_paused"
}

@Singleton
class SettingsRepository @Inject constructor(private val dao: SettingsDao) {
    fun observe(): Flow<SettingsUi> = combine(dao.observe(), dao.observeAllow()) { rows, allow ->
        val map = rows.associate { it.key to it.value }
        SettingsUi(
            alarmSound = map[Prefs.ALARM_SOUND] != "false",
            haptics = map[Prefs.HAPTICS] != "false",
            volumeCap = map[Prefs.VOLUME_CAP]?.toIntOrNull()?.coerceIn(0, 100) ?: 100,
            paused = map[Prefs.PROTECTION_PAUSED] == "true",
            allowList = allow.map { it.domain },
        )
    }

    suspend fun isOnboardingDone(): Boolean = dao.get(Prefs.ONBOARDING_DONE) == "true"

    suspend fun setOnboardingDone() = put(Prefs.ONBOARDING_DONE, "true")

    suspend fun isPaused(): Boolean = dao.get(Prefs.PROTECTION_PAUSED) == "true"

    suspend fun alertPrefs(): AlertPrefs {
        val cap = dao.get(Prefs.VOLUME_CAP)?.toIntOrNull()?.coerceIn(0, 100) ?: 100
        return AlertPrefs(
            sound = dao.get(Prefs.ALARM_SOUND) != "false" && cap > 0,
            haptics = dao.get(Prefs.HAPTICS) != "false",
            volumeCap = cap,
        )
    }

    suspend fun setBool(key: String, value: Boolean) = put(key, if (value) "true" else "false")

    suspend fun setVolumeCap(percent: Int) = put(Prefs.VOLUME_CAP, percent.coerceIn(0, 100).toString())

    suspend fun addAllowedDomain(raw: String): Boolean {
        val domain = AllowListMatcher.normalizeDomain(raw) ?: return false
        dao.allow(AllowListEntity(domain))
        return true
    }

    suspend fun removeAllowedDomain(domain: String) = dao.removeAllow(domain)

    suspend fun allowedDomains(): List<String> = dao.allowList().map { it.domain }

    private suspend fun put(key: String, value: String) = dao.put(SettingEntity(key, value))
}
