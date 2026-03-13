package lab.p4c.nextup.core.domain.survey.usecase

import java.time.ZoneId
import javax.inject.Inject
import lab.p4c.nextup.core.domain.survey.port.SurveyRepository
import lab.p4c.nextup.core.domain.system.TimeProvider

/**
 * 디버그 환경에서 오늘 설문 데이터를 제거한다.
 *
 * 전달된 [zone] 기준 오늘 날짜의 survey를 Room에서 삭제한다.
 */
class DebugClearTodaySurvey @Inject constructor(
    private val surveyRepository: SurveyRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val todayKey = timeProvider.now()
            .atZone(zone)
            .toLocalDate()
            .toString()

        surveyRepository.deleteByDate(todayKey)
        return todayKey
    }
}