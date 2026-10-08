package com.example.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.quiz.PedagogyQuizData
import com.example.model.quiz.QuizCategory
import com.example.model.quiz.QuizOption
import com.example.model.quiz.ReviewDueState
import com.example.util.QuizSpeechManager
import com.example.ui.theme.AssistiveAmber
import com.example.ui.theme.AssistiveAmberLight
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBlueLight
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveGreenLight
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveRedLight
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary

enum class QuizFlowStep {
    CHILD_INTRO,
    CHILD_QUESTION,
    CHILD_FEEDBACK,
    CHILD_SUMMARY,
    HANDOFF_TO_PARENT,
    PARENT_INTRO,
    PARENT_QUESTION,
    PARENT_FEEDBACK,
    PARENT_TIPS,
    PARENT_PIN
}

@Composable
fun QuizFlowScreen(
    appName: String,
    category: QuizCategory,
    reviewState: ReviewDueState = ReviewDueState.NONE,
    parentPin: String,
    onCompleteUnlocked: () -> Unit,
    onCancel: () -> Unit
) {
    BackHandler { onCancel() }

    val module = remember(category) { PedagogyQuizData.getModule(category) }

    var currentStep by remember { mutableStateOf(QuizFlowStep.CHILD_INTRO) }

    // Child quiz state
    var childQuestionIdx by remember { mutableIntStateOf(0) }
    var childSelectedIdx by remember { mutableStateOf<Int?>(null) }
    var childIsCorrect by remember { mutableStateOf<Boolean?>(null) }

    // Parent quiz state
    var parentQuestionIdx by remember { mutableIntStateOf(0) }
    var parentSelectedIdx by remember { mutableStateOf<Int?>(null) }
    var parentIsCorrect by remember { mutableStateOf<Boolean?>(null) }

    // PIN state
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val appIcon: ImageVector = remember(category) {
        when (category) {
            QuizCategory.ANRUFEN, QuizCategory.UNBEKANNTE_NUMMERN -> Icons.Default.Phone
            QuizCategory.FOTOS_MACHEN -> Icons.Default.CameraAlt
            QuizCategory.FOTOS_VON_ANDEREN, QuizCategory.BEVOR_VERSCHICKEN -> Icons.Default.PhotoLibrary
            QuizCategory.WHATSAPP, QuizCategory.NACHRICHTEN, QuizCategory.SPRACHNACHRICHTEN -> Icons.Default.Chat
            QuizCategory.BLOCKIEREN, QuizCategory.PERSOENLICHE_DATEN -> Icons.Default.Security
            QuizCategory.INTERNET_SURFEN -> Icons.Default.Language
        }
    }

    val context = LocalContext.current
    val speechManager = remember { QuizSpeechManager(context) }
    val isSpeaking by speechManager.isSpeaking

    DisposableEffect(Unit) {
        onDispose {
            speechManager.shutdown()
        }
    }

    // Stop speaking automatically whenever the step or question changes
    LaunchedEffect(currentStep, childQuestionIdx, parentQuestionIdx) {
        speechManager.stop()
    }

    val currentSpeechText = remember(
        currentStep,
        childQuestionIdx,
        childIsCorrect,
        parentQuestionIdx,
        parentIsCorrect,
        appName,
        module,
        reviewState
    ) {
        when (currentStep) {
            QuizFlowStep.CHILD_INTRO -> {
                val reviewText = if (reviewState != ReviewDueState.NONE) {
                    "Wir schauen heute noch einmal gemeinsam, was du über $appName weißt."
                } else {
                    "Bevor du $appName benutzen kannst, schauen wir uns gemeinsam an, was dabei wichtig ist."
                }
                "$appName. ${module.childIntroText}. $reviewText"
            }
            QuizFlowStep.CHILD_QUESTION -> {
                val q = module.childQuestions.getOrNull(childQuestionIdx)
                if (q != null) {
                    val optionsSpoken = q.options.joinToString(". ") { "Antwort ${it.id}: ${it.text}" }
                    "Frage ${childQuestionIdx + 1} von ${module.childQuestions.size}. ${q.title}. ${q.situation}. Die Antwortmöglichkeiten lauten: $optionsSpoken"
                } else ""
            }
            QuizFlowStep.CHILD_FEEDBACK -> {
                val q = module.childQuestions.getOrNull(childQuestionIdx)
                val statusText = if (childIsCorrect == true) "Genau richtig! Super gemacht!" else "Nochmal überlegen!"
                if (q != null) "$statusText. ${q.explanation}" else statusText
            }
            QuizFlowStep.CHILD_SUMMARY -> {
                "Das hast du gelernt: " + module.childSummaryPoints.joinToString(". ")
            }
            QuizFlowStep.HANDOFF_TO_PARENT -> {
                "Jetzt sind deine Eltern dran! Deine Eltern beantworten ein paar Fragen, um $appName für dich freizugeben. Sie erfahren dabei auch, worauf sie achten sollten."
            }
            QuizFlowStep.PARENT_INTRO -> {
                "$appName. Warum ist das wichtig? ${module.parentIntroText}. Wichtige Themen sind: " + module.parentFocusTopics.joinToString(". ")
            }
            QuizFlowStep.PARENT_QUESTION -> {
                val q = module.parentQuestions.getOrNull(parentQuestionIdx)
                if (q != null) {
                    val optionsSpoken = q.options.joinToString(". ") { "Option ${it.id}: ${it.text}" }
                    "Frage ${parentQuestionIdx + 1} von ${module.parentQuestions.size}. ${q.title}. ${q.situation}. Die Optionen lauten: $optionsSpoken"
                } else ""
            }
            QuizFlowStep.PARENT_FEEDBACK -> {
                val q = module.parentQuestions.getOrNull(parentQuestionIdx)
                val statusText = if (parentIsCorrect == true) "Genau richtig!" else "Nochmal überlegen!"
                if (q != null) "$statusText. ${q.explanation}" else statusText
            }
            QuizFlowStep.PARENT_TIPS -> {
                "Tipps für Eltern bei $appName: " + module.parentTips.joinToString(". ")
            }
            QuizFlowStep.PARENT_PIN -> {
                "Bitte gib deine vierstellige Eltern-PIN ein, um die App freizuschalten."
            }
        }
    }

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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with TTS Vorlesen button and cancel X button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEDOKI Kids",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Vorlesen / Stopp on-demand Pill Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSpeaking) AssistiveRedLight else AssistiveBlueLight,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSpeaking) AssistiveRed else AssistiveBlue
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (isSpeaking) {
                                    speechManager.stop()
                                } else {
                                    speechManager.speak(currentSpeechText)
                                }
                            }
                            .testTag("quiz_tts_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isSpeaking) "Vorlesen beenden" else "Text vorlesen",
                                tint = if (isSpeaking) AssistiveRed else AssistiveBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isSpeaking) "Stopp" else "Vorlesen",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSpeaking) AssistiveRed else AssistiveBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            speechManager.stop()
                            onCancel()
                        },
                        modifier = Modifier.testTag("quiz_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = AssistiveTextSecondary
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "quiz_step_transition",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    QuizFlowStep.CHILD_INTRO -> {
                        ChildIntroView(
                            appName = appName,
                            category = category,
                            icon = appIcon,
                            introText = module.childIntroText,
                            isReview = reviewState != ReviewDueState.NONE,
                            onStart = {
                                childQuestionIdx = 0
                                childSelectedIdx = null
                                childIsCorrect = null
                                currentStep = QuizFlowStep.CHILD_QUESTION
                            }
                        )
                    }

                    QuizFlowStep.CHILD_QUESTION -> {
                        val question = module.childQuestions[childQuestionIdx]
                        val childComic = when (category) {
                            QuizCategory.FOTOS_MACHEN, QuizCategory.FOTOS_VON_ANDEREN, QuizCategory.BEVOR_VERSCHICKEN -> ComicType.QUESTION_ASK_PHOTO
                            QuizCategory.ANRUFEN, QuizCategory.UNBEKANNTE_NUMMERN -> ComicType.QUESTION_CALL_CONTACT
                            QuizCategory.WHATSAPP, QuizCategory.NACHRICHTEN, QuizCategory.SPRACHNACHRICHTEN, QuizCategory.BLOCKIEREN -> ComicType.QUESTION_CHAT_MESSAGE
                            else -> ComicType.QUESTION_ASK_PHOTO
                        }
                        QuestionView(
                            stepTitle = "${childQuestionIdx + 1}/${module.childQuestions.size}",
                            questionTitle = question.title,
                            situationText = question.situation,
                            questionId = question.id,
                            comicType = childComic,
                            options = question.options,
                            selectedIndex = childSelectedIdx,
                            onOptionSelected = { idx -> childSelectedIdx = idx },
                            onNext = {
                                if (childSelectedIdx != null) {
                                    childIsCorrect = (childSelectedIdx == question.correctIndex)
                                    currentStep = QuizFlowStep.CHILD_FEEDBACK
                                }
                            },
                            onSpeakSituation = {
                                val optionsSpoken = question.options.joinToString(". ") { "Antwort ${it.id}: ${it.text}" }
                                speechManager.speak("${question.title}. ${question.situation}. Die Antwortmöglichkeiten lauten: $optionsSpoken")
                            },
                            onSpeakOption = { opt ->
                                speechManager.speak("Antwort ${opt.id}: ${opt.text}")
                            }
                        )
                    }

                    QuizFlowStep.CHILD_FEEDBACK -> {
                        val question = module.childQuestions[childQuestionIdx]
                        val isCorrect = childIsCorrect == true
                        FeedbackView(
                            stepTitle = "${childQuestionIdx + 1}/${module.childQuestions.size}",
                            isCorrect = isCorrect,
                            explanationText = question.explanation,
                            questionId = question.id,
                            onNext = {
                                if (isCorrect) {
                                    if (childQuestionIdx + 1 < module.childQuestions.size) {
                                        childQuestionIdx++
                                        childSelectedIdx = null
                                        childIsCorrect = null
                                        currentStep = QuizFlowStep.CHILD_QUESTION
                                    } else {
                                        currentStep = QuizFlowStep.CHILD_SUMMARY
                                    }
                                } else {
                                    // Retry the question
                                    childSelectedIdx = null
                                    childIsCorrect = null
                                    currentStep = QuizFlowStep.CHILD_QUESTION
                                }
                            },
                            onSpeakExplanation = {
                                val statusText = if (isCorrect) "Genau richtig! Super gemacht!" else "Nochmal überlegen!"
                                speechManager.speak("$statusText. ${question.explanation}")
                            }
                        )
                    }

                    QuizFlowStep.CHILD_SUMMARY -> {
                        ChildSummaryView(
                            summaryPoints = module.childSummaryPoints,
                            onNext = {
                                currentStep = QuizFlowStep.HANDOFF_TO_PARENT
                            }
                        )
                    }

                    QuizFlowStep.HANDOFF_TO_PARENT -> {
                        HandoffToParentView(
                            appName = appName,
                            onProceed = {
                                currentStep = QuizFlowStep.PARENT_INTRO
                            }
                        )
                    }

                    QuizFlowStep.PARENT_INTRO -> {
                        ParentIntroView(
                            appName = appName,
                            icon = appIcon,
                            introText = module.parentIntroText,
                            focusTopics = module.parentFocusTopics,
                            onStart = {
                                parentQuestionIdx = 0
                                parentSelectedIdx = null
                                parentIsCorrect = null
                                currentStep = QuizFlowStep.PARENT_QUESTION
                            }
                        )
                    }

                    QuizFlowStep.PARENT_QUESTION -> {
                        val question = module.parentQuestions[parentQuestionIdx]
                        QuestionView(
                            stepTitle = "${parentQuestionIdx + 1}/${module.parentQuestions.size}",
                            questionTitle = question.title,
                            situationText = question.situation,
                            questionId = question.id,
                            comicType = ComicType.PARENT_DISCUSSION,
                            options = question.options,
                            selectedIndex = parentSelectedIdx,
                            onOptionSelected = { idx -> parentSelectedIdx = idx },
                            onNext = {
                                if (parentSelectedIdx != null) {
                                    parentIsCorrect = (parentSelectedIdx == question.correctIndex)
                                    currentStep = QuizFlowStep.PARENT_FEEDBACK
                                }
                            },
                            onSpeakSituation = {
                                val optionsSpoken = question.options.joinToString(". ") { "Option ${it.id}: ${it.text}" }
                                speechManager.speak("${question.title}. ${question.situation}. Die Optionen lauten: $optionsSpoken")
                            },
                            onSpeakOption = { opt ->
                                speechManager.speak("Option ${opt.id}: ${opt.text}")
                            }
                        )
                    }

                    QuizFlowStep.PARENT_FEEDBACK -> {
                        val question = module.parentQuestions[parentQuestionIdx]
                        val isCorrect = parentIsCorrect == true
                        FeedbackView(
                            stepTitle = "${parentQuestionIdx + 1}/${module.parentQuestions.size}",
                            isCorrect = isCorrect,
                            explanationText = question.explanation,
                            questionId = question.id,
                            onNext = {
                                if (isCorrect) {
                                    if (parentQuestionIdx + 1 < module.parentQuestions.size) {
                                        parentQuestionIdx++
                                        parentSelectedIdx = null
                                        parentIsCorrect = null
                                        currentStep = QuizFlowStep.PARENT_QUESTION
                                    } else {
                                        currentStep = QuizFlowStep.PARENT_TIPS
                                    }
                                } else {
                                    parentSelectedIdx = null
                                    parentIsCorrect = null
                                    currentStep = QuizFlowStep.PARENT_QUESTION
                                }
                            },
                            onSpeakExplanation = {
                                val statusText = if (isCorrect) "Genau richtig!" else "Nochmal überlegen!"
                                speechManager.speak("$statusText. ${question.explanation}")
                            }
                        )
                    }

                    QuizFlowStep.PARENT_TIPS -> {
                        ParentTipsView(
                            appName = appName,
                            tips = module.parentTips,
                            onNext = {
                                enteredPin = ""
                                pinError = false
                                currentStep = QuizFlowStep.PARENT_PIN
                            }
                        )
                    }

                    QuizFlowStep.PARENT_PIN -> {
                        ParentPinUnlockView(
                            enteredPin = enteredPin,
                            pinError = pinError,
                            onDigitEntered = { digit ->
                                if (enteredPin.length < 6) {
                                    pinError = false
                                    enteredPin += digit
                                    if (enteredPin == parentPin) {
                                        onCompleteUnlocked()
                                    } else if (enteredPin.length >= parentPin.length) {
                                        pinError = true
                                    }
                                }
                            },
                            onDelete = {
                                if (enteredPin.isNotEmpty()) {
                                    enteredPin = enteredPin.dropLast(1)
                                    pinError = false
                                }
                            },
                            onClear = {
                                enteredPin = ""
                                pinError = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Screen Composable Views
// -------------------------------------------------------------

@Composable
private fun ChildIntroView(
    appName: String,
    category: QuizCategory,
    icon: ImageVector,
    introText: String,
    isReview: Boolean,
    onStart: () -> Unit
) {
    val introComic = when (category) {
        QuizCategory.ANRUFEN -> ComicType.INTRO_CALL
        QuizCategory.NACHRICHTEN -> ComicType.INTRO_MESSAGES
        QuizCategory.FOTOS_MACHEN -> ComicType.INTRO_CAMERA
        QuizCategory.FOTOS_VON_ANDEREN -> ComicType.INTRO_PHOTOS
        QuizCategory.INTERNET_SURFEN -> ComicType.INTRO_SURF
        QuizCategory.WHATSAPP -> ComicType.INTRO_WHATSAPP
        QuizCategory.UNBEKANNTE_NUMMERN -> ComicType.INTRO_CALL
        QuizCategory.BEVOR_VERSCHICKEN -> ComicType.INTRO_PHOTOS
        else -> ComicType.INTRO_CAMERA
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AssistiveBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = appName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Comic Illustration: Category-specific intro illustration
            ComicIllustrationCard(
                type = introComic,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = introText,
                fontSize = 15.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Yellow Info Callout
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveAmberLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AssistiveAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Wichtig",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (isReview) {
                            "Wir schauen heute noch einmal gemeinsam, was du über $appName weißt."
                        } else {
                            "Bevor du $appName benutzen kannst, schauen wir uns gemeinsam an, was dabei wichtig ist."
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF78350F),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Button(
            onClick = onStart,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
                .testTag("quiz_start_button")
        ) {
            Text(
                text = "Los geht\'s!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun QuestionView(
    stepTitle: String,
    questionTitle: String,
    situationText: String,
    questionId: String? = null,
    comicType: ComicType? = null,
    options: List<QuizOption>,
    selectedIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit,
    onSpeakSituation: (() -> Unit)? = null,
    onSpeakOption: ((QuizOption) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AssistiveGreenLight)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stepTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val isPhoneQuestion = questionId in setOf("c_call_1", "c_call_2", "c_call_3", "p_call_1", "p_call_2", "p_call_3")

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = questionTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isPhoneQuestion && onSpeakSituation != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onSpeakSituation,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("quiz_speak_situation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Frage vorlesen",
                            tint = AssistiveBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Comic Illustration matching question topic
            Spacer(modifier = Modifier.height(10.dp))
            val cardHeight = if (isPhoneQuestion) 350.dp else 148.dp
            ComicIllustrationCard(
                type = comicType ?: ComicType.QUESTION_ASK_PHOTO,
                questionId = questionId,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(cardHeight)
            )

            if (!isPhoneQuestion) {
                Spacer(modifier = Modifier.height(10.dp))

                // Situation card with on-demand audio read-aloud button
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (onSpeakSituation != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Situation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextMuted
                            )
                            IconButton(
                                onClick = onSpeakSituation,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("quiz_speak_situation_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Frage und Situation vorlesen",
                                    tint = AssistiveBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = situationText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AssistiveTextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

            // Options
            options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                val borderCol = if (isSelected) AssistiveBlue else AssistiveBorder
                val bgCol = if (isSelected) AssistiveBlueLight else AssistiveSurface

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bgCol),
                    border = androidx.compose.foundation.BorderStroke(2.dp, borderCol),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOptionSelected(index) }
                        .testTag("option_${option.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AssistiveBlue else AssistiveBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.id,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else AssistiveTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = option.text,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = AssistiveTextPrimary,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (onSpeakOption != null) {
                            IconButton(
                                onClick = { onSpeakOption(option) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("quiz_speak_option_${option.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Option ${option.id} vorlesen",
                                    tint = if (isSelected) AssistiveBlue else AssistiveTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            enabled = selectedIndex != null,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AssistiveBlue,
                disabledContainerColor = AssistiveBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
                .testTag("quiz_next_button")
        ) {
            Text(
                text = "Weiter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FeedbackView(
    stepTitle: String,
    isCorrect: Boolean,
    explanationText: String,
    questionId: String? = null,
    onNext: () -> Unit,
    onSpeakExplanation: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCorrect) AssistiveGreenLight else AssistiveAmberLight)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stepTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) AssistiveGreen else AssistiveAmber
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comic Illustration matching question topic
            ComicIllustrationCard(
                type = if (isCorrect) ComicType.FEEDBACK_SUCCESS else ComicType.FEEDBACK_RETRY,
                questionId = if (isCorrect) questionId else null,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isCorrect) "Genau richtig!" else "Nochmal überlegen!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) AssistiveGreen else AssistiveAmber
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (onSpeakExplanation != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Erklärung",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextMuted
                            )
                            IconButton(
                                onClick = onSpeakExplanation,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("quiz_speak_feedback_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Erklärung vorlesen",
                                    tint = if (isCorrect) AssistiveGreen else AssistiveAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(
                        text = explanationText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = AssistiveTextPrimary,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCorrect) AssistiveBlue else AssistiveAmber
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
                .testTag("quiz_feedback_next_button")
        ) {
            Text(
                text = if (isCorrect) "Weiter" else "Nochmal versuchen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ChildSummaryView(
    summaryPoints: List<String>,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Das hast du gelernt",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComicIllustrationCard(
                type = ComicType.FEEDBACK_SUCCESS,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    summaryPoints.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AssistiveGreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Gelernt",
                                    tint = AssistiveGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = point,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = AssistiveTextPrimary,
                                lineHeight = 22.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
                .testTag("child_summary_next_button")
        ) {
            Text(
                text = "Weiter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun HandoffToParentView(
    appName: String,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jetzt sind deine Eltern dran",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComicIllustrationCard(
                type = ComicType.PARENT_DISCUSSION,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Deine Eltern beantworten ein paar Fragen, um $appName für dich freizugeben. Sie erfahren dabei auch, worauf sie achten sollten.",
                fontSize = 16.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Button(
            onClick = onProceed,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
                .testTag("handoff_to_parent_button")
        ) {
            Text(
                text = "Weiter zu den Eltern",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ParentIntroView(
    appName: String,
    icon: ImageVector,
    introText: String,
    focusTopics: List<String>,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(AssistiveBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = appName,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Column {
                    Text(
                        text = appName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Text(
                        text = "Eltern-Einführung",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AssistiveTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ComicIllustrationCard(
                type = ComicType.PARENT_DISCUSSION,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = introText,
                fontSize = 15.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    focusTopics.forEach { topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AssistiveBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AssistiveBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = topic,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = AssistiveTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onStart,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
                .testTag("parent_start_quiz_button")
        ) {
            Text(
                text = "Quiz starten",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ParentTipsView(
    appName: String,
    tips: List<String>,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Yellow warning box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveAmberLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AssistiveAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Bitte beachten",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Mit $appName hat Ihr Kind Zugriff auf erweiterte Funktionen. Besprechen Sie gemeinsam, wann und wo die App genutzt wird und wie Regeln eingehalten werden.",
                        fontSize = 14.sp,
                        color = Color(0xFF78350F),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    tips.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AssistiveBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AssistiveBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = tip,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = AssistiveTextPrimary,
                                lineHeight = 21.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 16.dp)
                .testTag("parent_tips_next_button")
        ) {
            Text(
                text = "Weiter zur Freigabe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ParentPinUnlockView(
    enteredPin: String,
    pinError: Boolean,
    onDigitEntered: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AssistiveBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Eltern-PIN",
                    tint = AssistiveBlue,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Eltern-PIN eingeben",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Geben Sie Ihre PIN ein, um die App für Ihr Kind freizuschalten.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots (● ● ● ○)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotColor = if (pinError) AssistiveRed else if (isFilled) AssistiveBlue else AssistiveBorder
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            if (pinError) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Falsche PIN. Bitte erneut versuchen.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AssistiveRed
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "DEL")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(AssistiveSurface)
                                .border(1.dp, AssistiveBorder, CircleShape)
                                .clickable {
                                    when (key) {
                                        "C" -> onClear()
                                        "DEL" -> onDelete()
                                        else -> onDigitEntered(key)
                                    }
                                }
                                .testTag("pin_key_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "DEL") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Löschen",
                                    tint = AssistiveTextPrimary
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AssistiveTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
