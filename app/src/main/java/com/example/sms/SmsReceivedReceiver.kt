package com.example.sms

import android.content.Context
import android.content.Intent

/**
 * Standard incoming SMS receiver subclass.
 * Points to SmsReceiver to process standard broadcasts (SMS_RECEIVED) safely when not set as default SMS app.
 */
class SmsReceivedReceiver : SmsReceiver()
