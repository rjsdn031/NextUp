package lab.p4c.nextup.core.domain.alarm.port

import kotlinx.coroutines.flow.Flow
import lab.p4c.nextup.core.domain.alarm.model.Alarm

interface AlarmRepository {
    fun observe(): Flow<List<Alarm>>
    suspend fun getAll(): List<Alarm>
    suspend fun upsert(alarm: Alarm)
    suspend fun delete(id: Int)
    suspend fun getById(id: Int): Alarm?
    suspend fun  setEnabled(id: Int, enabled: Boolean): Boolean
}