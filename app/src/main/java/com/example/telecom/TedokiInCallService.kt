package com.example.telecom

import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import com.example.MainActivity

private const val TAG = "TedokiInCallService"

/**
 * Android Telecom InCallService implementation for TEDOKI Kids.
 * Receives real telephony call events from Android OS,
 * and synchronizes with CallManager and the simplified call UI.
 */
class TedokiInCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: InCallService created")
        CallManager.registerService(this)
    }

    override fun onBind(intent: Intent?): android.os.IBinder? {
        Log.d(TAG, "onBind: Telecom binding to InCallService")
        CallManager.registerService(this)
        return super.onBind(intent)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "onCallAdded: call state=${call.state}")
        CallManager.onCallAdded(call)

        // Bring the simplified call screen to the foreground
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("EXTRA_NAVIGATE_TO_CALL", true)
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "onCallRemoved: call state=${call.state}")
        CallManager.onCallRemoved(call)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        Log.d(TAG, "onCallAudioStateChanged: muted=${audioState.isMuted}, route=${audioState.route}")
        CallManager.onAudioStateChanged(audioState)
    }

    override fun onBringToForeground(showDialpad: Boolean) {
        super.onBringToForeground(showDialpad)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("EXTRA_NAVIGATE_TO_CALL", true)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        CallManager.unregisterService()
    }
}
