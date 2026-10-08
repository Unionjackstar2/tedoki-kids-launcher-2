package com.example.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Required empty HeadlessSmsSendService to fulfill Android system requirements for ROLE_SMS.
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
