package lab.p4c.nextup.platform.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import lab.p4c.nextup.core.domain.auth.port.AuthClient

class FirebaseAuthClient @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthClient {

    override fun currentUidOrNull(): String? = firebaseAuth.currentUser?.uid

    override suspend fun signInAnonymously(): String =
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInAnonymously()
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
