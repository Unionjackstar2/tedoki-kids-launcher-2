package com.example.ui.quiz

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.quiz.QuizCategory
import com.example.ui.theme.AssistiveAmber
import com.example.ui.theme.AssistiveAmberLight
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBlueLight
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveGreenLight
import com.example.ui.theme.AssistivePink
import com.example.ui.theme.AssistivePurple
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary

enum class ComicType {
    INTRO_QUIZ_HERO,
    INTRO_CAMERA,
    INTRO_CALL,
    INTRO_WHATSAPP,
    INTRO_PHOTOS,
    INTRO_MESSAGES,
    INTRO_SURF,
    QUESTION_ASK_PHOTO,
    QUESTION_CALL_CONTACT,
    QUESTION_CHAT_MESSAGE,
    FEEDBACK_SUCCESS,
    FEEDBACK_RETRY,
    PARENT_DISCUSSION
}

fun getComicDrawableNameForQuestion(questionId: String?): String? {
    if (questionId == null) return null
    return when (questionId) {
        // ANRUFEN
        "c_call_1" -> "comic_call_mom"
        "p_call_1" -> "comic_parent_call_mom"
        "c_call_2" -> "comic_call_accidental"
        "p_call_2" -> "comic_parent_call_accidental"
        "c_call_3" -> "comic_call_unreachable"
        "p_call_3" -> "comic_parent_call_unreachable"

        // UNBEKANNTE NUMMERN
        "c_unk_1", "p_unk_1" -> "comic_unknown_caller"
        "c_unk_2", "p_unk_2" -> "comic_no_address"
        "c_unk_3", "p_unk_3" -> "comic_block_number"

        // NACHRICHTEN
        "c_msg_1", "p_msg_1" -> "comic_message_time"
        "c_msg_2", "p_msg_2" -> "comic_question_chat"
        "c_msg_3", "p_msg_3" -> "comic_mean_message"

        // WHATSAPP
        "c_wa_1", "p_wa_1" -> "comic_whatsapp_group"
        "c_wa_2", "p_wa_2" -> "comic_unknown_caller"
        "c_wa_3", "p_wa_3", "p_wa_2" -> "comic_group_photo_stop"

        // FOTOS MACHEN
        "c_cam_1", "p_cam_1" -> "comic_ask_photo"
        "c_cam_2", "p_cam_2" -> "comic_delete_photo"
        "c_cam_3", "p_cam_3" -> "comic_photo_background"

        // FOTOS VON ANDEREN
        "c_oth_1", "p_oth_1" -> "comic_group_photo_stop"
        "c_oth_2", "p_oth_2", "p_oth_3" -> "comic_no_address"

        // SPRACHNACHRICHTEN
        "c_vn_1", "c_vn_2", "p_vn_1", "p_vn_2" -> "comic_voice_note"

        // BLOCKIEREN
        "c_blk_1", "c_blk_2", "p_blk_1", "p_blk_2" -> "comic_block_number"

        // PERSÖNLICHE DATEN
        "c_dat_1", "c_dat_2", "p_dat_1", "p_dat_2", "p_dat_3" -> "comic_no_address"

        // BEVOR VERSCHICKEN
        "c_snd_1", "c_snd_2", "p_snd_1", "p_snd_2", "p_snd_3" -> "comic_think_before_send"

        // INTERNET SURFEN
        "c_surf_1", "p_surf_2" -> "comic_creepy_website"
        "c_surf_2", "p_surf_1" -> "comic_walled_garden"
        "c_surf_3", "p_surf_4" -> "comic_ad_trap"
        "c_surf_4", "p_surf_3" -> "comic_no_address"

        else -> null
    }
}

/**
 * Comic illustration card that visually depicts the situation
 * matching the colorful aesthetic in the user mockups.
 */
@Composable
fun ComicIllustrationCard(
    type: ComicType,
    modifier: Modifier = Modifier.height(148.dp),
    questionId: String? = null
) {
    val context = LocalContext.current
    val customDrawableId = remember(type, questionId) {
        val specificName = getComicDrawableNameForQuestion(questionId)
        val candidates = mutableListOf<String>()
        if (specificName != null) {
            candidates.add(specificName)
        }
        val typeCandidates = when (type) {
            ComicType.INTRO_QUIZ_HERO -> listOf("comic_quiz_intro", "quiz_intro_boy", "comic_thumbs_up")
            ComicType.INTRO_CAMERA -> listOf("comic_intro_camera", "comic_ask_photo", "comic_quiz_intro")
            ComicType.INTRO_CALL -> listOf("comic_intro_call", "comic_question_call", "comic_quiz_intro")
            ComicType.INTRO_WHATSAPP -> listOf("comic_intro_whatsapp", "comic_whatsapp_group", "comic_question_chat")
            ComicType.INTRO_PHOTOS -> listOf("comic_intro_photos", "comic_ask_photo", "comic_group_photo_stop")
            ComicType.INTRO_MESSAGES -> listOf("comic_intro_messages", "comic_message_time", "comic_question_chat", "comic_quiz_intro")
            ComicType.INTRO_SURF -> listOf("comic_intro_surf", "comic_creepy_website", "comic_ad_trap", "comic_quiz_intro")
            ComicType.QUESTION_ASK_PHOTO -> listOf("comic_ask_photo")
            ComicType.FEEDBACK_SUCCESS -> listOf("comic_thumbs_up", "comic_quiz_intro")
            ComicType.PARENT_DISCUSSION -> listOf("comic_parent_discuss")
            ComicType.QUESTION_CALL_CONTACT -> listOf("comic_question_call", "comic_call_mom")
            ComicType.QUESTION_CHAT_MESSAGE -> listOf("comic_question_chat", "comic_message_time")
            ComicType.FEEDBACK_RETRY -> listOf("comic_feedback_retry")
        }
        candidates.addAll(typeCandidates)

        var found = 0
        for (name in candidates) {
            try {
                val id = context.resources.getIdentifier(name, "drawable", context.packageName)
                if (id != 0) {
                    found = id
                    break
                }
            } catch (e: Exception) {}
        }
        found
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF1F5F9),
                            Color(0xFFE2E8F0).copy(alpha = 0.6f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (customDrawableId != 0) {
                Image(
                    painter = painterResource(id = customDrawableId),
                    contentDescription = "Comic Illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )
            } else {
                when (type) {
                    ComicType.INTRO_QUIZ_HERO -> ComicIntroQuizHero()
                    ComicType.INTRO_CAMERA -> ComicIntroCamera()
                    ComicType.INTRO_CALL -> ComicIntroCall()
                    ComicType.INTRO_WHATSAPP -> ComicIntroWhatsApp()
                    ComicType.INTRO_PHOTOS -> ComicIntroPhotos()
                    ComicType.INTRO_MESSAGES -> ComicIntroWhatsApp()
                    ComicType.INTRO_SURF -> ComicIntroCamera()
                    ComicType.QUESTION_ASK_PHOTO -> ComicQuestionAskPhoto()
                    ComicType.QUESTION_CALL_CONTACT -> ComicQuestionCallContact()
                    ComicType.QUESTION_CHAT_MESSAGE -> ComicQuestionChatMessage()
                    ComicType.FEEDBACK_SUCCESS -> ComicFeedbackSuccess()
                    ComicType.FEEDBACK_RETRY -> ComicFeedbackRetry()
                    ComicType.PARENT_DISCUSSION -> ComicParentDiscussion()
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Individual Comic Scenes
// -------------------------------------------------------------

/**
 * Quiz Intro Hero (Thumbs-up Boy)
 */
@Composable
private fun ComicIntroQuizHero() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                    )
                )
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDBEAFE))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "👍 LOS GEHT'S!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bereit für das Quiz?",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )
            Text(
                text = "2 kurze Fragen zum Start",
                fontSize = 12.sp,
                color = AssistiveTextSecondary
            )
        }
    }
}

/**
 * Intro Camera Scene
 */
@Composable
private fun ComicIntroCamera() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AssistiveBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = AssistiveBlue,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Floating Photo Card
        Box(
            modifier = Modifier
                .size(width = 82.dp, height = 90.dp)
                .shadow(3.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(2.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF67E8F9), Color(0xFF34D399))
                            )
                        ),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24))
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Foto & Momente",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextSecondary
                )
            }
        }
    }
}

/**
 * Question: "Darf ich das fotografieren?"
 * Left: Boy holding phone to take photo.
 * Right: Girl raising hand saying "stop / nicht ohne mich zu fragen".
 */
@Composable
private fun ComicQuestionAskPhoto() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(AssistiveBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = AssistiveBlue,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF3C7))
                    .border(1.5.dp, AssistiveAmber, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✋ Bitte vorher fragen!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recht am eigenen Bild",
                fontSize = 11.sp,
                color = AssistiveTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Feedback Success Scene
 */
@Composable
private fun ComicFeedbackSuccess() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AssistiveGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Richtig",
                tint = AssistiveGreen,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD1FAE5))
                    .border(1.5.dp, AssistiveGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "👍 Super gelöst!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Medienkompetenz verstanden",
                fontSize = 11.sp,
                color = AssistiveTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Feedback Retry Scene
 */
@Composable
private fun ComicFeedbackRetry() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AssistiveAmberLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = AssistiveAmber,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF3C7))
                    .border(1.5.dp, AssistiveAmber, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💡 Nochmal in Ruhe lesen",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Du schaffst das gleich!",
                fontSize = 11.sp,
                color = AssistiveTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Parent Quiz Scene
 */
@Composable
private fun ComicParentDiscussion() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AssistiveBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = AssistiveBlue,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFDBEAFE))
                    .border(1.5.dp, AssistiveBlue, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💬 Gemeinsam besprechen",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveBlue
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Medienbegleitung in der Familie",
                fontSize = 11.sp,
                color = AssistiveTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Comic Intro Phone / Calling
 */
@Composable
private fun ComicIntroCall() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color(0xFFFED7AA)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 62.dp, height = 22.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color(0xFF78350F))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF1E293B)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF1E293B)))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 6.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color(0xFFDC2626))
                )
            }
        }

        Spacer(modifier = Modifier.width(28.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AssistiveGreen)
                .shadow(4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

/**
 * Comic Intro WhatsApp / Chatting
 */
@Composable
private fun ComicIntroWhatsApp() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF25D366)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Chat bubble with smiley
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFF25D366), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hallo! 😊👋",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}

/**
 * Comic Intro Photos
 */
@Composable
private fun ComicIntroPhotos() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(AssistivePink),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Photo,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 76.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(2.dp, AssistivePink, RoundedCornerShape(14.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = AssistivePink,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Question: Contact calling
 */
@Composable
private fun ComicQuestionCallContact() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFFFED7AA)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 18.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color(0xFF78350F))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1E293B)))
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1E293B)))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 5.dp)
                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(Color(0xFFDC2626))
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Phone screen showing "Mama"
        Box(
            modifier = Modifier
                .size(width = 96.dp, height = 62.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(2.dp, AssistiveGreen, RoundedCornerShape(12.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = AssistiveGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mama",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

/**
 * Question: Chat Message received
 */
@Composable
private fun ComicQuestionChatMessage() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFFFED7AA)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 18.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color(0xFF78350F))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1E293B)))
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF1E293B)))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 5.dp)
                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(Color(0xFFDC2626))
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Message bubble
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 56.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(AssistiveBlueLight)
                .border(1.5.dp, AssistiveBlue, RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = AssistiveBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nachricht",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveBlue
                )
            }
        }
    }
}
