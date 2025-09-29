package lab.p4c.nextup.domain.repository

import kotlinx.coroutines.flow.Flow
import lab.p4c.nextup.domain.model.Alarm

interface AlarmRepository {
    fun observe(): Flow<List<Alarm>>
    suspend fun upsert(alarm: Alarm)
    suspend fun delete(id: Int)
    suspend fun getById(id: Int): Alarm?
}