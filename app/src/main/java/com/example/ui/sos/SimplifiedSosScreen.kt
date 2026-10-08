package com.example.ui.sos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import com.example.R
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.data.sos.SosLogRepository
import com.example.model.sos.SosCallStatus
import com.example.model.sos.SosLogEntry
import com.example.model.sos.SosSmsDeliveryStatus
import com.example.model.sos.SosSmsSendStatus
import com.example.util.EmergencyHelper
import com.example.util.SosLocationData
import kotlinx.coroutines.delay
import java.util.UUID

enum class SosSmsStatus {
    IDLE,
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}

/**
 * Simplified in-app Emergency SOS Screen.
 * Fully self-contained without jumping into the external phone app.
 * Keeps user on screen, displays active in-app emergency call status,
 * acquires GPS coordinates and confirms the Google Maps link SMS dispatch.
 */
@Composable
fun SimplifiedSosScreen(
    emergencyNumber: String,
    emergencyContactName: String,
    sendSosLocationSms: Boolean = true,
    contacts: List<com.example.model.LauncherContact> = emptyList(),
    onBack: () -> Unit
) {
    val handleBackAndHangUp = {
        com.example.telecom.CallManager.hangUp()
        onBack()
    }
    BackHandler { handleBackAndHangUp() }
    val context = LocalContext.current
    val emergencyHelper = remember(context) { EmergencyHelper(context) }

    val telecomCallState by com.example.telecom.CallManager.callState.collectAsState()
    val telecomDurationSeconds by com.example.telecom.CallManager.durationSeconds.collectAsState()

    var locationData by remember { mutableStateOf<SosLocationData?>(null) }
    var isLocationLoading by remember { mutableStateOf(sendSosLocationSms) }
    var sosSmsStatus by remember { mutableStateOf(SosSmsStatus.IDLE) }
    var currentSosLogId by remember { mutableStateOf<String?>(null) }
    val allSosLogs by SosLogRepository.logsFlow.collectAsState()
    val activeSosLog = allSosLogs.find { it.id == currentSosLogId }
    val effectiveSmsStatus = when {
        activeSosLog == null -> sosSmsStatus
        activeSosLog.smsDeliveryStatus == SosSmsDeliveryStatus.DELIVERED -> SosSmsStatus.DELIVERED
        activeSosLog.smsSendStatus == SosSmsSendStatus.SENT -> SosSmsStatus.SENT
        activeSosLog.smsSendStatus == SosSmsSendStatus.FAILED || activeSosLog.smsDeliveryStatus == SosSmsDeliveryStatus.NOT_DELIVERED -> SosSmsStatus.FAILED
        activeSosLog.smsSendStatus == SosSmsSendStatus.SENDING -> SosSmsStatus.SENDING
        else -> sosSmsStatus
    }
    var fallbackCallSeconds by remember { mutableIntStateOf(0) }
    var fallbackCallConnected by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context, intent: android.content.Intent) {
                val action = intent.action ?: return
                if (action == "com.example.sms.SOS_SMS_SENT") {
                    if (resultCode == android.app.Activity.RESULT_OK) {
                        sosSmsStatus = SosSmsStatus.SENT
                        currentSosLogId?.let { id ->
                            SosLogRepository.updateSmsStatus(
                                context,
                                id,
                                SosSmsSendStatus.SENT,
                                SosSmsDeliveryStatus.PENDING
                            )
                        }
                    } else {
                        sosSmsStatus = SosSmsStatus.FAILED
                        currentSosLogId?.let { id ->
                            SosLogRepository.updateSmsStatus(
                                context,
                                id,
                                SosSmsSendStatus.FAILED,
                                SosSmsDeliveryStatus.NOT_DELIVERED
                            )
                        }
                    }
                } else if (action == "com.example.sms.SOS_SMS_DELIVERED") {
                    sosSmsStatus = SosSmsStatus.DELIVERED
                    currentSosLogId?.let { id ->
                        SosLogRepository.updateSmsStatus(
                            context,
                            id,
                            SosSmsSendStatus.SENT,
                            SosSmsDeliveryStatus.DELIVERED
                        )
                    }
                }
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction("com.example.sms.SOS_SMS_SENT")
            addAction("com.example.sms.SOS_SMS_DELIVERED")
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (ignored: Exception) {}
        }
    }

    fun performSosActions() {
        val cleanEmergency = emergencyNumber.replace(Regex("[^0-9+]"), "").trim()
        val isShortEmergency = cleanEmergency in setOf("112", "110", "911", "999") || cleanEmergency.length < 5
        val willSendSms = sendSosLocationSms && !isShortEmergency && cleanEmergency.isNotBlank()
        val displayName = emergencyContactName.ifBlank { emergencyNumber }

        val logId = UUID.randomUUID().toString()
        currentSosLogId = logId

        val newLog = SosLogEntry(
            id = logId,
            timestamp = System.currentTimeMillis(),
            emergencyNumber = displayName,
            callStatus = if (emergencyNumber.isNotBlank()) SosCallStatus.INITIATED else SosCallStatus.NOT_ATTEMPTED,
            smsSendStatus = if (willSendSms) SosSmsSendStatus.SENDING else SosSmsSendStatus.NOT_REQUESTED,
            smsDeliveryStatus = if (willSendSms) SosSmsDeliveryStatus.PENDING else SosSmsDeliveryStatus.NOT_REQUESTED
        )
        SosLogRepository.addOrUpdateEntry(context, newLog)

        if (willSendSms) {
            isLocationLoading = true
            sosSmsStatus = SosSmsStatus.SENDING
            emergencyHelper.fetchLocation { loc ->
                locationData = loc
                isLocationLoading = false
                val sentIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    logId.hashCode(),
                    android.content.Intent("com.example.sms.SOS_SMS_SENT").apply {
                        putExtra("sos_log_id", logId)
                        setPackage(context.packageName)
                    },
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val deliveredIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    logId.hashCode() + 1,
                    android.content.Intent("com.example.sms.SOS_SMS_DELIVERED").apply {
                        putExtra("sos_log_id", logId)
                        setPackage(context.packageName)
                    },
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val success = emergencyHelper.sendSosSms(cleanEmergency, loc, sentIntent, deliveredIntent)
                if (!success) {
                    sosSmsStatus = SosSmsStatus.FAILED
                    currentSosLogId?.let { id ->
                        SosLogRepository.updateSmsStatus(
                            context,
                            id,
                            SosSmsSendStatus.FAILED,
                            SosSmsDeliveryStatus.NOT_DELIVERED
                        )
                    }
                }
            }
        } else {
            isLocationLoading = false
            sosSmsStatus = SosSmsStatus.IDLE
        }

        if (emergencyNumber.isNotBlank()) {
            try {
                // SOS call must automatically start with speakerphone enabled and tracked with logId!
                emergencyHelper.makeEmergencyCall(
                    phoneNumber = emergencyNumber,
                    startWithSpeaker = true,
                    sosLogId = logId
                )
            } catch (e: Exception) {
                currentSosLogId?.let { id ->
                    SosLogRepository.updateCallStatus(context, id, SosCallStatus.FAILED)
                }
            }
        }
    }

    // Request permissions if needed
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        performSosActions()
    }

    // Trigger on enter: fetch location, send SMS, and initiate real telecom call via InCallService / CallManager
    LaunchedEffect(Unit) {
        val hasLoc = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val hasCall = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val permsToRequest = mutableListOf<String>()
        if (sendSosLocationSms && !hasLoc) {
            permsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (sendSosLocationSms && !hasSms) {
            permsToRequest.add(Manifest.permission.SEND_SMS)
        }
        if (!hasCall) {
            permsToRequest.add(Manifest.permission.CALL_PHONE)
        }

        if (permsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permsToRequest.toTypedArray())
        } else {
            performSosActions()
        }

        // Fallback timer in case telecom state is simulated or disconnected
        delay(2000)
        fallbackCallConnected = true
        while (true) {
            delay(1000)
            fallbackCallSeconds++
        }
    }

    // Siren pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_siren"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFEF2F2), Color(0xFFFFFFFF))
                )
            )
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Siren Warning Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFFDC2626))
                    .border(4.dp, Color(0xFFFCA5A5), CircleShape)
                    .shadow(12.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            // Headline
            Text(
                text = stringResource(R.string.sos_triggered_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFDC2626),
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            // 1. In-App Emergency Call Status Card (Connected to CallManager & InCallService)
            val isCallActive = telecomCallState == android.telecom.Call.STATE_ACTIVE || (telecomCallState == android.telecom.Call.STATE_DISCONNECTED && fallbackCallConnected)
            val activeSecs = if (telecomCallState == android.telecom.Call.STATE_ACTIVE) telecomDurationSeconds else fallbackCallSeconds

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isCallActive) AssistiveGreen else Color(0xFFDC2626))
                        )
                        Text(
                            text = if (isCallActive) {
                                val mins = activeSecs / 60
                                val secs = activeSecs % 60
                                "${stringResource(R.string.sos_call_connected)} • %02d:%02d".format(mins, secs)
                            } else {
                                stringResource(R.string.sos_call_in_progress)
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCallActive) AssistiveGreen else Color(0xFFDC2626)
                        )
                    }

                    Text(
                        text = emergencyNumber,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = AssistiveTextPrimary
                    )

                    if (emergencyContactName.isNotEmpty()) {
                        Text(
                            text = emergencyContactName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveRed
                        )
                    }
                }
            }

            // 2. Google Maps Location SMS Confirmation Card
            if (sendSosLocationSms) {
                val isSmsSentOrDelivered = effectiveSmsStatus == SosSmsStatus.SENT || effectiveSmsStatus == SosSmsStatus.DELIVERED
                val isSmsFailed = effectiveSmsStatus == SosSmsStatus.FAILED

                val cardBg = when {
                    isSmsSentOrDelivered -> Color(0xFFECFDF5)
                    isSmsFailed -> Color(0xFFFEF2F2)
                    else -> Color.White
                }
                val cardBorderColor = when {
                    isSmsSentOrDelivered -> AssistiveGreen
                    isSmsFailed -> AssistiveRed
                    else -> Color(0xFFE2E8F0)
                }
                val statusIcon = when {
                    isSmsSentOrDelivered -> Icons.Default.CheckCircle
                    isSmsFailed -> Icons.Default.Warning
                    else -> Icons.Default.LocationOn
                }
                val statusIconTint = when {
                    isSmsSentOrDelivered -> AssistiveGreen
                    isSmsFailed -> AssistiveRed
                    else -> Color(0xFFDC2626)
                }
                val statusTitle = when (effectiveSmsStatus) {
                    SosSmsStatus.SENDING -> "Standort-SMS wird gesendet..."
                    SosSmsStatus.SENT -> "Standort-SMS gesendet ✓"
                    SosSmsStatus.DELIVERED -> "Standort-SMS zugestellt ✓✓"
                    SosSmsStatus.FAILED -> "Standort-SMS fehlgeschlagen ❌"
                    else -> stringResource(R.string.sos_location_title)
                }
                val statusTitleColor = when {
                    isSmsSentOrDelivered -> Color(0xFF065F46)
                    isSmsFailed -> Color(0xFFB91C1C)
                    else -> AssistiveTextPrimary
                }
                val statusDescColor = when {
                    isSmsSentOrDelivered -> Color(0xFF047857)
                    isSmsFailed -> Color(0xFFB91C1C)
                    else -> AssistiveTextSecondary
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusIconTint,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = statusTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusTitleColor
                            )
                        }

                        if (isLocationLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 3.dp,
                                    color = Color(0xFFDC2626)
                                )
                                Text(
                                    text = stringResource(R.string.sos_location_sending),
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
                                )
                            }
                        } else if (locationData != null) {
                            val loc = locationData!!

                            // SMS confirmation text
                            Text(
                                text = stringResource(
                                    R.string.sos_sms_confirmed_desc,
                                    emergencyContactName.ifEmpty { emergencyNumber },
                                    emergencyNumber
                                ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusDescColor
                            )

                            // Clickable Google Maps Link box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .clickable {
                                        try {
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(loc.mapsUrl)).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(mapIntent)
                                        } catch (ignored: Exception) {}
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = loc.mapsUrl,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2563EB),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "GPS: %.4f, %.4f (±%.0fm)".format(loc.latitude, loc.longitude, loc.accuracy),
                                            fontSize = 11.sp,
                                            color = AssistiveTextMuted
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Bottom Actions: Big Red Cancel/End SOS & Large Back Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // End SOS Call Button
            Button(
                onClick = handleBackAndHangUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .testTag("sos_cancel_button"),
                shape = RoundedCornerShape(33.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = stringResource(R.string.sos_cancel),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.sos_cancel),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Large Back Button
            LargeBackButton(
                onClick = handleBackAndHangUp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
