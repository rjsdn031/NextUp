package lab.p4c.nextup.feature.alarm.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lab.p4c.nextup.feature.alarm.data.local.dao.AlarmDao
import lab.p4c.nextup.feature.alarm.data.local.mapper.toDomain
import lab.p4c.nextup.feature.alarm.data.local.mapper.toEntity
import lab.p4c.nextup.core.domain.alarm.model.Alarm
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao
) : AlarmRepository {

    override fun observe(): Flow<List<Alarm>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(alarm: Alarm) {
        val entity = alarm.toEntity()
        if (entity.id == 0) {
            dao.insert(entity)
        } else {
            val rows = dao.update(entity)
            if (rows == 0) dao.insert(entity)
        }
    }

    override suspend fun delete(id: Int) {
        dao.deleteById(id)
    }

    override suspend fun getById(id: Int): Alarm? =
        dao.findByIdOrNull(id)?.toDomain()
}
