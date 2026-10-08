package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.util.DeviceStatusManager

/**
 * Custom launcher status bar that displays real hardware device status
 * (Live Battery %, Charging state, Real WiFi / Cellular Connectivity, Time).
 *
 * It draws over the top edge of the screen and absorbs pull-down gestures
 * to prevent the child from pulling down the system notification shade or quick settings.
 */
@Composable
fun CustomLauncherStatusBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent
) {
    val batteryLevel by DeviceStatusManager.batteryLevel.collectAsState()
    val isCharging by DeviceStatusManager.isCharging.collectAsState()
    val isWifiConnected by DeviceStatusManager.isWifiConnected.collectAsState()
    val isCellularConnected by DeviceStatusManager.isCellularConnected.collectAsState()
    val isAirplaneMode by DeviceStatusManager.isAirplaneMode.collectAsState()
    val currentTime by DeviceStatusManager.currentTime.collectAsState()

    val batteryColor by animateColorAsState(
        targetValue = when {
            isCharging -> Color(0xFFF59E0B) // Amber charging
            batteryLevel <= 20 -> AssistiveRed
            else -> AssistiveGreen
        },
        label = "battery_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(backgroundColor)
            .pointerInput(Unit) {
                // Intercept downward drag gestures so child cannot drag down notification bar
                detectVerticalDragGestures { _, _ -> }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val isDark = LocalIsDarkTheme.current
        val mutedIconColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
        val batteryBorderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Connectivity & Battery Icons (Time removed as large clock already exists in launcher)
            // Kept compact on the right side so center punch-hole cameras don't overlap cellular/wifi icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Airplane Mode
                if (isAirplaneMode) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = "Airplane Mode",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Cellular Signal
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = if (isCellularConnected) "Cellular Connected" else "No Cellular",
                    tint = if (isCellularConnected) AssistiveTextPrimary else mutedIconColor,
                    modifier = Modifier.size(22.dp).testTag("status_bar_cellular")
                )

                // WiFi
                Icon(
                    imageVector = if (isWifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = if (isWifiConnected) "WiFi Connected" else "WiFi Disconnected",
                    tint = if (isWifiConnected) AssistiveBlue else mutedIconColor,
                    modifier = Modifier.size(22.dp).testTag("status_bar_wifi")
                )

                Spacer(modifier = Modifier.width(3.dp))

                // Battery % Text
                Text(
                    text = "$batteryLevel %",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary,
                    modifier = Modifier.testTag("status_bar_battery_text")
                )

                // Battery Icon Graphic
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 3.dp)
                ) {
                    // Battery Body Capsule
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(16.dp)
                            .border(1.8.dp, batteryBorderColor, RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    ) {
                        // Fill level indicator
                        val fillFraction = (batteryLevel / 100f).coerceIn(0.08f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fillFraction)
                                .clip(RoundedCornerShape(2.dp))
                                .background(batteryColor)
                        )
                    }

                    // Battery positive terminal tip
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(topEnd = 1.5.dp, bottomEnd = 1.5.dp))
                            .background(batteryBorderColor)
                    )

                    // Charging Lightning Bolt
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(start = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
