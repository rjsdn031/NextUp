package lab.p4c.nextup.feature.settings.infra

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.feature.settings.ui.model.InstalledAppInfo

@Singleton
class InstalledAppFetcher @Inject constructor(
    @param:ApplicationContext val context: Context
) {

    fun fetchInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(launcherIntent, 0)

        return resolved
            .map { ri ->
                val appInfo = ri.activityInfo.applicationInfo
                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = runCatching { pm.getApplicationIcon(appInfo) }.getOrNull()
                )
            }
            .filter { it.packageName.isNotBlank() }
            .sortedBy { it.appName.lowercase() }
    }
}
