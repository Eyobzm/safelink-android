package com.safelink.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.safelink.data.local.AppDatabase
import com.safelink.data.local.dao.GuideDao
import com.safelink.data.local.dao.GuideSearchHit
import com.safelink.data.local.entity.GuidePlatformEntity
import com.safelink.data.local.entity.GuideSectionEntity
import com.safelink.data.local.entity.GuideStepEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class GuideDetail(
    val id: String,
    val name: String,
    val sections: List<GuideSectionBlock>,
)

data class GuideSectionBlock(
    val type: String,
    val steps: List<GuideStepEntity>,
)

@Singleton
class GuideRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val dao: GuideDao,
) {
    suspend fun ensureSeeded() {
        if (dao.platforms().isNotEmpty()) return
        val text = context.assets.open("guides_seed.json").bufferedReader().use { it.readText() }
        val platforms = JSONObject(text).getJSONArray("platforms")
        db.withTransaction {
            if (dao.platforms().isNotEmpty()) return@withTransaction
            for (i in 0 until platforms.length()) {
                val platform = platforms.getJSONObject(i)
                val id = platform.getString("id")
                dao.insertPlatform(GuidePlatformEntity(id, platform.getString("name")))
                val sections = platform.getJSONArray("sections")
                for (s in 0 until sections.length()) {
                    val section = sections.getJSONObject(s)
                    val sectionId = dao.insertSection(
                        GuideSectionEntity(platformId = id, type = section.getString("type"), sortOrder = s)
                    )
                    val steps = section.getJSONArray("steps")
                    for (t in 0 until steps.length()) {
                        val step = steps.getJSONObject(t)
                        dao.insertStep(
                            GuideStepEntity(
                                sectionId = sectionId,
                                title = step.getString("title"),
                                body = step.getString("body"),
                                sortOrder = t,
                                verifiedOn = step.optString("verified_on").ifBlank { null },
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun platforms() = dao.platforms().sortedBy { it.name }

    suspend fun detail(platformId: String): GuideDetail? {
        val platform = dao.platforms().firstOrNull { it.id == platformId } ?: return null
        val sections = dao.sections(platformId).map { section ->
            GuideSectionBlock(section.type, dao.steps(section.id))
        }
        return GuideDetail(platform.id, platform.name, sections)
    }

    suspend fun search(query: String): List<GuideSearchHit> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        return dao.searchHits(q)
    }
}
