package com.example.telecom

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.TelecomManager
import android.util.Log
import com.example.data.call.CallHistoryRepository
import com.example.model.call.CallDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "CallManager"

/**
 * Central repository and controller for active Android Telecom calls,
 * connected directly to TedokiInCallService.
 */
object CallManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var durationJob: Job? = null

    private var inCallService: TedokiInCallService? = null
    private var currentAppContext: Context? = null
    private var activeCallHistoryId: String? = null
    private var currentCallDirection: CallDirection = CallDirection.OUTGOING
    private var callWasAnswered: Boolean = false

    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall.asStateFlow()

    private val _callState = MutableStateFlow<Int>(Call.STATE_DISCONNECTED)
    val callState: StateFlow<Int> = _callState.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isSosCall = MutableStateFlow(false)
    val isSosCall: StateFlow<Boolean> = _isSosCall.asStateFlow()

    private val _activeSosLogId = MutableStateFlow<String?>(null)
    val activeSosLogId: StateFlow<String?> = _activeSosLogId.asStateFlow()

    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber.asStateFlow()

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d(TAG, "Call onStateChanged: $state")
            _callState.value = state

            when (state) {
                Call.STATE_ACTIVE -> {
                    callWasAnswered = true
                    startTimer()
                }
                Call.STATE_DISCONNECTED -> {
                    stopTimer()
                    val duration = _durationSeconds.value
                    val ctx = currentAppContext ?: inCallService?.applicationContext
                    val historyId = activeCallHistoryId
                    if (ctx != null && historyId != null) {
                        if (currentCallDirection == CallDirection.INCOMING && !callWasAnswered && duration == 0) {
                            // If incoming call was not answered, it is missed
                            CallHistoryRepository.deleteCall(ctx, historyId)
                            val number = _phoneNumber.value ?: ""
                            CallHistoryRepository.addCall(
                                context = ctx,
                                phoneNumber = number,
                                direction = CallDirection.MISSED,
                                durationSeconds = 0
                            )
                        } else {
                            CallHistoryRepository.updateCallDuration(ctx, historyId, duration)
                        }
                    }

                    scope.launch {
                        delay(1200)
                        if (_callState.value == Call.STATE_DISCONNECTED) {
                            _currentCall.value = null
                            _durationSeconds.value = 0
                            activeCallHistoryId = null
                        }
                    }
                }
                else -> {}
            }
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            val handle = details.handle?.schemeSpecificPart
            if (!handle.isNullOrBlank()) {
                _phoneNumber.value = handle
            }
        }
    }

    fun registerService(service: TedokiInCallService) {
        Log.d(TAG, "registerService: InCallService attached")
        inCallService = service
        currentAppContext = service.applicationContext
    }

    fun unregisterService() {
        Log.d(TAG, "unregisterService: InCallService detached")
        inCallService = null
    }

    fun onCallAdded(call: Call) {
        Log.d(TAG, "onCallAdded: call=$call, state=${call.state}")
        _currentCall.value = call
        _callState.value = call.state
        val handle = call.details?.handle?.schemeSpecificPart
        if (!handle.isNullOrBlank()) {
            _phoneNumber.value = handle
        }

        call.registerCallback(callCallback)

        val isIncoming = call.state == Call.STATE_RINGING
        if (isIncoming) {
            currentCallDirection = CallDirection.INCOMING
            callWasAnswered = false
            val ctx = currentAppContext ?: inCallService?.applicationContext
            if (ctx != null && !handle.isNullOrBlank()) {
                val entry = CallHistoryRepository.addCall(
                    context = ctx,
                    phoneNumber = handle,
                    direction = CallDirection.INCOMING,
                    durationSeconds = 0
                )
                activeCallHistoryId = entry.id
            }
        }

        if (call.state == Call.STATE_ACTIVE) {
            callWasAnswered = true
            startTimer()
        }
    }

    fun onCallRemoved(call: Call) {
        Log.d(TAG, "onCallRemoved: call=$call")
        call.unregisterCallback(callCallback)
        _callState.value = Call.STATE_DISCONNECTED
        stopTimer()

        val duration = _durationSeconds.value
        val ctx = currentAppContext ?: inCallService?.applicationContext
        val historyId = activeCallHistoryId
        if (ctx != null && historyId != null) {
            if (currentCallDirection == CallDirection.INCOMING && !callWasAnswered && duration == 0) {
                CallHistoryRepository.deleteCall(ctx, historyId)
                val number = _phoneNumber.value ?: ""
                CallHistoryRepository.addCall(
                    context = ctx,
                    phoneNumber = number,
                    direction = CallDirection.MISSED,
                    durationSeconds = 0
                )
            } else {
                CallHistoryRepository.updateCallDuration(ctx, historyId, duration)
            }
        }

        scope.launch {
            delay(1200)
            if (_currentCall.value == call) {
                _currentCall.value = null
                _durationSeconds.value = 0
                activeCallHistoryId = null
            }
        }
    }

    fun onAudioStateChanged(audioState: CallAudioState) {
        _isMuted.value = audioState.isMuted
        _isSpeakerOn.value = (audioState.route and CallAudioState.ROUTE_SPEAKER) != 0
    }

    fun hangUp() {
        val call = _currentCall.value
        if (call != null) {
            Log.d(TAG, "hangUp called, disconnecting call...")
            try {
                call.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting call", e)
            }
        }
        _callState.value = Call.STATE_DISCONNECTED
        stopTimer()
    }

    fun answer() {
        val call = _currentCall.value
        if (call != null && call.state == Call.STATE_RINGING) {
            try {
                call.answer(0)
            } catch (e: Exception) {
                Log.e(TAG, "Error answering call", e)
            }
        }
        _callState.value = Call.STATE_ACTIVE
        startTimer()
    }

    fun simulateIncomingCall(number: String = "0151 1234567") {
        currentCallDirection = CallDirection.INCOMING
        callWasAnswered = false
        _phoneNumber.value = number
        _durationSeconds.value = 0
        _callState.value = Call.STATE_RINGING

        val ctx = currentAppContext
        if (ctx != null) {
            val entry = CallHistoryRepository.addCall(
                context = ctx,
                phoneNumber = number,
                direction = CallDirection.INCOMING,
                durationSeconds = 0
            )
            activeCallHistoryId = entry.id
        }
    }

    fun toggleMute() {
        val service = inCallService
        if (service != null) {
            val target = !_isMuted.value
            service.setMuted(target)
            _isMuted.value = target
        }
    }

    fun toggleSpeaker() {
        val service = inCallService
        if (service != null) {
            val target = !_isSpeakerOn.value
            val route = if (target) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
            service.setAudioRoute(route)
            _isSpeakerOn.value = target
        }
    }

    fun resetSosState() {
        _isSosCall.value = false
        _activeSosLogId.value = null
    }

    fun startCall(
        context: Context,
        number: String,
        startWithSpeaker: Boolean = false,
        isSos: Boolean = false,
        sosLogId: String? = null
    ) {
        val cleanNumber = number.trim()
        if (cleanNumber.isBlank()) return

        currentAppContext = context.applicationContext
        currentCallDirection = CallDirection.OUTGOING
        callWasAnswered = false

        val entry = CallHistoryRepository.addCall(
            context = context,
            phoneNumber = cleanNumber,
            direction = CallDirection.OUTGOING,
            durationSeconds = 0
        )
        activeCallHistoryId = entry.id

        _isSosCall.value = isSos
        _activeSosLogId.value = sosLogId
        _phoneNumber.value = cleanNumber
        _durationSeconds.value = 0
        _callState.value = Call.STATE_CONNECTING

        val uri = Uri.parse("tel:$cleanNumber")
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

        try {
            val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (hasPerm && telecomManager != null && isDefaultDialer(context)) {
                val extras = android.os.Bundle()
                extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, startWithSpeaker)
                telecomManager.placeCall(uri, extras)
            } else {
                val intent = Intent(if (hasPerm) Intent.ACTION_CALL else Intent.ACTION_DIAL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to place call via TelecomManager", e)
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed fallback dial intent", e2)
            }
        }
    }

    fun isDefaultDialer(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true
        } else {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            telecomManager?.defaultDialerPackage == context.packageName
        }
    }

    fun createDefaultDialerIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        } else {
            @Suppress("DEPRECATION")
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            }
        }
    }

    private fun startTimer() {
        if (durationJob?.isActive == true) return
        durationJob = scope.launch {
            while (isActive && _callState.value == Call.STATE_ACTIVE) {
                delay(1000)
                _durationSeconds.value += 1
            }
        }
    }

    private fun stopTimer() {
        durationJob?.cancel()
        durationJob = null
    }
}
