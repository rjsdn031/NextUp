package lab.p4c.nextup.feature.usage.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.feature.usage.infra.UsageStatsService

@Module
@InstallIn(SingletonComponent::class)
object UsageModule {

    @Provides
    fun provideUsageStatsService(): UsageStatsService = UsageStatsService
}
