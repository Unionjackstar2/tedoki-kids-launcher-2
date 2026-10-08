package com.example.ui.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Conni-style analog learning clock for kids:
 * - Warm creamy parchment dial
 * - Outer bold red & white frame
 * - Outer minute tick ring (60 minute dashes)
 * - Bright red bold hours 1..12
 * - Inner blue 24-hour numbers 13..24
 * - Prominent red hour hand (shorter, chunky) and minute hand (longer, pointed)
 * - Red central cap
 *
 * Pedagogical Easter Egg:
 * Both hands can be freely rotated by children touching/dragging them!
 * On each discrete minute tick during rotation, the vibration motor emits a tactile click.
 * Whenever the screen turns off or on (or the lockscreen re-opens), the hands automatically
 * reset back to the real current time!
 */
@Composable
fun ConniLearningClock(
    modifier: Modifier = Modifier,
    resetKey: Any? = Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerTickHaptic() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Try prebaked effect click, fallback to one shot vibration
                    val clickEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    vibrator.vibrate(clickEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (_: Exception) {
            // Fallback to simple vibration
            try {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20L)
            } catch (_: Exception) {}
        }
    }

    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    // User drag Easter Egg states:
    // If user interacts with hour hand or minute hand, isUserInteracting becomes true
    // and manual angles override real time.
    var isUserInteracting by remember { mutableStateOf(false) }
    var userMinuteAngleDeg by remember { mutableFloatStateOf(0f) }
    var userHourAngleDeg by remember { mutableFloatStateOf(0f) }

    // Reset clock hands whenever resetKey changes
    LaunchedEffect(resetKey) {
        isUserInteracting = false
        currentTime = Calendar.getInstance()
    }

    // Reset clock hands immediately whenever the screen turns off or on (power button pressed)
    DisposableEffect(context) {
        val screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF,
                    Intent.ACTION_SCREEN_ON,
                    Intent.ACTION_USER_PRESENT -> {
                        isUserInteracting = false
                        currentTime = Calendar.getInstance()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(screenReceiver, filter)
        onDispose {
            try {
                context.unregisterReceiver(screenReceiver)
            } catch (_: Exception) {}
        }
    }

    // Reset clock hands whenever the activity resumes (e.g. device woke up or foregrounded)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                isUserInteracting = false
                currentTime = Calendar.getInstance()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Track last minute index (0..59) to trigger haptic feedback exactly when ticking over a minute
    var lastMinuteTickIndex by remember { mutableIntStateOf(-1) }
    var lastHourTickIndex by remember { mutableIntStateOf(-1) }

    // Active drag target: "NONE", "MINUTE", "HOUR"
    var activeHandDrag by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isUserInteracting) {
        while (!isUserInteracting) {
            currentTime = Calendar.getInstance()
            delay(1000L)
        }
    }

    val realHour = currentTime.get(Calendar.HOUR)
    val realMinute = currentTime.get(Calendar.MINUTE)
    val realSecond = currentTime.get(Calendar.SECOND)

    val realHourFraction = realHour + (realMinute / 60f) + (realSecond / 3600f)
    val realMinuteFraction = realMinute + (realSecond / 60f)

    // Compute effective angles (0 deg = 12 o'clock, clockwise)
    val currentMinuteDeg = if (isUserInteracting) {
        userMinuteAngleDeg
    } else {
        (realMinuteFraction * 6f) % 360f
    }

    val currentHourDeg = if (isUserInteracting) {
        userHourAngleDeg
    } else {
        (realHourFraction * 30f) % 360f
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("conni_learning_clock")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = startOffset.x - centerX
                        val dy = startOffset.y - centerY
                        val dist = sqrt(dx * dx + dy * dy)
                        val radius = (minOf(size.width, size.height) / 2f) * 0.94f

                        // Touch angle in degrees from 12 o'clock (0..360)
                        var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                        if (touchAngle < 0f) touchAngle += 360f

                        // Initialize user angles from current clock if not already interacting
                        if (!isUserInteracting) {
                            userMinuteAngleDeg = currentMinuteDeg
                            userHourAngleDeg = currentHourDeg
                            isUserInteracting = true
                        }

                        // Determine which hand is closer/targeted:
                        // Minute hand is longer (radius * 0.45f .. radius * 0.95f)
                        // Hour hand is shorter (radius * 0.15f .. radius * 0.60f)
                        val angleDiffMinute = minOf(
                            kotlin.math.abs(touchAngle - (userMinuteAngleDeg % 360f)),
                            360f - kotlin.math.abs(touchAngle - (userMinuteAngleDeg % 360f))
                        )
                        val angleDiffHour = minOf(
                            kotlin.math.abs(touchAngle - (userHourAngleDeg % 360f)),
                            360f - kotlin.math.abs(touchAngle - (userHourAngleDeg % 360f))
                        )

                        // Heuristic: if touch is further out, prefer minute hand; if closer to center, prefer hour hand
                        activeHandDrag = if (dist > radius * 0.55f) {
                            if (angleDiffMinute <= 45f) "MINUTE"
                            else if (angleDiffHour <= 35f) "HOUR"
                            else "MINUTE" // Free rotate the longer minute hand by default outside
                        } else if (dist > radius * 0.12f) {
                            if (angleDiffHour <= 45f) "HOUR"
                            else if (angleDiffMinute <= 35f) "MINUTE"
                            else "HOUR"
                        } else {
                            null
                        }

                        // Initialize tick indices
                        val minuteTick = ((userMinuteAngleDeg % 360f) / 6f).toInt()
                        lastMinuteTickIndex = (minuteTick % 60 + 60) % 60
                        val hourTick = ((userHourAngleDeg % 360f) / 30f).toInt()
                        lastHourTickIndex = (hourTick % 12 + 12) % 12
                    },
                    onDragEnd = {
                        activeHandDrag = null
                    },
                    onDragCancel = {
                        activeHandDrag = null
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val pos = change.position
                        val dx = pos.x - centerX
                        val dy = pos.y - centerY

                        // Calculate current finger angle from 12 o'clock (0..360)
                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                        if (angleDeg < 0f) angleDeg += 360f

                        when (activeHandDrag) {
                            "MINUTE" -> {
                                val oldAngle = userMinuteAngleDeg
                                var delta = angleDeg - (oldAngle % 360f)
                                if (delta > 180f) delta -= 360f
                                if (delta < -180f) delta += 360f

                                userMinuteAngleDeg = (oldAngle + delta)
                                if (userMinuteAngleDeg < 0f) userMinuteAngleDeg += 360f
                                userMinuteAngleDeg %= 360f

                                // Also naturally advance the hour hand slightly (like a real clock mechanism)
                                userHourAngleDeg = (userHourAngleDeg + delta / 12f)
                                if (userHourAngleDeg < 0f) userHourAngleDeg += 360f
                                userHourAngleDeg %= 360f

                                // Check minute tick crossing (each 6 degrees is 1 minute)
                                val currentMinuteTick = (userMinuteAngleDeg / 6f).toInt() % 60
                                if (currentMinuteTick != lastMinuteTickIndex) {
                                    triggerTickHaptic()
                                    lastMinuteTickIndex = currentMinuteTick
                                }
                            }
                            "HOUR" -> {
                                val oldAngle = userHourAngleDeg
                                var delta = angleDeg - (oldAngle % 360f)
                                if (delta > 180f) delta -= 360f
                                if (delta < -180f) delta += 360f

                                userHourAngleDeg = (oldAngle + delta)
                                if (userHourAngleDeg < 0f) userHourAngleDeg += 360f
                                userHourAngleDeg %= 360f

                                // When dragging the hour hand directly, calculate corresponding minute index or 5-min intervals
                                val currentHourTick = (userHourAngleDeg / 6f).toInt() % 60
                                if (currentHourTick != lastHourTickIndex) {
                                    triggerTickHaptic()
                                    lastHourTickIndex = currentHourTick
                                }
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) * 0.94f

            val warmDialColor = Color(0xFFF9F7E8) // Warm creamy paper background
            val outerBorderColor = Color(0xFFD63B3B) // Conni warm red border
            val innerDottedRingColor = Color(0xFFD1D5DB)
            val minuteTickColor = Color(0xFFB91C1C)

            // 1. Outer Red Border Ring
            drawCircle(
                color = outerBorderColor,
                radius = radius,
                center = center,
                style = Stroke(width = radius * 0.08f)
            )

            // 2. Main Dial Face
            drawCircle(
                color = warmDialColor,
                radius = radius * 0.96f,
                center = center
            )

            // 3. Inner accent circle for 24h area
            drawCircle(
                color = Color(0xFFFFFFFF).copy(alpha = 0.55f),
                radius = radius * 0.58f,
                center = center
            )
            drawCircle(
                color = innerDottedRingColor.copy(alpha = 0.6f),
                radius = radius * 0.58f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Minute Ticks (60 ticks)
            val tickRadiusOuter = radius * 0.94f
            val tickRadiusInnerShort = radius * 0.86f
            val tickRadiusInnerLong = radius * 0.81f

            for (i in 0 until 60) {
                val angleRad = (i * 6f - 90f) * (PI / 180f).toFloat()
                val isFiveMinute = (i % 5 == 0)
                val innerR = if (isFiveMinute) tickRadiusInnerLong else tickRadiusInnerShort
                val strokeWidth = if (isFiveMinute) 3.5.dp.toPx() else 1.8.dp.toPx()
                val tickColor = if (isFiveMinute) minuteTickColor else minuteTickColor.copy(alpha = 0.75f)

                val start = Offset(
                    x = center.x + innerR * cos(angleRad),
                    y = center.y + innerR * sin(angleRad)
                )
                val end = Offset(
                    x = center.x + tickRadiusOuter * cos(angleRad),
                    y = center.y + tickRadiusOuter * sin(angleRad)
                )

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // 5. Draw 1..12 Numbers (Outer, Big, Red, Kid-friendly)
            val textPaint12 = Paint().apply {
                color = android.graphics.Color.rgb(214, 40, 40)
                textSize = radius * 0.22f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val numRadius12 = radius * 0.70f
            for (h in 1..12) {
                val angleRad = (h * 30f - 90f) * (PI / 180f).toFloat()
                val textX = center.x + numRadius12 * cos(angleRad)
                val textY = center.y + numRadius12 * sin(angleRad) - ((textPaint12.descent() + textPaint12.ascent()) / 2f)

                drawContext.canvas.nativeCanvas.drawText(
                    h.toString(),
                    textX,
                    textY,
                    textPaint12
                )
            }

            // 6. Draw 13..24 Numbers (Inner, Smaller, Blue)
            val textPaint24 = Paint().apply {
                color = android.graphics.Color.rgb(37, 99, 235)
                textSize = radius * 0.12f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val numRadius24 = radius * 0.46f
            for (h in 1..12) {
                val val24 = if (h == 12) 24 else (h + 12)
                val angleRad = (h * 30f - 90f) * (PI / 180f).toFloat()
                val textX = center.x + numRadius24 * cos(angleRad)
                val textY = center.y + numRadius24 * sin(angleRad) - ((textPaint24.descent() + textPaint24.ascent()) / 2f)

                drawContext.canvas.nativeCanvas.drawText(
                    val24.toString(),
                    textX,
                    textY,
                    textPaint24
                )
            }

            // 7. Hands (Conni-style Chunky Red Hands)
            val handRed = Color(0xFFDC2626)
            val handShadow = Color(0x33000000)

            // Hour Hand (Shorter, Chunky)
            val hourAngleRad = (currentHourDeg - 90f) * (PI / 180f).toFloat()
            val hourHandLength = radius * 0.44f
            val hourHandWidth = radius * 0.08f

            // Minute Hand (Longer, Pointer)
            val minuteAngleRad = (currentMinuteDeg - 90f) * (PI / 180f).toFloat()
            val minuteHandLength = radius * 0.72f
            val minuteHandWidth = radius * 0.065f

            // Hour Hand Shadow + Hand
            drawLine(
                color = handShadow,
                start = center + Offset(4f, 4f),
                end = Offset(
                    center.x + hourHandLength * cos(hourAngleRad) + 4f,
                    center.y + hourHandLength * sin(hourAngleRad) + 4f
                ),
                strokeWidth = hourHandWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = handRed,
                start = center,
                end = Offset(
                    center.x + hourHandLength * cos(hourAngleRad),
                    center.y + hourHandLength * sin(hourAngleRad)
                ),
                strokeWidth = hourHandWidth,
                cap = StrokeCap.Round
            )

            // Minute Hand Shadow + Hand
            drawLine(
                color = handShadow,
                start = center + Offset(4f, 4f),
                end = Offset(
                    center.x + minuteHandLength * cos(minuteAngleRad) + 4f,
                    center.y + minuteHandLength * sin(minuteAngleRad) + 4f
                ),
                strokeWidth = minuteHandWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = handRed,
                start = center,
                end = Offset(
                    center.x + minuteHandLength * cos(minuteAngleRad),
                    center.y + minuteHandLength * sin(minuteAngleRad)
                ),
                strokeWidth = minuteHandWidth,
                cap = StrokeCap.Round
            )

            // 8. Center Plastic Cap (Red)
            drawCircle(
                color = Color(0xFF991B1B),
                radius = radius * 0.12f,
                center = center
            )
            drawCircle(
                color = handRed,
                radius = radius * 0.09f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = radius * 0.035f,
                center = center - Offset(radius * 0.02f, radius * 0.02f)
            )
        }
    }
}
