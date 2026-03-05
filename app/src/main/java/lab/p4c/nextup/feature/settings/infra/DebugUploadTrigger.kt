package lab.p4c.nextup.feature.settings.infra

interface DebugUploadTrigger {
    fun triggerInSeconds(seconds: Long)
}