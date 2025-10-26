package lab.p4c.nextup.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.app.time.SystemTimeProvider
import lab.p4c.nextup.core.domain.system.TimeProvider
import java.time.Clock
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.alarm.policy.HolidayProvider
import lab.p4c.nextup.core.domain.alarm.policy.NoopHolidayProvider

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideTimeProvider(clock: Clock): TimeProvider = SystemTimeProvider(clock)
}
