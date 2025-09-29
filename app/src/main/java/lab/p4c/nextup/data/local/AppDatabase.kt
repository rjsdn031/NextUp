package lab.p4c.nextup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import lab.p4c.nextup.data.local.dao.AlarmDao
import lab.p4c.nextup.data.local.entity.AlarmEntity

@Database(entities = [AlarmEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
