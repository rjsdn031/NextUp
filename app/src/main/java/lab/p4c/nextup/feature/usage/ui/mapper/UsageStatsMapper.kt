package lab.p4c.nextup.feature.usage.ui.mapper

import lab.p4c.nextup.feature.usage.infra.UsageStatsService
import lab.p4c.nextup.feature.usage.ui.model.AppUsageRow
import lab.p4c.nextup.feature.usage.ui.model.UsageSession

fun UsageStatsService.Result.toUiRows(): List<AppUsageRow> =
    summary.map { row ->
        AppUsageRow(
            packageName = row.packageName,
            totalMillis = row.total.toMillis()
        )
    }

fun UsageStatsService.Result.toUiSessions(): Map<String, List<UsageSession>> =
    sessionsByApp.mapValues { (_, list) ->
        list.map { s ->
            UsageSession(
                startMillis = s.startMillis,
                endMillis = s.endMillis,
                packageName = s.packageName
            )
        }
    }
