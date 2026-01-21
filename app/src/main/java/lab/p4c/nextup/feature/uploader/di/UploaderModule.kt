package lab.p4c.nextup.feature.uploader.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import lab.p4c.nextup.feature.uploader.infra.runner.UploadHandler
import lab.p4c.nextup.feature.usage.infra.upload.UsageUploadHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UploadModule {

    @Binds
    @IntoSet
    @Singleton
    abstract fun bindUsageUploadHandler(impl: UsageUploadHandler): UploadHandler

    // SurveyUploadHandler, TelemetryUploadHandler 만들면 똑같이 @IntoSet 추가
}
