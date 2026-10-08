package com.example.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.model.ButtonType
import com.example.model.LauncherButton

/**
 * Returns the localized button title for default/system buttons (Camera, Photos, Mom, Dad),
 * or the custom title entered by the user.
 */
@Composable
fun getLocalizedButtonTitle(button: LauncherButton): String {
    return when {
        button.type == ButtonType.CAMERA -> stringResource(R.string.btn_camera)
        button.type == ButtonType.PHOTOS -> stringResource(R.string.btn_photos)
        button.type == ButtonType.BROWSER || button.id == "browser_default" -> stringResource(R.string.btn_browser)
        button.type == ButtonType.SMS || button.id == "sms_default" -> "Nachrichten"
        button.id == "call_mom_default" || button.title.equals("Mom", ignoreCase = true) || button.title.equals("Mama", ignoreCase = true) -> stringResource(R.string.btn_call_mom)
        button.id == "call_dad_default" || button.title.equals("Dad", ignoreCase = true) || button.title.equals("Papa", ignoreCase = true) -> stringResource(R.string.btn_call_dad)
        else -> button.title
    }
}

fun getLocalizedButtonTitle(context: Context, button: LauncherButton): String {
    return when {
        button.type == ButtonType.CAMERA -> context.getString(R.string.btn_camera)
        button.type == ButtonType.PHOTOS -> context.getString(R.string.btn_photos)
        button.type == ButtonType.BROWSER || button.id == "browser_default" -> context.getString(R.string.btn_browser)
        button.type == ButtonType.SMS || button.id == "sms_default" -> "Nachrichten"
        button.id == "call_mom_default" || button.title.equals("Mom", ignoreCase = true) || button.title.equals("Mama", ignoreCase = true) -> context.getString(R.string.btn_call_mom)
        button.id == "call_dad_default" || button.title.equals("Dad", ignoreCase = true) || button.title.equals("Papa", ignoreCase = true) -> context.getString(R.string.btn_call_dad)
        else -> button.title
    }
}
