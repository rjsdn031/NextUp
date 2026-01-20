package lab.p4c.nextup.feature.usage.infra.upload

import lab.p4c.nextup.feature.usage.data.local.entity.UsageEntity

object UsageNdjsonCodec {
    /**
     * 한 줄 = 한 JSON 오브젝트 (NDJSON)
     */
    fun toNdjsonLine(e: UsageEntity): String {
        fun esc(s: String): String =
            s.replace("\\", "\\\\").replace("\"", "\\\"")

        return buildString {
            append('{')
            append("\"id\":\"").append(esc(e.id)).append("\",")
            append("\"dateKey\":\"").append(esc(e.dateKey)).append("\",")
            append("\"packageName\":\"").append(esc(e.packageName)).append("\",")
            append("\"startMillis\":").append(e.startMillis).append(',')
            append("\"endMillis\":").append(e.endMillis).append(',')
            append("\"durationMillis\":").append(e.durationMillis).append(',')
            append("\"createdAtMillis\":").append(e.createdAtMillis)
            append('}')
        }
    }
}
