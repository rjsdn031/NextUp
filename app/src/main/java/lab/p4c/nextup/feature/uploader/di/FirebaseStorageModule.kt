package lab.p4c.nextup.feature.uploader.di

import com.google.firebase.storage.FirebaseStorage

@dagger.Module
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
object FirebaseModule {
    @dagger.Provides
    @javax.inject.Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
