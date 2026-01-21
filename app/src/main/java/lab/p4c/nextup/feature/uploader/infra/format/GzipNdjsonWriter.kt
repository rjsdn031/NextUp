package lab.p4c.nextup.feature.uploader.infra.format

import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GzipNdjsonWriter @Inject constructor() {

    fun writeLines(outFile: File, lines: Sequence<String>) {
        outFile.outputStream().use { fos ->
            GZIPOutputStream(fos).use { gos ->
                BufferedWriter(OutputStreamWriter(gos, Charsets.UTF_8)).use { w ->
                    for (line in lines) {
                        w.write(line)
                        w.newLine()
                    }
                }
            }
        }
    }
}
