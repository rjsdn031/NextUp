package lab.p4c.nextup.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import lab.p4c.nextup.domain.usecase.HolidayProvider
import lab.p4c.nextup.domain.usecase.NoopHolidayProvider

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides @Singleton
    fun provideHolidayProvider(): HolidayProvider = NoopHolidayProvider()
}
