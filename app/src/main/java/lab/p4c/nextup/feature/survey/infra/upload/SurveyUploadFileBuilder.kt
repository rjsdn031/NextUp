package lab.p4c.nextup.feature.survey.infra.upload

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.feature.survey.data.local.dao.SurveyDao

@Singleton
class SurveyUploadFileBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: SurveyDao
) {
    suspend fun build(dateKey: String): File {
        val e = dao.getByDate(dateKey)
            ?: error("Survey not found for dateKey=$dateKey")

        val json = buildJson(e)

        val out = File(context.cacheDir, "survey_$dateKey.jsonl")
        out.writeText(json + "\n")
        return out
    }

    private fun buildJson(e: lab.p4c.nextup.feature.survey.data.local.entity.SurveyEntity): String {
        return buildString {
            append('{')

            append("\"dateKey\":\"").append(e.dateKey).append("\",")

            append("\"missedYesterdayReason\":")
            append(jsonStringOrNull(e.missedYesterdayReason)).append(',')

            append("\"sleepStartTime\":\"").append(e.sleepStartTime).append("\",")
            append("\"sleepEndTime\":\"").append(e.sleepEndTime).append("\",")

            append("\"sleepQualityScore\":").append(e.sleepQualityScore).append(',')

            append("\"productivityScore\":").append(e.productivityScore).append(',')
            append("\"productivityReason\":\"")
                .append(escape(e.productivityReason)).append("\",")

            append("\"goalAchievement\":").append(e.goalAchievement).append(',')
            append("\"nextGoal\":\"").append(escape(e.nextGoal)).append('"')

            append('}')
        }
    }

    private fun jsonStringOrNull(value: String?): String =
        value?.let { "\"${escape(it)}\"" } ?: "null"

    private fun escape(s: String): String =
        s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
}
