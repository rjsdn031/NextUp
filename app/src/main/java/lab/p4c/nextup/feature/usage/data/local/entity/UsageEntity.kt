package lab.p4c.nextup.feature.usage.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_session",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["packageName"]),
        Index(value = ["startMillis"])
    ]
)
data class UsageEntity(
    @PrimaryKey
    val id: String,

    val dateKey: String,
    val packageName: String,

    val startMillis: Long,
    val endMillis: Long,
    val durationMillis: Long,

    val createdAtMillis: Long
) {
    companion object {
        fun buildId(packageName: String, startMillis: Long, endMillis: Long): String {
            return "$packageName-$startMillis-$endMillis"
        }
    }
}