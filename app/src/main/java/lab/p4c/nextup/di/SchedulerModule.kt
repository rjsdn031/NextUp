package lab.p4c.nextup.di

import android.app.AlarmManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.p4c.nextup.data.scheduler.AlarmScheduler
import lab.p4c.nextup.data.scheduler.AndroidAlarmScheduler

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerBindModule {
    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AndroidAlarmScheduler): AlarmScheduler
}

@Module
@InstallIn(SingletonComponent::class)
object SchedulerProvideModule {
    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext ctx: Context): AlarmManager =
        ctx.getSystemService(AlarmManager::class.java)
}