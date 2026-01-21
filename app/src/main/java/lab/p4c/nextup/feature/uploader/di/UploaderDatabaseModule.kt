package lab.p4c.nextup.feature.uploader.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.p4c.nextup.feature.uploader.data.local.UploadQueueDatabase
import lab.p4c.nextup.feature.uploader.data.local.dao.UploadTaskDao

@Module
@InstallIn(SingletonComponent::class)
object UploadDatabaseModule {

    @Provides
    @Singleton
    fun provideUploadQueueDatabase(
        @ApplicationContext context: Context
    ): UploadQueueDatabase {
        return Room.databaseBuilder(
            context, UploadQueueDatabase::class.java, "upload_queue.db"
        ).build()
    }

    @Provides
    fun provideUploadTaskDao(db: UploadQueueDatabase): UploadTaskDao = db.uploadTaskDao()
}
