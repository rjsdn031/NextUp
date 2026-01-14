package lab.p4c.nextup.feature.usage.ui.mapper

import lab.p4c.nextup.feature.usage.infra.UsageStatsService
import lab.p4c.nextup.feature.usage.ui.model.AppUsageRow
import lab.p4c.nextup.feature.usage.ui.model.UsageSession

private val DEFAULT_EXCLUDED_PACKAGES = setOf(
    "com.android.systemui",
    "com.sec.android.app.launcher",       // One UI Home
    "com.google.android.apps.nexuslauncher", // Pixel Launcher
    "com.android.launcher3",
    "com.samsung.android.honeyboard",     // Samsung Keyboard
    "com.google.android.inputmethod.latin", // Gboard
)

private fun shouldExclude(pkg: String): Boolean {
    if (pkg in DEFAULT_EXCLUDED_PACKAGES) return true

    return false
}

fun UsageStatsService.Result.toUiRows(): List<AppUsageRow> =
    summary
        .filter { !shouldExclude(it.packageName) }
        .map { row ->
        AppUsageRow(
            packageName = row.packageName,
            totalMillis = row.total.toMillis()
        )
    }

fun UsageStatsService.Result.toUiSessions(): Map<String, List<UsageSession>> =
    sessionsByApp
        .filterKeys { pkg -> !shouldExclude(pkg) }
        .mapValues { (_, list) -> list.map { s ->
            UsageSession(
                startMillis = s.startMillis,
                endMillis = s.endMillis,
                packageName = s.packageName
            )
        }
    }
