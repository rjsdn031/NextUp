package lab.p4c.nextup.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.data.local.AppDatabase
import lab.p4c.nextup.data.repository.AlarmRepositoryImpl
import lab.p4c.nextup.domain.repository.AlarmRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "nextup.db").build()

    @Provides
    fun provideAlarmDao(db: AppDatabase) = db.alarmDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds @Singleton
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository
}
