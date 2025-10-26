package lab.p4c.nextup.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.domain.alarm.policy.HolidayProvider
import lab.p4c.nextup.core.domain.alarm.policy.NoopHolidayProvider
import lab.p4c.nextup.core.domain.alarm.service.NextTriggerCalculator
import java.time.Clock
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Module
@InstallIn(SingletonComponent::class)
object AlarmDomainModule {

    @Provides @Singleton
    fun provideHolidayProvider(): HolidayProvider = NoopHolidayProvider()

    @OptIn(ExperimentalTime::class)
    @Provides @Singleton
    fun provideNextTriggerCalculator(
        clock: Clock,
        holidayProvider: HolidayProvider
    ): NextTriggerCalculator = NextTriggerCalculator(clock, holidayProvider)
}