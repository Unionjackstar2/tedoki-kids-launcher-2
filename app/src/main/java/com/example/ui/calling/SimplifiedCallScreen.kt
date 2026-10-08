package com.example.ui.calling

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.telecom.Call
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.LauncherButton
import com.example.telecom.CallManager
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import kotlinx.coroutines.delay

/**
 * Simplified in-app Call Screen backed by Android Telecom InCallService.
 * Connects directly to carrier network calls via CallManager.
 */
@Composable
fun SimplifiedCallScreen(
    button: LauncherButton,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current

    // Proximity Sensor & Screen-Off WakeLock (Hardware Ear Detection)
    val powerManager = remember {
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }
    val proximityWakeLock = remember {
        try {
            if (powerManager != null &&
                powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)
            ) {
                powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    "tedoki:CallProximityWakeLock"
                ).apply {
                    setReferenceCounted(false)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Proximity Sensor fallback
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    val proximitySensor = remember {
        sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }
    var isNearEar by remember { mutableStateOf(false) }

    // Keep screen awake during active call + engage proximity screen-off
    DisposableEffect(Unit) {
        val activity = context.findActivity()

        // 1. Keep screen on while call is running (no timeout)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 2. Hardware Proximity WakeLock
        try {
            proximityWakeLock?.acquire()
        } catch (e: Exception) {}

        // 3. Software Proximity Sensor Listener
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.isNotEmpty()) {
                    val distance = event.values[0]
                    val maxRange = proximitySensor?.maximumRange ?: 5f
                    // If distance is less than 4cm and less than maxRange, phone is at ear
                    isNearEar = distance < 4f && distance < maxRange
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (proximitySensor != null) {
            sensorManager?.registerListener(
                sensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            // Restore normal screen timeout and release wake lock immediately when leaving call
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            try {
                if (proximityWakeLock?.isHeld == true) {
                    proximityWakeLock.release()
                }
            } catch (e: Exception) {}
            try {
                sensorManager?.unregisterListener(sensorListener)
            } catch (e: Exception) {}
        }
    }

    val telecomCall by CallManager.currentCall.collectAsState()
    val telecomCallState by CallManager.callState.collectAsState()
    val callDuration by CallManager.durationSeconds.collectAsState()
    val isMuted by CallManager.isMuted.collectAsState()
    val isSpeakerOn by CallManager.isSpeakerOn.collectAsState()

    var isDefaultDialer by remember { mutableStateOf(CallManager.isDefaultDialer(context)) }

    // Launcher for requesting Default Dialer role
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultDialer = CallManager.isDefaultDialer(context)
    }

    // Launcher for CALL_PHONE permission
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && button.phoneNumber.isNotBlank()) {
            CallManager.startCall(context, button.phoneNumber)
        }
    }

    // Trigger phone call via CallManager when entering screen
    LaunchedEffect(button.phoneNumber) {
        if (button.phoneNumber.isNotBlank()) {
            val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPerm) {
                // If not already in an active call, start outgoing call
                if (telecomCallState == Call.STATE_DISCONNECTED) {
                    CallManager.startCall(context, button.phoneNumber)
                }
            } else {
                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    // Automatically navigate back after call ends
    LaunchedEffect(telecomCallState) {
        if (telecomCallState == Call.STATE_DISCONNECTED && telecomCall != null) {
            delay(1200)
            onBack()
        }
    }

    val isCallEnded = telecomCallState == Call.STATE_DISCONNECTED && telecomCall != null
    val isCallActive = telecomCallState == Call.STATE_ACTIVE
    val isIncomingRinging = telecomCallState == Call.STATE_RINGING

    // Pulse animation for avatar while connecting, ringing or calling
    val infiniteTransition = rememberInfiniteTransition(label = "calling_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!isCallEnded) (if (isIncomingRinging) 1.12f else 1.08f) else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isIncomingRinging) 700 else 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AssistiveBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Optional banner: Set as default phone app for full InCallService integration
            if (!isDefaultDialer) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = CallManager.createDefaultDialerIntent(context)
                            if (intent != null) {
                                roleLauncher.launch(intent)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Tipp: Als Standard-Telefon festlegen für Vollbild-Anrufe",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Contact Info & Avatar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pulsing large avatar with incoming call halo
                val haloColor = if (isIncomingRinging) AssistiveGreen else Color(button.colorHex)

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                if (!isCallEnded) haloColor.copy(alpha = if (isIncomingRinging) 0.35f else 0.2f) else Color.Transparent
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(button.colorHex))
                            .border(3.dp, if (isIncomingRinging) AssistiveGreen else Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (button.photoUri != null) {
                            AsyncImage(
                                model = button.photoUri,
                                contentDescription = button.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(70.dp)
                            )
                        }
                    }
                }

                // Incoming Call hint
                if (isIncomingRinging) {
                    Text(
                        text = stringResource(R.string.incoming_call),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AssistiveGreen
                    )
                }

                // Contact Name
                Text(
                    text = com.example.ui.components.getLocalizedButtonTitle(button),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )

                // Call Status & Timer Badge powered by Android Telecom
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isCallEnded -> Color(0xFFFEF2F2)
                            isCallActive || isIncomingRinging -> Color(0xFFECFDF5)
                            else -> Color(0xFFEFF6FF)
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCallEnded -> AssistiveRed
                                        isCallActive || isIncomingRinging -> AssistiveGreen
                                        else -> AssistiveBlue
                                    }
                                )
                        )

                        Text(
                            text = when {
                                isCallEnded -> stringResource(R.string.call_ended)
                                isCallActive -> {
                                    val mins = callDuration / 60
                                    val secs = callDuration % 60
                                    "${stringResource(R.string.call_connected)} • %02d:%02d".format(mins, secs)
                                }
                                isIncomingRinging -> stringResource(R.string.incoming_call)
                                else -> stringResource(R.string.call_connecting)
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCallEnded -> AssistiveRed
                                isCallActive || isIncomingRinging -> AssistiveGreen
                                else -> AssistiveBlue
                            }
                        )
                    }
                }

                if (button.phoneNumber.isNotEmpty()) {
                    Text(
                        text = button.phoneNumber,
                        fontSize = 17.sp,
                        color = AssistiveTextMuted
                    )
                }

                // Mute & Speaker Toggles (controlling real Telecom audio routing)
                AnimatedVisibility(
                    visible = isCallActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        // Mute button
                        CallOptionButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = stringResource(R.string.call_mute),
                            isActive = isMuted,
                            onClick = { CallManager.toggleMute() }
                        )

                        // Speaker button
                        CallOptionButton(
                            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            label = stringResource(R.string.call_speaker),
                            isActive = isSpeakerOn,
                            onClick = { CallManager.toggleSpeaker() }
                        )
                    }
                }
            }

            // Bottom Actions: Pillow Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!isCallEnded) {
                    if (isIncomingRinging) {
                        // Incoming call ringing: Large Green "Annehmen" button & Large Red "Ablehnen" button in Pillow Design
                        Button(
                            onClick = {
                                CallManager.answer()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .testTag("accept_call_button"),
                            shape = RoundedCornerShape(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AssistiveGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = stringResource(R.string.accept_call),
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.accept_call),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                CallManager.hangUp()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .testTag("decline_call_button"),
                            shape = RoundedCornerShape(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AssistiveRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = stringResource(R.string.decline_call),
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.decline_call),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Active or connecting call: Very large Red "Auflegen" button
                        Button(
                            onClick = {
                                CallManager.hangUp()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .testTag("end_call_button"),
                            shape = RoundedCornerShape(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AssistiveRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = stringResource(R.string.end_call),
                                modifier = Modifier
                                    .size(30.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = stringResource(R.string.end_call),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // When phone is held to ear, render solid black overlay to disable screen & touches
        if (isNearEar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        enabled = true,
                        onClick = {}
                    )
            )
        }
    }
}

@Composable
private fun CallOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isActive) AssistiveBlue else Color.White)
                .border(2.dp, if (isActive) AssistiveBlue else Color(0xFFCBD5E1), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else AssistiveTextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) AssistiveBlue else AssistiveTextSecondary
        )
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
