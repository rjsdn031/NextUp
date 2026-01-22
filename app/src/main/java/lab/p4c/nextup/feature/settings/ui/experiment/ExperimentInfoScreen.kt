package lab.p4c.nextup.feature.settings.ui.experiment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import lab.p4c.nextup.app.ui.components.ThrottleButton
import lab.p4c.nextup.app.ui.components.ThrottleIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentInfoScreen(
    navController: NavController,
    vm: ExperimentInfoViewModel = hiltViewModel()
) {
    val info by vm.info.collectAsState()
    val uiState by vm.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // 저장 후에는 기본적으로 읽기 전용, "수정" 버튼을 눌렀을 때만 편집 모드
    var isEditing by remember { mutableStateOf(true) }
    var hasInitialized by remember { mutableStateOf(false) }

    val hasSaved = info != null
    val isBusy = uiState.isSaving

    val isFormValid = name.trim().isNotEmpty() &&
            age.toIntOrNull() != null &&
            gender.isNotBlank()

    // 최초 1회 + 저장 완료 후(새 info 반영)에는 폼을 저장값으로 동기화하고 읽기 전용으로 전환
    LaunchedEffect(info) {
        if (!hasInitialized) {
            hasInitialized = true
        }
        info?.let {
            name = it.name
            age = it.age.toString()
            gender = it.gender
            isEditing = false
        } ?: run {
            // 아직 저장된 값이 없으면 입력 가능
            isEditing = true
        }
    }

    val canEdit = isEditing && !isBusy

    val primaryButtonText = when {
        !hasSaved -> "저장"
        isEditing -> "저장"
        else -> "수정"
    }

    val primaryButtonEnabled = when {
        isBusy -> false
        hasSaved && !isEditing -> true
        else -> isFormValid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("실험 정보 입력") },
                navigationIcon = {
                    ThrottleIconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                enabled = canEdit,
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                enabled = canEdit,
                label = { Text("나이") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GenderButton(
                    text = "남성",
                    selected = gender == "남성",
                    onClick = { gender = "남성" },
                    modifier = Modifier.weight(1f),
                    enabled = canEdit
                )

                GenderButton(
                    text = "여성",
                    selected = gender == "여성",
                    onClick = { gender = "여성" },
                    modifier = Modifier.weight(1f),
                    enabled = canEdit
                )

                GenderButton(
                    text = "기타",
                    selected = gender == "기타",
                    onClick = { gender = "기타" },
                    modifier = Modifier.weight(1f),
                    enabled = canEdit
                )
            }

            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }

            ThrottleButton(
                onClick = {
                    if (hasSaved && !isEditing) {
                        isEditing = true
                    } else {
                        vm.save(name, age, gender)
                    }
                },
                enabled = primaryButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isBusy) "저장 중..." else primaryButtonText)
            }
        }
    }
}
