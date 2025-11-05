package lab.p4c.nextup.feature.overlay.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import lab.p4c.nextup.core.domain.overlay.model.TargetSource
import lab.p4c.nextup.core.domain.overlay.port.OverlayTargetRepository

@Singleton
class OverlayTargetRepositoryImpl @Inject constructor(
    private val store: OverlayTargetStore
) : OverlayTargetRepository {

    private val defaultText = "나는 오늘 집중을 유지한다"

    // 필요 시 source 우선순위 정책을 여기에 추가
    override suspend fun setToday(text: String, source: TargetSource) {
        store.setToday(text)
    }

    override suspend fun getActiveOrDefault(): String =
        store.observeToday().first()?.takeIf { it.isNotBlank() } ?: defaultText

    override fun observeActive(): Flow<String> =
        store.observeToday().map { it?.takeIf(String::isNotBlank) ?: defaultText }
}
