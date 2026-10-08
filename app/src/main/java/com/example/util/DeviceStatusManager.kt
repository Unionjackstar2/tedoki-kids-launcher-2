package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages live, real hardware status data (Battery, WiFi, Cellular, Airplane mode, Time)
 * without needing accessibility services, using standard Android APIs.
 */
object DeviceStatusManager {

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _isWifiConnected = MutableStateFlow(false)
    val isWifiConnected: StateFlow<Boolean> = _isWifiConnected.asStateFlow()

    private val _isCellularConnected = MutableStateFlow(false)
    val isCellularConnected: StateFlow<Boolean> = _isCellularConnected.asStateFlow()

    private val _hasInternet = MutableStateFlow(false)
    val hasInternet: StateFlow<Boolean> = _hasInternet.asStateFlow()

    private val _isAirplaneMode = MutableStateFlow(false)
    val isAirplaneMode: StateFlow<Boolean> = _isAirplaneMode.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext

        // 1. Clock timer
        CoroutineScope(Dispatchers.Main).launch {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            while (true) {
                _currentTime.value = format.format(Date())
                delay(1000)
            }
        }

        // 2. Battery Monitoring (Intent.ACTION_BATTERY_CHANGED is sticky)
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                intent?.let { updateBattery(it) }
            }
        }
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialBattery = appContext.registerReceiver(batteryReceiver, batteryFilter)
        initialBattery?.let { updateBattery(it) }

        // 3. Real Connectivity Monitoring
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm != null) {
            updateConnectivity(cm)
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            try {
                cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        updateConnectivity(cm)
                    }

                    override fun onLost(network: Network) {
                        updateConnectivity(cm)
                    }

                    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                        updateConnectivity(cm)
                    }
                })
            } catch (e: Exception) {
                // Fallback for limited environments
                updateConnectivity(cm)
            }
        }

        // 4. Airplane Mode Monitoring
        val airplaneReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                updateAirplaneMode(appContext)
            }
        }
        appContext.registerReceiver(airplaneReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        updateAirplaneMode(appContext)
    }

    private fun updateBattery(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            _batteryLevel.value = (level * 100) / scale
        }
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun updateConnectivity(cm: ConnectivityManager) {
        try {
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            if (capabilities != null) {
                _hasInternet.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                _isWifiConnected.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                _isCellularConnected.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } else {
                _hasInternet.value = false
                _isWifiConnected.value = false
                _isCellularConnected.value = false
            }
        } catch (e: Exception) {
            _hasInternet.value = false
            _isWifiConnected.value = false
            _isCellularConnected.value = false
        }
    }

    private fun updateAirplaneMode(context: Context) {
        try {
            val isAirplane = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
            _isAirplaneMode.value = isAirplane
        } catch (e: Exception) {
            _isAirplaneMode.value = false
        }
    }
}
