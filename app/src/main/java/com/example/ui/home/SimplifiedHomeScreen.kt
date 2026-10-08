package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import kotlinx.coroutines.isActive
import androidx.compose.runtime.withFrameMillis
import kotlin.random.Random
import com.example.ui.theme.AssistiveBlue
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.quiz.PedagogyPreferences
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherSettings
import com.example.model.LayoutStyle
import com.example.model.quiz.AppPedagogyStatus
import com.example.model.quiz.QuizCategory
import com.example.model.quiz.ReviewDueState
import com.example.ui.components.PinKeypadDialog
import com.example.ui.components.SosHoldButton
import com.example.ui.components.SosModalDialog
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveGreenLight
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SimplifiedHomeScreen(
    settings: LauncherSettings,
    pedagogyStatuses: Map<String, AppPedagogyStatus> = emptyMap(),
    celebrationMessage: String? = null,
    onDismissCelebration: () -> Unit = {},
    onButtonClick: (LauncherButton) -> Unit,
    onTriggerQuiz: (LauncherButton, String, QuizCategory, ReviewDueState) -> Unit = { _, _, _, _ -> },
    onOpenParentSettings: () -> Unit,
    onSosTriggered: () -> Unit,
    onReorderButtons: (List<LauncherButton>) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        com.example.sms.SmsRepository.init(context)
    }

    var buttonsList by remember(settings.buttons) { mutableStateOf(settings.buttons) }
    var draggingButtonId by remember { mutableStateOf<String?>(null) }
    var startSlotIndex by remember { mutableStateOf<Int?>(null) }
    var targetSlot by remember { mutableStateOf<Int?>(null) }
    var startSlotRect by remember { mutableStateOf(Rect.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }

    val currentButtonsList by rememberUpdatedState(buttonsList)
    val currentOnReorderButtons by rememberUpdatedState(onReorderButtons)

    LaunchedEffect(settings.buttons) {
        if (draggingButtonId == null) {
            buttonsList = settings.buttons
        }
    }

    val displayButtons = remember(buttonsList, draggingButtonId, targetSlot) {
        val currentDraggingId = draggingButtonId
        val currentTargetSlot = targetSlot
        if (currentDraggingId == null || currentTargetSlot == null) {
            buttonsList
        } else {
            val fromIndex = buttonsList.indexOfFirst { it.id == currentDraggingId }
            if (fromIndex != -1 && fromIndex != currentTargetSlot && currentTargetSlot in buttonsList.indices) {
                val list = buttonsList.toMutableList()
                val item = list.removeAt(fromIndex)
                list.add(currentTargetSlot, item)
                list
            } else {
                buttonsList
            }
        }
    }

    val commitDrop = {
        val currentDragId = draggingButtonId
        val currentTarget = targetSlot
        val baseList = currentButtonsList
        if (currentDragId != null && currentTarget != null) {
            val fromIndex = baseList.indexOfFirst { it.id == currentDragId }
            val toIndex = currentTarget
            if (fromIndex != -1 && fromIndex != toIndex && toIndex in baseList.indices) {
                val updated = baseList.toMutableList().apply {
                    val item = removeAt(fromIndex)
                    add(toIndex, item)
                }
                buttonsList = updated
                currentOnReorderButtons(updated)
            }
        }
        draggingButtonId = null
        targetSlot = null
        startSlotIndex = null
        dragOffset = Offset.Zero
    }
    val currentCommitDrop by rememberUpdatedState(commitDrop)

    var showPinDialog by remember { mutableStateOf(false) }
    var showAvatarModal by remember { mutableStateOf(false) }
    var showSosModal by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    val locale = if (settings.language == "de") Locale.GERMANY else Locale.US

    // Live clock update
    LaunchedEffect(settings.language) {
        val timeFormat = SimpleDateFormat("HH:mm", locale)
        val dateFormat = SimpleDateFormat("EEEE, d. MMMM", locale)
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    val handleTileClick: (LauncherButton) -> Unit = { button ->
        val appKey = PedagogyPreferences.resolveAppKey(button)
        val quizCat = PedagogyPreferences.resolveQuizCategory(button)
        if (quizCat != null) {
            val status = pedagogyStatuses[appKey] ?: AppPedagogyStatus(
                appKey = appKey,
                appName = button.title,
                quizCategory = quizCat
            )
            val reviewDue = status.isReviewDue()
            if (!status.unlocked || reviewDue != ReviewDueState.NONE) {
                onTriggerQuiz(button, appKey, quizCat, reviewDue)
            } else {
                onButtonClick(button)
            }
        } else {
            onButtonClick(button)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (LocalIsDarkTheme.current) {
                        listOf(
                            Color(0xFF0F172A),
                            AssistiveBackground,
                            Color(0xFF020617)
                        )
                    } else {
                        listOf(
                            Color(0xFFF8FAFC),
                            AssistiveBackground,
                            Color(0xFFE2E8F0)
                        )
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Calm Header: Time, Date & Discreet Parent Lock Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentTime.ifEmpty { "12:00" },
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary,
                        letterSpacing = (-1).sp,
                        modifier = Modifier
                            .testTag("home_clock_display")
                            .then(
                                if (settings.enableClockClick) {
                                    Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            val opened = com.example.util.AppUtils.openClockApp(context)
                                            if (!opened) {
                                                android.widget.Toast.makeText(context, R.string.clock_app_not_found, android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else Modifier
                            )
                    )
                    Text(
                        text = currentDate.ifEmpty { "Wednesday, 1 October" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = AssistiveTextSecondary,
                        modifier = Modifier
                            .testTag("home_date_display")
                            .then(
                                if (settings.enableCalendarClick) {
                                    Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val opened = com.example.util.AppUtils.openCalendarApp(context)
                                            if (!opened) {
                                                android.widget.Toast.makeText(context, R.string.calendar_app_not_found, android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else Modifier
                            )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Circular SOS Button (same size 46.dp, placed left of child avatar button)
                    if (settings.showSosButton) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                                .border(1.5.dp, Color(0xFFFCA5A5), CircleShape)
                                .clickable { showSosModal = true }
                                .testTag("sos_circular_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SOS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    val avatarPreset = com.example.ui.setup.PRESET_AVATARS.find { it.id == settings.childAvatar }
                    val isCustomPhoto = settings.childAvatar.startsWith("content://") || settings.childAvatar.startsWith("file://")

                    // Child Avatar Button (same size as parent lock button: 46.dp)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCustomPhoto) Color.Transparent
                                else Color(avatarPreset?.bgHex ?: 0xFF3B82F6)
                            )
                            .border(1.5.dp, AssistiveBorder, CircleShape)
                            .clickable { showAvatarModal = true }
                            .testTag("child_avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCustomPhoto) {
                            AsyncImage(
                                model = settings.childAvatar,
                                contentDescription = "Avatar von ${settings.childName}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = avatarPreset?.emoji ?: "🦁",
                                fontSize = 24.sp
                            )
                        }
                    }

                    // Discreet Parent Access Icon in corner
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AssistiveSurface.copy(alpha = 0.85f))
                            .border(1.dp, AssistiveBorder, CircleShape)
                            .clickable { showPinDialog = true }
                            .testTag("parent_lock_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Parent Settings",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Buttons according to chosen LayoutStyle (Grid or Rows) with live Drag-and-Drop
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (settings.layoutStyle == LayoutStyle.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home_grid_layout"),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(displayButtons, key = { _, button -> button.id }) { index, button ->
                            val appKey = PedagogyPreferences.resolveAppKey(button)
                            val isDragging = draggingButtonId == button.id

                            val targetIndex = if (isDragging) (targetSlot ?: index) else index
                            val targetSlotRect = slotBounds[targetIndex] ?: startSlotRect

                            val translationX = if (isDragging && startSlotRect != Rect.Zero && targetSlotRect != Rect.Zero) {
                                startSlotRect.left + dragOffset.x - targetSlotRect.left
                            } else if (isDragging) {
                                dragOffset.x
                            } else 0f

                            val translationY = if (isDragging && startSlotRect != Rect.Zero && targetSlotRect != Rect.Zero) {
                                startSlotRect.top + dragOffset.y - targetSlotRect.top
                            } else if (isDragging) {
                                dragOffset.y
                            } else 0f

                            val tileModifier = Modifier
                                .animateItem()
                                .onGloballyPositioned { coordinates ->
                                    if (draggingButtonId == null) {
                                        slotBounds[index] = coordinates.boundsInRoot()
                                    }
                                }
                                .zIndex(if (isDragging) 10f else 1f)
                                .graphicsLayer {
                                    this.translationX = translationX
                                    this.translationY = translationY
                                    if (isDragging) {
                                        scaleX = 1.08f
                                        scaleY = 1.08f
                                        shadowElevation = 24.dp.toPx()
                                    }
                                }
                                .pointerInput(button.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val currentIdx = displayButtons.indexOfFirst { it.id == button.id }
                                            draggingButtonId = button.id
                                            startSlotIndex = currentIdx
                                            targetSlot = currentIdx
                                            startSlotRect = slotBounds[currentIdx] ?: Rect.Zero
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            if (startSlotRect != Rect.Zero) {
                                                val currentCenter = startSlotRect.center + dragOffset
                                                val bestSlot = slotBounds.entries
                                                    .filter { it.key < displayButtons.size }
                                                    .minByOrNull { (_, rect) ->
                                                        val dx = rect.center.x - currentCenter.x
                                                        val dy = rect.center.y - currentCenter.y
                                                        dx * dx + dy * dy
                                                    }?.key

                                                if (bestSlot != null && bestSlot != targetSlot) {
                                                    targetSlot = bestSlot
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            currentCommitDrop()
                                        },
                                        onDragCancel = {
                                            currentCommitDrop()
                                        }
                                    )
                                }

                            val quizCat = PedagogyPreferences.resolveQuizCategory(button)
                            AssistiveGridTile(
                                button = button,
                                pedagogyStatus = pedagogyStatuses[appKey] ?: if (quizCat != null) {
                                    AppPedagogyStatus(appKey = appKey, appName = button.title, quizCategory = quizCat, unlocked = false)
                                } else null,
                                modifier = tileModifier,
                                isClickable = draggingButtonId == null,
                                onClick = { handleTileClick(button) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home_rows_layout"),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(displayButtons, key = { _, button -> button.id }) { index, button ->
                            val appKey = PedagogyPreferences.resolveAppKey(button)
                            val isDragging = draggingButtonId == button.id

                            val targetIndex = if (isDragging) (targetSlot ?: index) else index
                            val targetSlotRect = slotBounds[targetIndex] ?: startSlotRect

                            val translationX = if (isDragging && startSlotRect != Rect.Zero && targetSlotRect != Rect.Zero) {
                                startSlotRect.left + dragOffset.x - targetSlotRect.left
                            } else if (isDragging) {
                                dragOffset.x
                            } else 0f

                            val translationY = if (isDragging && startSlotRect != Rect.Zero && targetSlotRect != Rect.Zero) {
                                startSlotRect.top + dragOffset.y - targetSlotRect.top
                            } else if (isDragging) {
                                dragOffset.y
                            } else 0f

                            val tileModifier = Modifier
                                .animateItem()
                                .onGloballyPositioned { coordinates ->
                                    if (draggingButtonId == null) {
                                        slotBounds[index] = coordinates.boundsInRoot()
                                    }
                                }
                                .zIndex(if (isDragging) 10f else 1f)
                                .graphicsLayer {
                                    this.translationX = translationX
                                    this.translationY = translationY
                                    if (isDragging) {
                                        scaleX = 1.08f
                                        scaleY = 1.08f
                                        shadowElevation = 24.dp.toPx()
                                    }
                                }
                                .pointerInput(button.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val currentIdx = displayButtons.indexOfFirst { it.id == button.id }
                                            draggingButtonId = button.id
                                            startSlotIndex = currentIdx
                                            targetSlot = currentIdx
                                            startSlotRect = slotBounds[currentIdx] ?: Rect.Zero
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            if (startSlotRect != Rect.Zero) {
                                                val currentCenter = startSlotRect.center + dragOffset
                                                val bestSlot = slotBounds.entries
                                                    .filter { it.key < displayButtons.size }
                                                    .minByOrNull { (_, rect) ->
                                                        val dx = rect.center.x - currentCenter.x
                                                        val dy = rect.center.y - currentCenter.y
                                                        dx * dx + dy * dy
                                                    }?.key

                                                if (bestSlot != null && bestSlot != targetSlot) {
                                                    targetSlot = bestSlot
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            currentCommitDrop()
                                        },
                                        onDragCancel = {
                                            currentCommitDrop()
                                        }
                                    )
                                }

                            val quizCat = PedagogyPreferences.resolveQuizCategory(button)
                            AssistiveRowTile(
                                button = button,
                                pedagogyStatus = pedagogyStatuses[appKey] ?: if (quizCat != null) {
                                    AppPedagogyStatus(appKey = appKey, appName = button.title, quizCategory = quizCat, unlocked = false)
                                } else null,
                                modifier = tileModifier,
                                isClickable = draggingButtonId == null,
                                onClick = { handleTileClick(button) }
                            )
                        }
                    }
                }
            }

            // Celebration Modal with Confetti when an app was just unlocked
            if (celebrationMessage != null) {
                CelebrationModal(
                    message = celebrationMessage,
                    onDismiss = onDismissCelebration
                )
            }
        }

        // Parent PIN Dialog
        if (showPinDialog) {
            PinKeypadDialog(
                correctPin = settings.pin,
                onPinSuccess = {
                    showPinDialog = false
                    onOpenParentSettings()
                },
                onDismiss = { showPinDialog = false }
            )
        }

        // Child Avatar Explanation Modal
        if (showAvatarModal) {
            ChildAvatarExplanationModal(
                childName = settings.childName,
                childAvatar = settings.childAvatar,
                buttons = buttonsList,
                pedagogyStatuses = pedagogyStatuses,
                onDismiss = { showAvatarModal = false }
            )
        }

        // SOS Hold Modal Overlay
        if (showSosModal) {
            SosModalDialog(
                emergencyNumber = settings.emergencyNumber,
                emergencyContactName = settings.emergencyContactName,
                onSosTriggered = onSosTriggered,
                onDismiss = { showSosModal = false }
            )
        }
    }
}

/**
 * Large iOS Assistive Access style Grid Tile (Big icon/photo + large label)
 */
@Composable
fun AssistiveGridTile(
    button: LauncherButton,
    pedagogyStatus: AppPedagogyStatus? = null,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    onClick: () -> Unit
) {
    val smsMessages by com.example.sms.SmsRepository.messagesFlow.collectAsState()
    val unreadSmsCount = remember(smsMessages, button.type) {
        if (button.type == ButtonType.SMS) smsMessages.count { it.isIncoming && !it.isRead } else 0
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shadow(6.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .clickable(enabled = isClickable, onClick = onClick)
                .testTag("tile_${button.id}"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Icon or Contact Photo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(button.colorHex)),
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
                    } else if (button.type == ButtonType.APP) {
                        com.example.util.InstalledAppIcon(
                            packageName = button.packageName,
                            modifier = Modifier.size(52.dp)
                        )
                    } else {
                        val icon = when (button.type) {
                            ButtonType.CAMERA -> Icons.Default.PhotoCamera
                            ButtonType.PHOTOS -> Icons.Default.Image
                            ButtonType.CALL -> Icons.Default.Phone
                            ButtonType.APP -> Icons.Default.Phone
                            ButtonType.BROWSER -> Icons.Default.Language
                            ButtonType.SMS -> Icons.Default.Email
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = button.title,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High contrast large label
                Text(
                    text = com.example.ui.components.getLocalizedButtonTitle(button),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary,
                    maxLines = 1
                )
            }
        }

        // Status badge: green checkmark if fully unlocked, or lock if protected & locked
        if (pedagogyStatus != null) {
            val isUnlocked = pedagogyStatus.isFullyUnlocked()
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) AssistiveGreen else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                    contentDescription = if (isUnlocked) "Freigeschaltet" else "Gesperrt",
                    tint = if (isUnlocked) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Visual notification counter badge for unread SMS
        if (unreadSmsCount > 0 && button.type == ButtonType.SMS) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadSmsCount > 99) "99+" else unreadSmsCount.toString(),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Large iOS Assistive Access style Row Tile (Full-width list with large icon & text)
 */
@Composable
fun AssistiveRowTile(
    button: LauncherButton,
    pedagogyStatus: AppPedagogyStatus? = null,
    modifier: Modifier = Modifier,
    isClickable: Boolean = true,
    onClick: () -> Unit
) {
    val smsMessages by com.example.sms.SmsRepository.messagesFlow.collectAsState()
    val unreadSmsCount = remember(smsMessages, button.type) {
        if (button.type == ButtonType.SMS) smsMessages.count { it.isIncoming && !it.isRead } else 0
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .shadow(5.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .clickable(enabled = isClickable, onClick = onClick)
                .testTag("row_${button.id}"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Icon or Photo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(button.colorHex)),
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
                    } else if (button.type == ButtonType.APP) {
                        com.example.util.InstalledAppIcon(
                            packageName = button.packageName,
                            modifier = Modifier.size(42.dp)
                        )
                    } else {
                        val icon = when (button.type) {
                            ButtonType.CAMERA -> Icons.Default.PhotoCamera
                            ButtonType.PHOTOS -> Icons.Default.Image
                            ButtonType.CALL -> Icons.Default.Phone
                            ButtonType.APP -> Icons.Default.Phone
                            ButtonType.BROWSER -> Icons.Default.Language
                            ButtonType.SMS -> Icons.Default.Email
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = button.title,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = com.example.ui.components.getLocalizedButtonTitle(button),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    if (button.type == ButtonType.CALL && button.phoneNumber.isNotEmpty()) {
                        Text(
                            text = button.phoneNumber,
                            fontSize = 15.sp,
                            color = AssistiveTextMuted
                        )
                    }
                }
            }
        }

        // Status badge: green checkmark if fully unlocked, or lock if protected & locked
        if (pedagogyStatus != null) {
            val isUnlocked = pedagogyStatus.isFullyUnlocked()
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) AssistiveGreen else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                    contentDescription = if (isUnlocked) "Freigeschaltet" else "Gesperrt",
                    tint = if (isUnlocked) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Visual notification counter badge for unread SMS
        if (unreadSmsCount > 0 && button.type == ButtonType.SMS) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (unreadSmsCount > 99) "99+" else unreadSmsCount.toString(),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private data class UnlockedFeatureItem(
    val id: String,
    val title: String,
    val emoji: String,
    val colorHex: Long,
    val explanation: String
)

@Composable
private fun ChildAvatarExplanationModal(
    childName: String,
    childAvatar: String,
    buttons: List<LauncherButton>,
    pedagogyStatuses: Map<String, AppPedagogyStatus>,
    onDismiss: () -> Unit
) {
    val avatarPreset = com.example.ui.setup.PRESET_AVATARS.find { it.id == childAvatar }
    val isCustomPhoto = childAvatar.startsWith("content://") || childAvatar.startsWith("file://")

    // Determine unlocked features from buttons and pedagogyStatuses
    val unlockedItems = remember(buttons, pedagogyStatuses) {
        val list = mutableListOf<UnlockedFeatureItem>()
        val seenKeys = mutableSetOf<String>()

        for (btn in buttons) {
            val appKey = PedagogyPreferences.resolveAppKey(btn)
            val quizCat = PedagogyPreferences.resolveQuizCategory(btn)
            val isUnlocked = if (quizCat != null) {
                pedagogyStatuses[appKey]?.isFullyUnlocked() == true
            } else {
                true
            }

            if (isUnlocked) {
                val uniqueKey = when (btn.type) {
                    ButtonType.CAMERA -> "camera"
                    ButtonType.PHOTOS -> "photos"
                    ButtonType.CALL -> "call_${btn.title}"
                    ButtonType.BROWSER -> "browser"
                    ButtonType.SMS -> "sms"
                    ButtonType.APP -> "app_${btn.packageName ?: btn.id}"
                }
                if (!seenKeys.contains(uniqueKey)) {
                    seenKeys.add(uniqueKey)
                    val item = when (btn.type) {
                        ButtonType.CAMERA -> UnlockedFeatureItem(
                            id = uniqueKey,
                            title = "Kamera",
                            emoji = "📸",
                            colorHex = 0xFF2563EB,
                            explanation = "Du darfst tolle Fotos machen! Frage Freunde vorher um Erlaubnis."
                        )
                        ButtonType.PHOTOS -> UnlockedFeatureItem(
                            id = uniqueKey,
                            title = "Fotos & Galerie",
                            emoji = "🖼️",
                            colorHex = 0xFFDB2777,
                            explanation = "Hier kannst du deine eigenen Bilder und Erinnerungen anschauen."
                        )
                        ButtonType.CALL -> UnlockedFeatureItem(
                            id = uniqueKey,
                            title = "Anrufen: ${btn.title}",
                            emoji = "📞",
                            colorHex = 0xFF059669,
                            explanation = "Du kannst ${btn.title} direkt anrufen. Sag Bescheid, wenn du Hilfe brauchst!"
                        )
                        ButtonType.BROWSER -> UnlockedFeatureItem(
                            id = uniqueKey,
                            title = "Sicheres Internet",
                            emoji = "🌐",
                            colorHex = 0xFF0284C7,
                            explanation = "Entdecke geprüfte Kinderseiten wie Die Maus, KiKA und FragFINN."
                        )
                        ButtonType.SMS -> UnlockedFeatureItem(
                            id = uniqueKey,
                            title = "SMS Nachrichten",
                            emoji = "💬",
                            colorHex = 0xFF6366F1,
                            explanation = "Schreibe und empfange SMS mit deinen Kontakten."
                        )
                        ButtonType.APP -> {
                            when (quizCat) {
                                QuizCategory.WHATSAPP -> UnlockedFeatureItem(
                                    id = uniqueKey,
                                    title = "WhatsApp / Chat",
                                    emoji = "💬",
                                    colorHex = 0xFF25D366,
                                    explanation = "Schreibe liebe Nachrichten an Familie und halte Passwörter geheim."
                                )
                                QuizCategory.NACHRICHTEN, QuizCategory.SPRACHNACHRICHTEN -> UnlockedFeatureItem(
                                    id = uniqueKey,
                                    title = btn.title,
                                    emoji = "💬",
                                    colorHex = 0xFF6366F1,
                                    explanation = "Freundlich chatten und bei komischen Nachrichten den Eltern Bescheid sagen."
                                )
                                QuizCategory.INTERNET_SURFEN -> UnlockedFeatureItem(
                                    id = uniqueKey,
                                    title = btn.title,
                                    emoji = "🌐",
                                    colorHex = 0xFF0284C7,
                                    explanation = "Sicher im Internet surfen und auf geprüften Webseiten bleiben."
                                )
                                else -> UnlockedFeatureItem(
                                    id = uniqueKey,
                                    title = btn.title,
                                    emoji = "📱",
                                    colorHex = 0xFF4F46E5,
                                    explanation = "Freigeschaltete App. Nutze sie achtsam und halte die vereinbarten Zeiten ein."
                                )
                            }
                        }
                    }
                    list.add(item)
                }
            }
        }
        list
    }

    // Count how many features are currently locked
    val lockedCount = remember(buttons, pedagogyStatuses) {
        buttons.count { btn ->
            val appKey = PedagogyPreferences.resolveAppKey(btn)
            val quizCat = PedagogyPreferences.resolveQuizCategory(btn)
            quizCat != null && pedagogyStatuses[appKey]?.isFullyUnlocked() != true
        }
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
            border = BorderStroke(1.5.dp, AssistiveBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("child_avatar_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Avatar, Greeting & Big Close Button (X)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCustomPhoto) Color.Transparent
                                    else Color(avatarPreset?.bgHex ?: 0xFF3B82F6)
                                )
                                .border(1.5.dp, AssistiveBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCustomPhoto) {
                                AsyncImage(
                                    model = childAvatar,
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(text = avatarPreset?.emoji ?: "🦁", fontSize = 26.sp)
                            }
                        }

                        Column {
                            Text(
                                text = "Hallo ${childName.ifBlank { "Entdecker" }}! 🌟",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = "Deine freigeschalteten Apps:",
                                fontSize = 13.sp,
                                color = AssistiveTextSecondary
                            )
                        }
                    }

                    // Big X Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AssistiveBackground)
                            .border(1.dp, AssistiveBorder, CircleShape)
                            .testTag("close_avatar_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = AssistiveTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content: List of unlocked items or empty state
                if (unlockedItems.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveBackground),
                        border = BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "🌱", fontSize = 32.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Noch keine Apps freigeschaltet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tippe auf eine Kachel auf deinem Homescreen, um gemeinsam mit deinen Eltern das Quiz zu starten!",
                                    fontSize = 12.sp,
                                    color = AssistiveTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        unlockedItems.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AssistiveBackground,
                                border = BorderStroke(1.dp, AssistiveBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(item.colorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = item.emoji, fontSize = 18.sp)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AssistiveTextPrimary
                                        )
                                        Text(
                                            text = item.explanation,
                                            fontSize = 11.sp,
                                            color = AssistiveTextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer hint
                if (lockedCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Noch $lockedCount Funktion(en) mit Eltern per Quiz freischaltbar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AssistiveTextMuted
                        )
                    }
                } else {
                    Text(
                        text = "Super gemacht! Alle deine Funktionen sind freigeschaltet. 🚀",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AssistiveGreen
                    )
                }
            }
        }
    }
}

data class ConfettiParticle(
    val xFraction: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val rotation: Float,
    val rotSpeed: Float
)

@Composable
fun CelebrationModal(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
        ) {
            // Live Confetti Canvas Particle System
            ConfettiEffect()

            // Main Content Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, AssistiveBorder),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .align(Alignment.Center)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Close "X" Button top right
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .testTag("celebration_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = AssistiveTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // High-fidelity Reward Badge / Icon
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎉", fontSize = 48.sp)
                        }

                        Text(
                            text = "Super!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = AssistiveTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Du hast die neue Funktion freigeschaltet!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveBlue,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )

                        // Custom message explaining the unlocked app
                        val cleanMessage = message.replace(" wurde freigeschaltet. Viel Spaß und denke an das, was du gelernt hast!", "")
                        Text(
                            text = "Du kannst jetzt die Funktion \"$cleanMessage\" frei nutzen.",
                            fontSize = 15.sp,
                            color = AssistiveTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = "Los geht's!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiEffect() {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val sizeBase = remember { with(density) { 14.dp.toPx() } }
    
    val particles = remember {
        val list = mutableListOf<ConfettiParticle>()
        val colors = listOf(
            Color(0xFFFFC107), // Yellow
            Color(0xFFFF5722), // Orange
            Color(0xFFE91E63), // Pink
            Color(0xFF00BCD4), // Cyan
            Color(0xFF4CAF50), // Green
            Color(0xFF9C27B0)  // Purple
        )
        for (i in 0..65) {
            list.add(
                ConfettiParticle(
                    xFraction = Random.nextFloat(),
                    y = -Random.nextFloat() * 1500f,
                    size = Random.nextFloat() * sizeBase + sizeBase / 2,
                    color = colors.random(),
                    speed = Random.nextFloat() * 8f + 5f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = Random.nextFloat() * 5f - 2.5f
                )
            )
        }
        list
    }

    var frameState by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis { time ->
                frameState = time
                for (p in particles) {
                    p.y += p.speed
                    if (p.y > 2500f) {
                        p.y = -50f
                    }
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val redraw = frameState
        for (p in particles) {
            val drawX = p.xFraction * size.width
            val drawY = p.y
            if (drawY in -100f..size.height) {
                rotate(
                    degrees = p.rotation + (drawY * p.rotSpeed),
                    pivot = androidx.compose.ui.geometry.Offset(drawX, drawY)
                ) {
                    drawRect(
                        color = p.color,
                        topLeft = androidx.compose.ui.geometry.Offset(drawX - p.size / 2, drawY - p.size / 2),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size / 2)
                    )
                }
            }
        }
    }
}
