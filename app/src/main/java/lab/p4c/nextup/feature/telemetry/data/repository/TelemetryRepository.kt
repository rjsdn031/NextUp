package lab.p4c.nextup.feature.telemetry.data.repository

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.feature.telemetry.infra.upload.TelemetryPackager

@Singleton
class TelemetryRepository @Inject constructor(
    private val packager: TelemetryPackager
) {
    fun deleteDateFile(dateKey: String) {
        val f: File = packager.telemetryFile(dateKey)
        if (f.exists()) f.delete()
    }
}
