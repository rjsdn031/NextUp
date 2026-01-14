package lab.p4c.nextup.feature.usage.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.feature.usage.infra.DefaultUsageStatsService
import lab.p4c.nextup.feature.usage.infra.UsageStatsService

@Module
@InstallIn(SingletonComponent::class)
abstract class UsageModule {

    @Binds
    abstract fun bindUsageStatsService(
        impl: DefaultUsageStatsService
    ): UsageStatsService
}
