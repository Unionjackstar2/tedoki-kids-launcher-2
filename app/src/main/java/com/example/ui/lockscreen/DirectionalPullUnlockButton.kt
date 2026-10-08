package com.example.ui.lockscreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Large green pull-to-unlock lock button in the bottom third.
 * Kids can drag it in ANY direction (up, down, left, right, diagonally).
 * When dragged past a threshold (approx 80-100dp), it unlocks with haptic/visual feedback.
 */
@Composable
fun DirectionalPullUnlockButton(
    modifier: Modifier = Modifier,
    resetTrigger: Any? = null,
    onUnlocked: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var isUnlockedTriggered by remember { mutableStateOf(false) }
    var returnAnimationJob by remember { mutableStateOf<Job?>(null) }

    // Explicitly animate back to center
    fun springBackToCenter(onCompleted: (() -> Unit)? = null) {
        returnAnimationJob?.cancel()
        returnAnimationJob = coroutineScope.launch {
            val animX = launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
            val animY = launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
            animX.join()
            animY.join()
            onCompleted?.invoke()
        }
    }

    // When resetTrigger changes (e.g. math dialog dismissed or shown), ensure the button is smoothly centered and interactive
    LaunchedEffect(resetTrigger) {
        returnAnimationJob?.cancel()
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
        isUnlockedTriggered = false
    }

    // Threshold in pixels to trigger unlock (85 dp)
    val unlockThresholdPx = with(density) { 85.dp.toPx() }

    val currentDistance = hypot(offsetX.value, offsetY.value)
    val progress = (currentDistance / unlockThresholdPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .size(160.dp)
            .testTag("directional_unlock_area"),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow / target guidance rings
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f + 0.15f * progress))
        )
        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
        )

        // Draggable green lock button container positioned via offset
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .size(92.dp)
                .scale(1f + 0.08f * progress)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (!isUnlockedTriggered) {
                                springBackToCenter()
                            }
                        },
                        onDragCancel = {
                            if (!isUnlockedTriggered) {
                                springBackToCenter()
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isUnlockedTriggered) return@detectDragGestures

                            returnAnimationJob?.cancel()
                            val newX = offsetX.value + dragAmount.x
                            val newY = offsetY.value + dragAmount.y
                            coroutineScope.launch {
                                offsetX.snapTo(newX)
                                offsetY.snapTo(newY)
                            }

                            val dist = hypot(newX, newY)
                            if (dist >= unlockThresholdPx && !isUnlockedTriggered) {
                                isUnlockedTriggered = true
                                onUnlocked()
                                springBackToCenter {
                                    isUnlockedTriggered = false
                                }
                            }
                        }
                    )
                }
                .testTag("pull_unlock_button"),
            contentAlignment = Alignment.Center
        ) {
            // Material3 Surface provides rock-solid elevation, hardware clipping to CircleShape and background
            Surface(
                modifier = Modifier.size(92.dp),
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF34D399), // Bright emerald green
                                    Color(0xFF059669)  // Deep green
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (progress >= 0.85f || isUnlockedTriggered) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Entsperren durch Ziehen",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }
    }
}

