package com.example.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable?
)

object AppUtils {

    /**
     * Queries all user-facing launchable apps installed on the device.
     */
    fun getInstalledLauncherApps(context: Context): List<InstalledAppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        val currentPackageName = context.packageName

        return resolveInfoList
            .mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == currentPackageName) return@mapNotNull null // Don't list this launcher itself

                val appLabel = resolveInfo.loadLabel(packageManager).toString()
                val appIcon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }
                InstalledAppInfo(
                    label = appLabel,
                    packageName = pkgName,
                    icon = appIcon
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    /**
     * Launches the installed app by package name.
     */
    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens the device Clock / Alarm app.
     */
    fun openClockApp(context: Context): Boolean {
        // 1. Try standard Alarm actions
        val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            if (alarmIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(alarmIntent)
                return true
            }
        } catch (ignored: Exception) {}

        val setAlarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            if (setAlarmIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(setAlarmIntent)
                return true
            }
        } catch (ignored: Exception) {}

        // 2. Common OEM Clock package names
        val clockPackages = listOf(
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.asus.deskclock",
            "com.sonyericsson.organizer",
            "com.coloros.alarm",
            "com.oppo.alarm"
        )
        for (pkg in clockPackages) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return true
                }
            } catch (ignored: Exception) {}
        }
        return false
    }

    /**
     * Opens the device Calendar app.
     */
    fun openCalendarApp(context: Context): Boolean {
        // 1. Try standard Calendar Contract View intent
        try {
            val builder = CalendarContract.CONTENT_URI.buildUpon()
            builder.appendPath("time")
            ContentUris.appendId(builder, System.currentTimeMillis())
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = builder.build()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
        } catch (ignored: Exception) {}

        // 2. Common OEM Calendar packages
        val calendarPackages = listOf(
            "com.google.android.calendar",
            "com.android.calendar",
            "com.samsung.android.calendar",
            "com.bbk.calendar",
            "com.coloros.calendar"
        )
        for (pkg in calendarPackages) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return true
                }
            } catch (ignored: Exception) {}
        }
        return false
    }

    /**
     * Converts an Android Drawable into a Bitmap.
     */
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

@Composable
fun InstalledAppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    fallbackTint: Color = Color.White
) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            AppUtils.drawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            tint = fallbackTint,
            modifier = modifier
        )
    }
}
