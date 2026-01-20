package lab.p4c.nextup.feature.usage.data.repository

import lab.p4c.nextup.core.domain.system.dateKeyFromUtcEpochMillis
import lab.p4c.nextup.feature.usage.data.local.dao.UsageDao
import lab.p4c.nextup.feature.usage.data.local.entity.UsageEntity
import lab.p4c.nextup.feature.usage.ui.model.UsageSession
import javax.inject.Inject

class UsageRepository @Inject constructor(
    private val usageDao: UsageDao
) {

    suspend fun saveSessions(sessions: List<UsageSessionInput>) {
        val now = System.currentTimeMillis()

        val entities = sessions.map { s ->
            val end = s.endMillis
            val start = s.startMillis
            val pkg = s.packageName

            UsageEntity(
                id = UsageEntity.buildId(pkg, start, end),
                dateKey = dateKeyFromUtcEpochMillis(start),
                packageName = pkg,
                startMillis = start,
                endMillis = end,
                durationMillis = (end - start).coerceAtLeast(0L),
                createdAtMillis = now
            )
        }

        usageDao.insertAll(entities)
    }

    suspend fun getSessionsByWindow(
        packageName: String,
        startMs: Long,
        endMs: Long
    ): List<UsageSession> {
        return usageDao.getSessionsByWindow(
            packageName = packageName,
            startMs = startMs,
            endMs = endMs
        ).map { e ->
            UsageSession(
                startMillis = e.startMillis,
                endMillis = e.endMillis,
                packageName = e.packageName
            )
        }
    }

    suspend fun deleteByDateKey(dateKey: String): Int {
        return usageDao.deleteByDateKey(dateKey)
    }

    suspend fun deleteBefore(dateKey: String) {
        usageDao.deleteBefore(dateKey)
    }

    suspend fun getEntitiesByDateKey(dateKey: String): List<UsageEntity> {
        return usageDao.getSessionsByDateKey(dateKey)
    }
}

data class UsageSessionInput(
    val packageName: String,
    val startMillis: Long,
    val endMillis: Long
)
