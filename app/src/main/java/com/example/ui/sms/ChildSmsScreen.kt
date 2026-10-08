package com.example.ui.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherSettings
import com.example.sms.SmsMessageItem
import com.example.sms.SmsRepository
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.assistiveTextFieldColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsContactTarget(
    val title: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val colorHex: Long = 0xFF2563EB
)

/**
 * Child-friendly SMS screen with two clear steps:
 * 1. Contact selection window ("vorhandenen Kontakt auswählen")
 * 2. Chat / Compose & Send window ("nächste Fenster zum schreiben und absenden Button")
 */
@Composable
fun ChildSmsScreen(
    settings: LauncherSettings,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize SMS repository
    LaunchedEffect(Unit) {
        SmsRepository.init(context)
    }

    val allMessages by SmsRepository.messagesFlow.collectAsState()
    var selectedContact by remember { mutableStateOf<SmsContactTarget?>(null) }
    var isDefaultSmsApp by remember { mutableStateOf(SmsRepository.isDefaultSmsApp(context)) }

    // Role launcher for default SMS role
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultSmsApp = SmsRepository.isDefaultSmsApp(context)
    }

    // Permission launcher for SMS permissions
    val smsPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val hasSend = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val hasReceive = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val hasRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

        if (!hasSend || !hasReceive || !hasRead) {
            smsPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                )
            )
        }
    }

    BackHandler {
        if (selectedContact != null) {
            selectedContact = null
        } else {
            onBack()
        }
    }

    // Available contacts from settings + any contact in message history
    val availableContacts = remember(settings.contacts, allMessages) {
        val list = mutableListOf<SmsContactTarget>()
        val seenNumbers = mutableSetOf<String>()

        // 1. Load from parent-configured contacts list
        settings.contacts.forEach { contact ->
            val clean = contact.phoneNumber.trim()
            if (!seenNumbers.contains(clean)) {
                seenNumbers.add(clean)
                list.add(
                    SmsContactTarget(
                        title = contact.name,
                        phoneNumber = clean,
                        photoUri = contact.photoUri,
                        colorHex = contact.colorHex
                    )
                )
            }
        }

        // 2. Add sample/emergency numbers if empty
        if (list.isEmpty()) {
            list.add(
                SmsContactTarget(
                    title = "Mama",
                    phoneNumber = "555-0101",
                    colorHex = 0xFF059669
                )
            )
            list.add(
                SmsContactTarget(
                    title = "Papa",
                    phoneNumber = "555-0102",
                    colorHex = 0xFF2563EB
                )
            )
        }

        // 3. Any other phone number in message history
        allMessages.forEach { msg ->
            val clean = msg.phoneNumber.trim()
            if (clean.isNotBlank() && list.none { SmsRepository.isSamePhoneNumber(it.phoneNumber, clean) }) {
                list.add(
                    SmsContactTarget(
                        title = msg.senderName ?: clean,
                        phoneNumber = clean,
                        colorHex = 0xFF7C3AED
                    )
                )
            }
        }

        list
    }

    val parentConfiguredNumbers = remember(settings.contacts) {
        settings.contacts.map { it.phoneNumber.trim() }.toSet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AssistiveBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedContact == null) {
                // STEP 1: Select Contact Screen
                SmsContactSelectionView(
                    contacts = availableContacts,
                    allMessages = allMessages,
                    parentConfiguredNumbers = parentConfiguredNumbers,
                    onSelectContact = { contact -> selectedContact = contact },
                    onBack = onBack
                )
            } else {
                // STEP 2: Chat & Send Message Screen
                SmsChatView(
                    contact = selectedContact!!,
                    allMessages = allMessages,
                    onSendMessage = { body ->
                        SmsRepository.sendMessage(
                            context = context,
                            phoneNumber = selectedContact!!.phoneNumber,
                            body = body,
                            senderName = selectedContact!!.title
                        )
                    },
                    onBackToContacts = { selectedContact = null }
                )
            }
        }
    }
}

@Composable
private fun SwipeToDeleteContactCard(
    contact: SmsContactTarget,
    isDeletable: Boolean,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!isDeletable) {
        content()
        return
    }

    var offsetX by remember { mutableStateOf(0f) }

    val animatedOffset by androidx.compose.animation.core.animateFloatAsState(
        targetValue = offsetX,
        label = "swipe_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFFEF4444))
    ) {
        // Red background with Trash Icon and Löschen label
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Löschen",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Löschen",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Swipeable content box
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(contact.phoneNumber) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -180f) {
                                offsetX = -1000f
                                onDelete()
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            if (newOffset <= 0f && newOffset >= -320f) {
                                offsetX = newOffset
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

/**
 * Step 1 View: Select existing contact to message.
 */
@Composable
private fun SmsContactSelectionView(
    contacts: List<SmsContactTarget>,
    allMessages: List<SmsMessageItem>,
    parentConfiguredNumbers: Set<String>,
    onSelectContact: (SmsContactTarget) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = "Nachrichten",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Text(
                        text = "Wähle einen Kontakt aus",
                        fontSize = 14.sp,
                        color = AssistiveTextSecondary
                    )
                }
            }
        }

        // Contact Pillow Cards
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(contacts, key = { it.phoneNumber }) { contact ->
                val contactMsgs = allMessages.filter { SmsRepository.isSamePhoneNumber(it.phoneNumber, contact.phoneNumber) }
                val lastMsg = contactMsgs.lastOrNull()
                val unreadCount = contactMsgs.count { it.isIncoming && !it.isRead }
                val isParentContact = parentConfiguredNumbers.any { SmsRepository.isSamePhoneNumber(it, contact.phoneNumber) }
                val isDeletable = !isParentContact

                SwipeToDeleteContactCard(
                    contact = contact,
                    isDeletable = isDeletable,
                    onDelete = {
                        SmsRepository.deleteMessagesForContact(context, contact.phoneNumber)
                    }
                ) {
                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .clickable { onSelectContact(contact) }
                            .testTag("sms_contact_${contact.phoneNumber}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar with unread badge overlay
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(contact.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (contact.photoUri != null) {
                                        AsyncImage(
                                            model = contact.photoUri,
                                            contentDescription = contact.title,
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
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }

                                if (unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = contact.title,
                                    fontSize = 22.sp,
                                    fontWeight = if (unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                if (lastMsg != null) {
                                    Text(
                                        text = lastMsg.body,
                                        fontSize = 14.sp,
                                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (unreadCount > 0) Color(0xFFEF4444) else AssistiveTextSecondary,
                                        maxLines = 1
                                    )
                                } else {
                                    Text(
                                        text = contact.phoneNumber,
                                        fontSize = 13.sp,
                                        color = AssistiveTextMuted
                                    )
                                }
                            }

                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadCount.toString(),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = "Schatten",
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large full-width Back button at the very bottom
        LargeBackButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sms_contacts_back_button")
        )
    }
}

/**
 * Step 2 View: Chat history & Compose / Send SMS window with big buttons.
 */
@Composable
private fun SmsChatView(
    contact: SmsContactTarget,
    allMessages: List<SmsMessageItem>,
    onSendMessage: (String) -> Boolean,
    onBackToContacts: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val contactMessages = remember(allMessages, contact.phoneNumber) {
        allMessages.filter { SmsRepository.isSamePhoneNumber(it.phoneNumber, contact.phoneNumber) }.sortedBy { it.timestamp }
    }

    // Mark messages as read when viewing this chat
    LaunchedEffect(contact.phoneNumber, contactMessages.size) {
        SmsRepository.markMessagesAsReadFor(context, contact.phoneNumber)
        if (contactMessages.isNotEmpty()) {
            listState.animateScrollToItem(contactMessages.size - 1)
        }
    }

    val quickReplies = listOf(
        "Hallo! ❤️",
        "Mir geht es gut! 😊",
        "Kannst du mich anrufen? 📞",
        "Ich bin zuhause! 🏡",
        "Danke! 👍"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Chat Header
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(contact.colorHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (contact.photoUri != null) {
                            AsyncImage(
                                model = contact.photoUri,
                                contentDescription = contact.title,
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
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = contact.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )
                        Text(
                            text = contact.phoneNumber,
                            fontSize = 13.sp,
                            color = AssistiveTextMuted
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(contactMessages, key = { it.id }) { msg ->
                SmsBubble(msg = msg)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Reply Chips for Kids
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickReplies.take(3).forEach { reply ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (LocalIsDarkTheme.current) Color(0xFF312E81) else Color(0xFFEEF2FF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8)),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            onSendMessage(reply)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reply,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Compose Box: Full-Width 2-Line Text Field for Kid
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sms_input_field"),
            placeholder = { Text("Nachricht schreiben…", color = AssistiveTextMuted, fontSize = 18.sp) },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = AssistiveTextPrimary
            ),
            colors = assistiveTextFieldColors(),
            shape = RoundedCornerShape(20.dp),
            minLines = 2,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                        keyboardController?.hide()
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large Green Absenden Button (Icon Only - No "Senden" text)
        Button(
            onClick = {
                if (messageText.isNotBlank()) {
                    onSendMessage(messageText)
                    messageText = ""
                    keyboardController?.hide()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("send_sms_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AssistiveGreen,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFCBD5E1),
                disabledContentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            enabled = messageText.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Senden",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large full-width Back button at the very bottom
        LargeBackButton(
            onClick = onBackToContacts,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sms_chat_back_button")
        )
    }
}

@Composable
private fun SmsBubble(msg: SmsMessageItem) {
    val context = LocalContext.current
    val isIncoming = msg.isIncoming
    val align = if (isIncoming) Alignment.Start else Alignment.End
    val bg = if (isIncoming) {
        if (LocalIsDarkTheme.current) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    } else {
        if (msg.status == com.example.sms.SmsSendStatus.FAILED) {
            Color(0xFFDC2626)
        } else {
            AssistiveGreen
        }
    }
    val textColor = if (isIncoming) AssistiveTextPrimary else Color.White

    val timeStr = remember(msg.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
    }

    val statusStr = if (isIncoming) "" else {
        when (msg.status) {
            com.example.sms.SmsSendStatus.PROCESSING -> "  ○"
            com.example.sms.SmsSendStatus.SENT -> "  ✓"
            com.example.sms.SmsSendStatus.DELIVERED -> "  ✓✓"
            com.example.sms.SmsSendStatus.FAILED -> "  ❌ Senden fehlgeschlagen (Tippen zum Erneut Senden)"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isIncoming) 4.dp else 20.dp,
                bottomEnd = if (isIncoming) 20.dp else 4.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bg),
            border = if (isIncoming) androidx.compose.foundation.BorderStroke(1.dp, AssistiveBorder) else null,
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .then(
                    if (!isIncoming && msg.status == com.example.sms.SmsSendStatus.FAILED) {
                        Modifier.clickable {
                            com.example.sms.SmsRepository.retrySendMessage(context, msg.id)
                        }
                    } else Modifier
                )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = msg.body,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = "$timeStr$statusStr",
                    fontSize = 11.sp,
                    color = if (isIncoming) AssistiveTextMuted else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
