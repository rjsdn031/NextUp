package lab.p4c.nextup.feature.usage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import lab.p4c.nextup.feature.usage.data.local.dao.UsageDao
import lab.p4c.nextup.feature.usage.data.local.entity.UsageEntity

@Database(
    entities = [UsageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class UsageDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
}
