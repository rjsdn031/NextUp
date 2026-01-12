package lab.p4c.nextup.platform.telemetry.permission

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PermissionTelemetryEntryPoint {
    fun permissionChangeTracker(): PermissionChangeTracker
}