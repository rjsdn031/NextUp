package lab.p4c.nextup.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lab.p4c.nextup.data.local.dao.AlarmDao
import lab.p4c.nextup.data.mapper.toDomain
import lab.p4c.nextup.data.mapper.toEntity
import lab.p4c.nextup.domain.model.Alarm
import lab.p4c.nextup.domain.repository.AlarmRepository
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
