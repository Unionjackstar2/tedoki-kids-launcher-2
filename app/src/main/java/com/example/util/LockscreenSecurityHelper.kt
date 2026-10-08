package com.example.util

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.telecom.CallManager

object LockscreenSecurityHelper {

    /**
     * Checks if the device has a secure Android lock screen enabled (PIN, pattern, password, or biometric).
     * Returns true if a system lock screen is active, false if set to "None" or "Swipe".
     */
    fun isAndroidSystemLockscreenEnabled(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            ?: return false
        return keyguardManager.isDeviceSecure
    }

    /**
     * Open Android security settings so parents can change/remove device screen lock.
     */
    fun openAndroidSecuritySettings(context: Context) {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Returns true if there is an active telecom call.
     * During active calls, when proximity sensor blanks the screen at the ear,
     * the lockscreen must NOT be triggered.
     */
    fun isCallActive(): Boolean {
        return CallManager.currentCall.value != null
    }
}
