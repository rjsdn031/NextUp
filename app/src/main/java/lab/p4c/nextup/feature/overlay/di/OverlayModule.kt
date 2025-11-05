package lab.p4c.nextup.feature.overlay.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.overlay.port.OverlayTargetRepository
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
