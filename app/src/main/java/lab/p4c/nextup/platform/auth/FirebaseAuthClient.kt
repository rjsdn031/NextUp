package lab.p4c.nextup.platform.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import lab.p4c.nextup.core.domain.auth.port.AuthClient

class FirebaseAuthClient @Inject constructor(
    private val auth: FirebaseAuth
) : AuthClient {
    private val mutex = Mutex()

    override fun currentUidOrNull(): String? = auth.currentUser?.uid

    override suspend fun ensureSignedIn(): String =
        suspendCancellableCoroutine { cont ->
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid != null) cont.resume(uid)
                    else cont.resumeWithException(IllegalStateException("Anonymous sign-in succeeded but uid is null"))
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
}
