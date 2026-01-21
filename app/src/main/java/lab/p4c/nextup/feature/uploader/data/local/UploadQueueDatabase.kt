package lab.p4c.nextup.feature.uploader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import lab.p4c.nextup.feature.uploader.data.local.dao.UploadTaskDao
import lab.p4c.nextup.feature.uploader.data.local.entity.UploadTaskEntity

@Database(
    entities = [UploadTaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class UploadQueueDatabase : RoomDatabase() {
    abstract fun uploadTaskDao(): UploadTaskDao
}
