package com.example.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Required empty MmsReceiver to fulfill Android system requirements for ROLE_SMS.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Required for default SMS handler compliance
    }
}
