package lab.p4c.nextup.feature.blocking.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.blocking.port.BlockTargetRepository
import lab.p4c.nextup.feature.blocking.data.BlockTargetRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class BlockingModule {

    @Binds
    @Singleton
    abstract fun bindBlockTargetRepository(
        impl: BlockTargetRepositoryImpl
    ): BlockTargetRepository
}