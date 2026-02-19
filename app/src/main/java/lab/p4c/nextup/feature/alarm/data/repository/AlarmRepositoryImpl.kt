package lab.p4c.nextup.feature.alarm.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.feature.alarm.data.mapper.toDomain
import lab.p4c.nextup.feature.alarm.data.mapper.toEntity
import javax.inject.Inject

/**
 * Room-backed implementation of [AlarmRepository].
 *
 * Persistence rules such as deduplication are enforced atomically in [AlarmDao.upsertDedup].
 */
class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao
) : AlarmRepository {

    override fun observe(): Flow<List<Alarm>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<Alarm> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getEnabledAll(): List<Alarm> =
        dao.findEnabledAll().map { it.toDomain() }

    override suspend fun getById(id: Int): Alarm? =
        dao.findByIdOrNull(id)?.toDomain()

    override suspend fun delete(id: Int) {
        dao.deleteById(id)
    }

    override suspend fun setEnabled(id: Int, enabled: Boolean): Boolean =
        dao.setEnabled(id, enabled) > 0

    override suspend fun upsert(alarm: Alarm): AlarmRepository.UpsertResult {
        val incoming = alarm.toEntity()
        val result = dao.upsertDedup(incoming)

        val persisted = alarm.copy(id = result.id)
        return AlarmRepository.UpsertResult(
            alarm = persisted,
            created = result.created
        )
    }

    override suspend fun setEnabledAndGet(id: Int, enabled: Boolean): Alarm? =
        dao.setEnabledAndGet(id, enabled)?.toDomain()
}
