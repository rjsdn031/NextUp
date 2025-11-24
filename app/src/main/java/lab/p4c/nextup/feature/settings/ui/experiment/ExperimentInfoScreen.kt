package lab.p4c.nextup.feature.settings.ui.experiment


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import lab.p4c.nextup.app.ui.components.ThrottleIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentInfoScreen(navController: NavController, vm: ExperimentInfoViewModel = hiltViewModel()) {
    val info by vm.info.collectAsState()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    LaunchedEffect(info) {
        info?.let {
            name = it.name
            age = it.age.toString()
            gender = it.gender
        }
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
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
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
                    modifier = Modifier.weight(1f)
                )

                GenderButton(
                    text = "여성",
                    selected = gender == "여성",
                    onClick = { gender = "여성" },
                    modifier = Modifier.weight(1f)
                )

                GenderButton(
                    text = "기타",
                    selected = gender == "기타",
                    onClick = { gender = "기타" },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    vm.save(name, age, gender)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
            }
        }
    }
}