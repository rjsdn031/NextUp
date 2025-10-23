package lab.p4c.nextup.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lab.p4c.nextup.core.common.permission.PermissionChecker
import lab.p4c.nextup.platform.permission.AndroidPermissionChecker
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {

    @Provides
    @Singleton
    fun providePermissionChecker(
        @ApplicationContext ctx: Context
    ): PermissionChecker = AndroidPermissionChecker(ctx)
}
