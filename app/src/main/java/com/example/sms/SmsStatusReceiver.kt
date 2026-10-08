package com.example.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.sos.SosLogRepository
import com.example.model.sos.SosSmsDeliveryStatus
import com.example.model.sos.SosSmsSendStatus

class SmsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val sosLogId = intent.getStringExtra("sos_log_id")

        if (!sosLogId.isNullOrBlank()) {
            Log.d("SmsStatusReceiver", "SOS SMS callback: action=$action, sosLogId=$sosLogId, resultCode=$resultCode")
            SosLogRepository.init(context)
            when (action) {
                "com.example.sms.SOS_SMS_SENT" -> {
                    if (resultCode == Activity.RESULT_OK) {
                        SosLogRepository.updateSmsStatus(
                            context,
                            sosLogId,
                            SosSmsSendStatus.SENT,
                            SosSmsDeliveryStatus.PENDING
                        )
                    } else {
                        SosLogRepository.updateSmsStatus(
                            context,
                            sosLogId,
                            SosSmsSendStatus.FAILED,
                            SosSmsDeliveryStatus.NOT_DELIVERED
                        )
                    }
                }
                "com.example.sms.SOS_SMS_DELIVERED" -> {
                    SosLogRepository.updateSmsStatus(
                        context,
                        sosLogId,
                        SosSmsSendStatus.SENT,
                        SosSmsDeliveryStatus.DELIVERED
                    )
                }
            }
            return
        }

        val messageId = intent.getStringExtra("message_id") ?: return
        Log.d("SmsStatusReceiver", "onReceive: action=$action, messageId=$messageId, resultCode=$resultCode")

        SmsRepository.init(context)
        if (action == "com.example.sms.SMS_SENT") {
            if (resultCode == Activity.RESULT_OK) {
                SmsRepository.updateMessageStatus(context, messageId, SmsSendStatus.SENT)
            } else {
                SmsRepository.updateMessageStatus(context, messageId, SmsSendStatus.FAILED)
            }
        } else if (action == "com.example.sms.SMS_DELIVERED") {
            SmsRepository.updateMessageStatus(context, messageId, SmsSendStatus.DELIVERED)
        }
    }
}
