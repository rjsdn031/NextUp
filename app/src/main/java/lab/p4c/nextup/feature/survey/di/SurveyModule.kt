package lab.p4c.nextup.feature.survey.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.feature.survey.data.local.SurveyDatabase
import lab.p4c.nextup.feature.survey.data.repository.SurveyRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SurveyModule {

    @Provides
    @Singleton
    fun provideSurveyDatabase(@ApplicationContext ctx: Context): SurveyDatabase =
        Room.databaseBuilder(ctx, SurveyDatabase::class.java, "survey.db").build()

    @Provides
    @Singleton
    fun bindSurveyRepository(impl: SurveyRepositoryImpl): SurveyRepository = impl
}
