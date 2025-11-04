package lab.p4c.nextup.feature.survey.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.domain.survey.port.SurveyReminderScheduler
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.system.TimeProvider
import lab.p4c.nextup.feature.survey.data.local.SurveyDatabase
import lab.p4c.nextup.feature.survey.data.local.dao.SurveyDao
import lab.p4c.nextup.feature.survey.data.repository.SurveyRepositoryImpl
import lab.p4c.nextup.feature.survey.infra.notifier.SurveyNotifier
import lab.p4c.nextup.feature.survey.infra.scheduler.AndroidSurveyReminderScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SurveyProvideModule {

    @Provides @Singleton
    fun provideSurveyDatabase(@ApplicationContext ctx: Context): SurveyDatabase =
        Room.databaseBuilder(ctx, SurveyDatabase::class.java, "survey.db").build()

    @Provides
    fun provideSurveyDao(db: SurveyDatabase): SurveyDao = db.surveyDao()

    @Provides @Singleton
    fun provideSurveyNotifier(@ApplicationContext ctx: Context): SurveyNotifier =
        SurveyNotifier(ctx)

    @Provides @Singleton
    fun provideSurveyReminderScheduler(
        @ApplicationContext ctx: Context,
        timeProvider: TimeProvider
    ): SurveyReminderScheduler =
        AndroidSurveyReminderScheduler(ctx)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SurveyBindModule {
    @Binds
    @Singleton
    abstract fun bindSurveyRepository(impl: SurveyRepositoryImpl): SurveyRepository
}