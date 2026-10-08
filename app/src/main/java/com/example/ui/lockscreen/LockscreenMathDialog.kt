package com.example.ui.lockscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.random.Random

/**
 * Super simple plus math challenge for kids after pulling the lock button:
 * e.g., "Was ist 2 + 3?"
 * Shows 3 big colorful answer buttons.
 * Correct answer directly unlocks to the home screen.
 */
@Composable
fun LockscreenMathDialog(
    onDismiss: () -> Unit,
    onSolved: () -> Unit
) {
    // Generate simple addition problem (numbers 1..5)
    val num1 = remember { Random.nextInt(1, 6) }
    val num2 = remember { Random.nextInt(1, 6) }
    val correctAnswer = num1 + num2

    // Generate 3 choices (1 correct, 2 plausible wrong)
    val choices = remember {
        val wrong1 = if (correctAnswer > 2) correctAnswer - 1 else correctAnswer + 2
        val wrong2 = if (correctAnswer < 9) correctAnswer + 1 else correctAnswer - 2
        listOf(correctAnswer, wrong1, wrong2).shuffled()
    }

    var selectedWrong by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("lockscreen_math_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header emoji
                Text(text = "⭐ 🎈 ⭐", fontSize = 28.sp)

                Text(
                    text = "Kleine Rechenaufgabe",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Löse die Plus-Aufgabe, um zum Startbildschirm zu gelangen:",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                // Math Equation Card
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$num1 + $num2 = ?",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309)
                    )
                }

                if (selectedWrong) {
                    Text(
                        text = "Fast! Versuche es noch einmal 😊",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3 Answer Option Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    choices.forEach { answer ->
                        val isCorrect = (answer == correctAnswer)
                        val btnColor = when (answer % 3) {
                            0 -> Color(0xFF2563EB) // Blue
                            1 -> Color(0xFF059669) // Green
                            else -> Color(0xFF7C3AED) // Purple
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(btnColor)
                                .clickable {
                                    if (isCorrect) {
                                        onSolved()
                                    } else {
                                        selectedWrong = true
                                    }
                                }
                                .testTag("math_choice_$answer"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = answer.toString(),
                                fontSize = 24.sp,
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
