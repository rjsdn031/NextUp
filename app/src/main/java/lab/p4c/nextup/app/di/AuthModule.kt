package lab.p4c.nextup.app.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.auth.port.AuthClient
import lab.p4c.nextup.platform.auth.FirebaseAuthClient

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthClient(
        firebaseAuth: FirebaseAuth
    ): AuthClient = FirebaseAuthClient(firebaseAuth)
}
