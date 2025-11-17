package lab.p4c.nextup.feature.overlay.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.domain.overlay.port.BlockTargetRepository
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.overlay.port.OverlayTargetRepository
import lab.p4c.nextup.feature.overlay.data.BlockTargetRepositoryImpl
import lab.p4c.nextup.feature.overlay.data.OverlayTargetRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class OverlayModule {

    @Binds
    @Singleton
    abstract fun bindOverlayTargetRepository(
        impl: OverlayTargetRepositoryImpl
    ): OverlayTargetRepository
}

@Module
@InstallIn(SingletonComponent::class)
object OverlayProvideModule {

    @Provides
    @Singleton
    fun provideBlockTargetRepository(
        impl: BlockTargetRepositoryImpl
    ): BlockTargetRepository = impl
}