package lab.p4c.nextup.feature.usage.infra.upload

import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream
import lab.p4c.nextup.feature.usage.data.local.entity.UsageEntity

object GzipNdjsonWriter {

    fun writeToGzipFile(
        outFile: File,
        rows: List<UsageEntity>
    ): File {
        outFile.parentFile?.mkdirs()

        FileOutputStream(outFile).use { fos ->
            GZIPOutputStream(fos).use { gz ->
                rows.forEach { e ->
                    val line = UsageNdjsonCodec.toNdjsonLine(e) + "\n"
                    gz.write(line.toByteArray(Charsets.UTF_8))
                }
            }
        }
        return outFile
    }
}
