package lab.p4c.nextup.feature.telemetry.infra.upload

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryPackager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun buildGzipJsonlForDate(dateKey: String): File {
        val input = telemetryFile(dateKey)
        require(input.exists()) { "Telemetry file not found for dateKey=$dateKey path=${input.absolutePath}" }

        val outFile = File(context.cacheDir, "telemetry_$dateKey.jsonl.gz")
        gzip(input, outFile)
        return outFile
    }

    fun telemetryFile(dateKey: String): File {
        val dir = File(context.filesDir, "telemetry")
        return File(dir, "telemetry_$dateKey.jsonl")
    }

    private fun gzip(input: File, output: File) {
        BufferedInputStream(FileInputStream(input)).use { fis ->
            BufferedOutputStream(FileOutputStream(output)).use { fos ->
                GZIPOutputStream(fos).use { gos ->
                    fis.copyTo(gos)
                }
            }
        }
    }
}
