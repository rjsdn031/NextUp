package lab.p4c.nextup.feature.alarm.infra.scheduler

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.feature.blocking.infra.BlockGate

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmReceiverEntryPoint {
    fun blockGate(): BlockGate
}