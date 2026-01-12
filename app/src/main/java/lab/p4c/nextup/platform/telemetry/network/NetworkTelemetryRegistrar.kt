package lab.p4c.nextup.platform.telemetry.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import lab.p4c.nextup.core.domain.telemetry.service.TelemetryLogger

@Singleton
class NetworkTelemetryRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val telemetryLogger: TelemetryLogger
) {
    private val cm = context.getSystemService(ConnectivityManager::class.java)
    private var registered = false

    private var lastStatus: Boolean? = null
    private var lastType: String? = null

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = logIfChanged()
        override fun onLost(network: Network) = logIfChanged()
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = logIfChanged()
    }

    fun start() {
        if (registered) return
        registered = true

        logIfChanged()

        runCatching { cm.registerDefaultNetworkCallback(callback) }
            .onFailure { registered = false }
    }

    fun stop() {
        if (!registered) return
        registered = false
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    private fun logIfChanged() {
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }

        val connected =
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val type = when {
            caps == null -> "NONE"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "OTHER"
        }

        if (lastStatus == connected && lastType == type) return

        lastStatus = connected
        lastType = type

        telemetryLogger.log(
            eventName = "NetworkConnected",
            payload = mapOf(
                "connectStatus" to connected.toString(),
                "connectType" to type
            )
        )
    }
}
