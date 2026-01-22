package lab.p4c.nextup.feature.settings.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRemoteStore
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRepository
import lab.p4c.nextup.feature.settings.data.local.ExperimentInfoStore
import lab.p4c.nextup.feature.settings.infra.FirestoreExperimentInfoRemoteStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExperimentInfoModule {

    @Provides
    @Singleton
    fun provideExperimentInfoRepository(
        @ApplicationContext ctx: Context
    ): ExperimentInfoRepository = ExperimentInfoStore(ctx)

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideExperimentInfoRemoteStore(
        db: FirebaseFirestore
    ): ExperimentInfoRemoteStore =
        FirestoreExperimentInfoRemoteStore(db)
}