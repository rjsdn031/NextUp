package lab.p4c.nextup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import lab.p4c.nextup.app.ui.AppRoot
import lab.p4c.nextup.app.ui.theme.NextUpTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextUpTheme {
                AppRoot()
            }
        }
    }
}