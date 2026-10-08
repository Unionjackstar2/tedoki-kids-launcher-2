package com.example.ui.calling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary

/**
 * Modern Android-inspired Dialpad Bottom Sheet for Children.
 * Slides up from the bottom with large round touch-friendly number keys,
 * backspace, clear display, and wide green call action button.
 */
@Composable
fun KeypadDialerSheet(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onCallNumber: (String) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        var inputNumber by remember { mutableStateOf("") }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("keypad_dialer_sheet"),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = AssistiveSurface,
            tonalElevation = 12.dp,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, AssistiveBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with centered drag handle and round close button in the corner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    // Centered subtle drag bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(44.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFCBD5E1))
                    )

                    // Large circular close button (X) in the top-right corner with design matching the LargeBackButton
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("keypad_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Number Display Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(AssistiveBackground, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (inputNumber.isEmpty()) "Nummer eingeben..." else inputNumber,
                        fontSize = if (inputNumber.length > 12) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (inputNumber.isEmpty()) AssistiveTextMuted else AssistiveTextPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keypad_display_text"),
                        maxLines = 1
                    )

                    if (inputNumber.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (inputNumber.isNotEmpty()) {
                                    inputNumber = inputNumber.dropLast(1)
                                }
                            },
                            modifier = Modifier.testTag("keypad_backspace_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Löschen",
                                tint = AssistiveRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Grid of 3x4 dial buttons
                val rows = listOf(
                    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                    listOf("*" to "", "0" to "+", "#" to "")
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for ((digit, sub) in row) {
                            DialpadKey(
                                digit = digit,
                                subText = sub,
                                onClick = {
                                    if (inputNumber.length < 20) {
                                        inputNumber += digit
                                    }
                                },
                                onLongClick = {
                                    if (digit == "0" && inputNumber.length < 20) {
                                        inputNumber += "+"
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Green Call Button
                Button(
                    onClick = {
                        val clean = inputNumber.trim()
                        if (clean.isNotEmpty()) {
                            onCallNumber(clean)
                        }
                    },
                    enabled = inputNumber.trim().isNotEmpty(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AssistiveGreen,
                        disabledContainerColor = Color(0xFFE2E8F0),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("keypad_call_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Anrufen",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialpadKey(
    digit: String,
    subText: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9))
            .clickable(onClick = onClick)
            .testTag("keypad_key_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )
            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AssistiveTextMuted
                )
            }
        }
    }
}
