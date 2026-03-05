package lab.p4c.nextup.feature.settings.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.p4c.nextup.feature.settings.infra.AndroidDebugUploadTrigger
import lab.p4c.nextup.feature.settings.infra.DebugUploadTrigger

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsDebugModule {

    @Binds
    @Singleton
    abstract fun bindDebugUploadTrigger(
        impl: AndroidDebugUploadTrigger
    ): DebugUploadTrigger
}