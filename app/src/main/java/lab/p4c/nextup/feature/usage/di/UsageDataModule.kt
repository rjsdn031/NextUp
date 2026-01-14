package lab.p4c.nextup.feature.usage.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.p4c.nextup.feature.usage.data.local.UsageDatabase
import lab.p4c.nextup.feature.usage.data.local.dao.UsageDao
import lab.p4c.nextup.feature.usage.data.repository.UsageRepository

@Module
@InstallIn(SingletonComponent::class)
object UsageDataModule {

    @Provides
    @Singleton
    fun provideUsageDatabase(
        @ApplicationContext context: Context
    ): UsageDatabase {
        return Room.databaseBuilder(
            context,
            UsageDatabase::class.java,
            "usage.db"
        )
            .fallbackToDestructiveMigration() // 지금 version=1이면 OK. 나중에 migration 붙이면 제거.
            .build()
    }

    @Provides
    fun provideUsageDao(db: UsageDatabase): UsageDao = db.usageDao()

    @Provides
    @Singleton
    fun provideUsageRepository(dao: UsageDao): UsageRepository {
        return UsageRepository(dao)
    }
}
