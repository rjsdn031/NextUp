package lab.p4c.nextup.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object MicrophonePermission {

    /** 마이크 권한이 허용되어 있는지 확인 */
    fun isGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * 권한 요청을 시도한다.
     * - Activity context에서만 호출 가능.
     * - 시스템 다이얼로그를 띄우므로 onResume에서 다시 체크 필요.
     */
    fun request(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE
        )
    }

    /**
     * 사용자가 “다시는 묻지 않기”로 거부했을 경우, 앱 설정 화면으로 이동
     */
    fun openSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private const val REQUEST_CODE = 3001
}
