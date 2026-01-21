package lab.p4c.nextup.feature.usage.infra.upload

import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.feature.usage.data.local.entity.UsageEntity

@Singleton
class UsageNdjsonCodec @Inject constructor() {

    /**
     * NDJSON은 "한 줄 = 한 JSON object"
     */
    fun encodeLine(e: UsageEntity): String {
        val obj = JSONObject()
            .put("dateKey", e.dateKey)
            .put("packageName", e.packageName)
            .put("startMillis", e.startMillis)
            .put("endMillis", e.endMillis)
            .put("durationMillis", e.durationMillis)
            .put("createdAtMillis", e.createdAtMillis)

        return obj.toString()
    }
}
