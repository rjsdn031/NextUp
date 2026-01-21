package lab.p4c.nextup.feature.usage.infra.upload

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.p4c.nextup.feature.usage.data.repository.UsageRepository
import lab.p4c.nextup.feature.uploader.infra.format.GzipNdjsonWriter

@Singleton
class UsagePackager @Inject constructor(
    private val usageRepository: UsageRepository,
    private val codec: UsageNdjsonCodec,
    private val gzipWriter: GzipNdjsonWriter,
    @ApplicationContext private val context: Context
) {

    suspend fun buildGzipNdjsonWindow(dateKey: String, startMs: Long, endMs: Long): File =
        withContext(Dispatchers.IO) {
            val rows = usageRepository.getEntitiesByTimeWindow(startMs, endMs)

            val outDir = File(context.cacheDir, "uploads/usage").apply { mkdirs() }
            val outFile = File(outDir, "usage_$dateKey.ndjson.gz")
            if (outFile.exists()) outFile.delete()

            val lines = rows.asSequence().map { e -> codec.encodeLine(e) }
            gzipWriter.writeLines(outFile, lines)

            outFile
        }
}
