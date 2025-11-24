package lab.p4c.nextup.feature.settings.ui.experiment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lab.p4c.nextup.core.domain.experiment.model.ExperimentInfo
import lab.p4c.nextup.core.domain.experiment.port.ExperimentInfoRepository

@HiltViewModel
class ExperimentInfoViewModel @Inject constructor(
    private val repo: ExperimentInfoRepository
) : ViewModel() {

    private val _info = MutableStateFlow<ExperimentInfo?>(null)
    val info: StateFlow<ExperimentInfo?> = _info

    init {
        viewModelScope.launch {
            _info.value = repo.get()
        }
    }

    /**
     * name: 필수
     * age: 필수 숫자
     * gender: 필수 선택
     */
    fun save(name: String, age: String, gender: String) {
        viewModelScope.launch {

            // --- Validation ---
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) return@launch

            val parsedAge = age.toIntOrNull() ?: return@launch
            if (gender.isBlank()) return@launch

            // --- Domain 객체 생성 ---
            val info = ExperimentInfo(
                name = trimmedName,
                age = parsedAge,
                gender = gender
            )

            repo.save(info)
            _info.value = info
        }
    }
}
