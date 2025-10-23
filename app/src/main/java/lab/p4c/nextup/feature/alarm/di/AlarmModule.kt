package lab.p4c.nextup.feature.alarm.di

import android.app.AlarmManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler
import lab.p4c.nextup.feature.alarm.infra.scheduler.AndroidAlarmScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmBindModule {
    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AndroidAlarmScheduler): AlarmScheduler
}

@Module
@InstallIn(SingletonComponent::class)
object AlarmProvideModule {
    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext ctx: Context): AlarmManager =
        ctx.getSystemService(AlarmManager::class.java)
}