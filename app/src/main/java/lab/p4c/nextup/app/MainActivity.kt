package lab.p4c.nextup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import lab.p4c.nextup.app.ui.AppRoot
import lab.p4c.nextup.app.ui.theme.NextUpTheme
import lab.p4c.nextup.core.domain.survey.usecase.ScheduleDailySurveyReminder
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var scheduleDailySurveyReminder: ScheduleDailySurveyReminder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDailySurveyReminder(21, 0)
        setContent {
            NextUpTheme {
                AppRoot()
            }
        }
    }
}