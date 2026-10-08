package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

data class SosLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val mapsUrl: String
)

class EmergencyHelper(private val context: Context) {

    /**
     * Gets the last known or updates current location via LocationManager.
     * Uses a fast fallback mechanism so SOS SMS is never delayed excessively.
     */
    @SuppressLint("MissingPermission")
    fun fetchLocation(onLocationReceived: (SosLocationData?) -> Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            onLocationReceived(null)
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onLocationReceived(null)
            return
        }

        // 1. Check last known location first across all enabled providers
        var bestLocation: Location? = null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                        bestLocation = loc
                    }
                }
            } catch (ignored: Exception) {}
        }

        if (bestLocation != null) {
            onLocationReceived(createSosLocationData(bestLocation))
            return
        }

        // 2. If no cached location, request a single update with 3-second max timeout
        val handler = android.os.Handler(Looper.getMainLooper())
        var isCallbackFired = false

        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                if (!isCallbackFired) {
                    isCallbackFired = true
                    handler.removeCallbacksAndMessages(null)
                    try { locationManager.removeUpdates(this) } catch (ignored: Exception) {}
                    onLocationReceived(createSosLocationData(loc))
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
            override fun onProviderEnabled(p: String) {}
            override fun onProviderDisabled(p: String) {}
        }

        val timeoutRunnable = Runnable {
            if (!isCallbackFired) {
                isCallbackFired = true
                try { locationManager.removeUpdates(listener) } catch (ignored: Exception) {}
                onLocationReceived(null)
            }
        }

        try {
            val enabledProvider = when {
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }

            if (enabledProvider != null) {
                handler.postDelayed(timeoutRunnable, 3000L)
                locationManager.requestSingleUpdate(enabledProvider, listener, Looper.getMainLooper())
            } else {
                onLocationReceived(null)
            }
        } catch (e: Exception) {
            if (!isCallbackFired) {
                isCallbackFired = true
                handler.removeCallbacksAndMessages(null)
                onLocationReceived(null)
            }
        }
    }

    fun createSosLocationData(loc: Location): SosLocationData {
        val mapsUrl = String.format(java.util.Locale.US, "https://maps.google.com/?q=%.6f,%.6f", loc.latitude, loc.longitude)
        return SosLocationData(
            latitude = loc.latitude,
            longitude = loc.longitude,
            accuracy = loc.accuracy,
            mapsUrl = mapsUrl
        )
    }

    /**
     * Builds the text message containing the emergency alert and Google Maps link.
     */
    fun buildSosSmsMessage(location: SosLocationData?): String {
        return if (location != null) {
            "🚨 NOTRUF / EMERGENCY SOS! Ich brauche Hilfe! Mein Standort: ${location.mapsUrl} (Genauigkeit: ~${location.accuracy.toInt()}m)"
        } else {
            "🚨 NOTRUF / EMERGENCY SOS! Ich brauche dringend Hilfe! Bitte sofort kontaktieren."
        }
    }

    /**
     * Sends SMS with SOS message and live location to the emergency contact.
     * Operates purely in the background without launching external messaging apps.
     */
    fun sendSosSms(
        phoneNumber: String,
        location: SosLocationData?,
        sentIntent: PendingIntent? = null,
        deliveryIntent: PendingIntent? = null
    ): Boolean {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "").trim()
        if (cleanNumber.isEmpty()) return false

        val locationText = buildSosSmsMessage(location)

        return try {
            val hasSmsPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasSmsPermission) {
                @Suppress("DEPRECATION")
                val smsManager: SmsManager = SmsManager.getDefault()

                val parts: java.util.ArrayList<String> = try {
                    smsManager.divideMessage(locationText) ?: java.util.ArrayList(listOf(locationText))
                } catch (e: Exception) {
                    java.util.ArrayList(listOf(locationText))
                }

                if (parts.size > 1) {
                    try {
                        val sentIntents: java.util.ArrayList<PendingIntent>? = if (sentIntent != null) {
                            val list = java.util.ArrayList<PendingIntent>()
                            for (i in 0 until parts.size) {
                                list.add(sentIntent)
                            }
                            list
                        } else null

                        val deliveredIntents: java.util.ArrayList<PendingIntent>? = if (deliveryIntent != null) {
                            val list = java.util.ArrayList<PendingIntent>()
                            for (i in 0 until parts.size) {
                                list.add(deliveryIntent)
                            }
                            list
                        } else null

                        smsManager.sendMultipartTextMessage(
                            cleanNumber,
                            null,
                            parts,
                            sentIntents,
                            deliveredIntents
                        )
                    } catch (e: Exception) {
                        smsManager.sendTextMessage(cleanNumber, null, locationText, sentIntent, deliveryIntent)
                    }
                } else {
                    smsManager.sendTextMessage(cleanNumber, null, locationText, sentIntent, deliveryIntent)
                }

                android.util.Log.d("EmergencyHelper", "SOS SMS dispatched successfully to $cleanNumber")
                true
            } else {
                android.util.Log.w("EmergencyHelper", "SEND_SMS permission not granted")
                false
            }
        } catch (e: Throwable) {
            android.util.Log.e("EmergencyHelper", "Error dispatching SOS SMS to $cleanNumber: ${e.message}", e)
            false
        }
    }

    /**
     * Calls the emergency number directly using CallManager with SOS flag.
     */
    fun makeEmergencyCall(
        phoneNumber: String,
        startWithSpeaker: Boolean = false,
        sosLogId: String? = null
    ) {
        val cleanNumber = phoneNumber.trim()
        if (cleanNumber.isEmpty()) return
        com.example.telecom.CallManager.startCall(
            context = context,
            number = cleanNumber,
            startWithSpeaker = startWithSpeaker,
            isSos = true,
            sosLogId = sosLogId
        )
    }
}
