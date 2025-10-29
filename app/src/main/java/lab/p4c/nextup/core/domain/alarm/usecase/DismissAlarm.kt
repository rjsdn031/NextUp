package lab.p4c.nextup.core.domain.alarm.usecase

import javax.inject.Inject
import lab.p4c.nextup.core.domain.alarm.port.AlarmRepository
import lab.p4c.nextup.core.domain.alarm.port.AlarmScheduler

class DismissAlarm @Inject constructor(
    private val repo: AlarmRepository,
    private val scheduler: AlarmScheduler
) {
    /**
     * 알람이 실제로 울린 뒤 사용자가 Dismiss를 눌렀을 때 호출.
     * - 일회성 알람: enabled=false 로 저장
     * - 반복 알람: 상태 유지, 다음 트리거를 재계산해 스케줄
     */
    suspend operator fun invoke(alarmId: Int) {
        val a = repo.getById(alarmId) ?: return

        if (a.days.isEmpty()) {
            repo.setEnabled(alarmId, false)
            scheduler.cancel(alarmId)
            return
        }
    }
}
