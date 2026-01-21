package lab.p4c.nextup.feature.uploader.infra.firebase

import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseStorageUploader @Inject constructor(
    private val storage: FirebaseStorage
) {
    /**
     * remotePath 예: users/{uid}/usage/dateKey=2026-01-13/usage.ndjson.gz
     */
    suspend fun uploadFile(remotePath: String, file: File): String {
        val ref = storage.reference.child(remotePath)
        ref.putFile(file.toUri()).await()
        return remotePath
    }
}