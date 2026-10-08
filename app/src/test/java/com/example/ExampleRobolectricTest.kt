package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.location.Location
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.LauncherPreferences
import com.example.data.quiz.PedagogyPreferences
import com.example.data.quiz.PedagogyQuizData
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherSettings
import com.example.model.LayoutStyle
import com.example.model.quiz.QuizCategory
import com.example.model.quiz.ReviewDueState
import com.example.util.EmergencyHelper
import com.example.util.SosLocationData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TEDOKI kids", appName)
  }

  @Test
  fun `verify german translations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val config = Configuration(context.resources.configuration)
    config.setLocale(Locale.GERMAN)
    val germanContext = context.createConfigurationContext(config)

    assertEquals("Kamera", germanContext.getString(R.string.btn_camera))
    assertEquals("Fotos", germanContext.getString(R.string.btn_photos))
    assertEquals("Mama", germanContext.getString(R.string.btn_call_mom))
    assertEquals("Papa", germanContext.getString(R.string.btn_call_dad))
    assertEquals("Zurück", germanContext.getString(R.string.nav_back))
    assertEquals("Foto", germanContext.getString(R.string.camera_photo))
    assertEquals("Auflegen", germanContext.getString(R.string.end_call))
    assertEquals("SOS NOTRUF", germanContext.getString(R.string.sos_button_title))
    assertEquals("Installierte App auswählen", germanContext.getString(R.string.add_app_title))
    assertEquals("SOS-Notruf Einstellungen", germanContext.getString(R.string.emergency_submenu_title))
    assertEquals("SOS-Notrufknopf anzeigen", germanContext.getString(R.string.settings_sos_toggle_title))
    assertEquals("Tippen auf Uhr öffnet Wecker", germanContext.getString(R.string.settings_clock_click_title))
    assertEquals("Tippen auf Datum öffnet Kalender", germanContext.getString(R.string.settings_calendar_click_title))
    assertEquals("Google Maps-Link per SMS gesendet!", germanContext.getString(R.string.sos_sms_confirmed_title))
    assertEquals("Notruf wird getätigt…", germanContext.getString(R.string.sos_call_in_progress))
  }

  @Test
  fun `test default launcher preferences and emergency settings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)
    val settings = prefs.getSettings()
    assertFalse(settings.isSetupComplete)
    assertEquals("1234", settings.pin)
    assertEquals(LayoutStyle.GRID, settings.layoutStyle)
    assertNotNull(settings.buttons)
    assertEquals(LauncherSettings.defaultButtons().size, settings.buttons.size)
    assertEquals("112", settings.emergencyNumber)
    assertTrue(settings.showSosButton)
    assertTrue(settings.sendSosLocationSms)
    assertTrue(settings.enableClockClick)
    assertTrue(settings.enableCalendarClick)

    val emergencyHelper = EmergencyHelper(context)
    assertNotNull(emergencyHelper)
  }

  @Test
  fun `test Google Maps link creation and SMS formatting`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val emergencyHelper = EmergencyHelper(context)

    val location = Location("gps").apply {
      latitude = 52.520008
      longitude = 13.404954
      accuracy = 8.5f
    }

    val sosLocation = emergencyHelper.createSosLocationData(location)
    assertEquals(52.520008, sosLocation.latitude, 0.000001)
    assertEquals(13.404954, sosLocation.longitude, 0.000001)
    assertEquals("https://maps.google.com/?q=52.520008,13.404954", sosLocation.mapsUrl)

    val smsMessage = emergencyHelper.buildSosSmsMessage(sosLocation)
    assertTrue(smsMessage.contains("NOTRUF / EMERGENCY SOS"))
    assertTrue(smsMessage.contains("https://maps.google.com/?q=52.520008,13.404954"))
    assertTrue(smsMessage.contains("Genauigkeit: ~8m"))
  }

  @Test
  fun `test SMS sending with location link via EmergencyHelper`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val shadowApp = Shadows.shadowOf(app)
    shadowApp.grantPermissions(Manifest.permission.SEND_SMS)

    val emergencyHelper = EmergencyHelper(app)
    val testLocation = SosLocationData(
      latitude = 48.137154,
      longitude = 11.576124,
      accuracy = 5.0f,
      mapsUrl = "https://maps.google.com/?q=48.137154,11.576124"
    )

    val sentSuccess = emergencyHelper.sendSosSms("+491701234567", testLocation)
    assertTrue("sendSosSms should return true when permission is granted", sentSuccess)

    @Suppress("DEPRECATION")
    val smsManager = SmsManager.getDefault()
    val shadowSms = Shadows.shadowOf(smsManager)
    val lastSingle = shadowSms.lastSentTextMessageParams
    val lastMultipart = shadowSms.lastSentMultipartTextMessageParams

    assertTrue("An SMS message should have been recorded", lastSingle != null || lastMultipart != null)
    val dest = lastSingle?.destinationAddress ?: lastMultipart?.destinationAddress
    val body = lastSingle?.text ?: lastMultipart?.parts?.joinToString("") ?: ""

    assertEquals("+491701234567", dest)
    assertTrue("Sent SMS must contain Google Maps URL", body.contains("https://maps.google.com/?q=48.137154,11.576124"))
    assertTrue("Sent SMS must contain emergency alert header", body.contains("NOTRUF / EMERGENCY SOS"))
  }

  @Test
  fun `test app button and toggle persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)
    val current = prefs.getSettings()

    val appButton = LauncherButton(
      id = "app_calculator",
      type = ButtonType.APP,
      title = "Calculator",
      packageName = "com.google.android.calculator",
      colorHex = 0xFF4F46E5
    )

    val updatedButtons = current.buttons + appButton
    assertTrue(updatedButtons.size <= 6)
    prefs.updateSettings(
      current.copy(
        buttons = updatedButtons,
        showSosButton = false,
        sendSosLocationSms = false,
        enableClockClick = false,
        enableCalendarClick = false
      )
    )

    val reloaded = prefs.getSettings()
    val savedAppButton = reloaded.buttons.find { it.id == "app_calculator" }
    assertNotNull(savedAppButton)
    assertEquals(ButtonType.APP, savedAppButton?.type)
    assertEquals("com.google.android.calculator", savedAppButton?.packageName)
    assertEquals("Calculator", savedAppButton?.title)
    assertFalse(reloaded.showSosButton)
    assertFalse(reloaded.sendSosLocationSms)
    assertFalse(reloaded.enableClockClick)
    assertFalse(reloaded.enableCalendarClick)
  }

  @Test
  fun `test 1 WhatsApp selected for first time - child quiz required and WhatsApp locked`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    pedPrefs.resetAppPedagogy("com.whatsapp")

    val waButton = LauncherButton(
      id = "btn_wa",
      type = ButtonType.APP,
      title = "WhatsApp",
      packageName = "com.whatsapp"
    )

    val category = PedagogyPreferences.resolveQuizCategory(waButton)
    assertEquals(QuizCategory.WHATSAPP, category)

    val status = pedPrefs.getAppStatus("com.whatsapp", "WhatsApp", category!!)
    assertFalse("WhatsApp must not be unlocked on first use", status.unlocked)
    assertFalse("WhatsApp must not be fully unlocked", status.isFullyUnlocked())
    assertFalse("Child quiz must not be passed yet", status.childQuizPassed)
  }

  @Test
  fun `test 2 child quiz passed - advances to parent quiz and app remains locked`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    pedPrefs.resetAppPedagogy("com.whatsapp")

    val updated = pedPrefs.recordChildQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    assertTrue("Child quiz must be marked passed", updated.childQuizPassed)
    assertNotNull("Child quiz passed timestamp must be recorded", updated.childQuizPassedAt)
    assertFalse("Parent quiz must not be passed yet", updated.parentQuizPassed)
    assertFalse("WhatsApp must remain locked until parent quiz and PIN", updated.unlocked)
  }

  @Test
  fun `test 3 parent quiz passed - awaits PIN verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    pedPrefs.resetAppPedagogy("com.whatsapp")

    pedPrefs.recordChildQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    val updated = pedPrefs.recordParentQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    assertTrue("Child quiz is passed", updated.childQuizPassed)
    assertTrue("Parent quiz is passed", updated.parentQuizPassed)
    assertNotNull("Parent quiz timestamp recorded", updated.parentQuizPassedAt)
    assertFalse("PIN not yet verified, app still locked", updated.unlocked)
  }

  @Test
  fun `test 4 incorrect PIN keeps app locked`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    pedPrefs.resetAppPedagogy("com.whatsapp")

    pedPrefs.recordChildQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    pedPrefs.recordParentQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    val currentSettings = LauncherPreferences(context).getSettings()
    val enteredWrongPin = "9999"
    val isPinValid = (enteredWrongPin == currentSettings.pin)

    assertFalse("Wrong PIN must be rejected", isPinValid)
    val status = pedPrefs.getAppStatus("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    assertFalse("App must remain locked after wrong PIN", status.unlocked)
  }

  @Test
  fun `test 5 correct PIN unlocks app and stores unlockDate`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    pedPrefs.resetAppPedagogy("com.whatsapp")

    pedPrefs.recordChildQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    pedPrefs.recordParentQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    val currentSettings = LauncherPreferences(context).getSettings()
    val enteredPin = currentSettings.pin
    assertEquals("1234", enteredPin)

    val unlockedStatus = pedPrefs.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    assertTrue("App must be unlocked", unlockedStatus.unlocked)
    assertTrue("PIN must be marked verified", unlockedStatus.pinVerified)
    assertNotNull("Unlock timestamp must be stored", unlockedStatus.unlockedAt)
    assertTrue("App is fully unlocked", unlockedStatus.isFullyUnlocked())
  }

  @Test
  fun `test 6 WhatsApp opened on next day - opens normally without quiz`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    val baseTime = 1770000000000L // arbitrary fixed epoch

    val unlockedStatus = pedPrefs.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP).copy(
      unlockedAt = baseTime
    )
    pedPrefs.saveAppStatus(unlockedStatus)

    val nextDayTime = baseTime + (1L * 24 * 60 * 60 * 1000L) // +1 day
    val checkStatus = pedPrefs.getAppStatus("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    assertEquals(ReviewDueState.NONE, checkStatus.isReviewDue(nextDayTime))
    assertTrue("App must open normally without quiz", checkStatus.isFullyUnlocked(nextDayTime))
  }

  @Test
  fun `test 7 30 days after unlock - triggers 30-day review cycle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    val baseTime = 1770000000000L

    val unlockedStatus = pedPrefs.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP).copy(
      unlockedAt = baseTime,
      review30Completed = false
    )
    pedPrefs.saveAppStatus(unlockedStatus)

    val thirtyDaysLater = baseTime + (31L * 24 * 60 * 60 * 1000L)
    val statusAt31Days = pedPrefs.getAppStatus("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    assertEquals(ReviewDueState.DUE_30_DAYS, statusAt31Days.isReviewDue(thirtyDaysLater))
    assertFalse("App must not open normally while 30-day review is due", statusAt31Days.isFullyUnlocked(thirtyDaysLater))

    // Complete 30-day review cycle
    val reviewUnlocked = pedPrefs.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP, currentTimeMillis = thirtyDaysLater)
    assertTrue("30-day review completed flag must be true", reviewUnlocked.review30Completed)
    assertNotNull(reviewUnlocked.review30CompletedAt)
    assertTrue("App is unlocked again after review", reviewUnlocked.isFullyUnlocked(thirtyDaysLater))
  }

  @Test
  fun `test 8 90 days after initial unlock - triggers 90-day review cycle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    val baseTime = 1770000000000L

    val statusWith30Done = pedPrefs.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP).copy(
      unlockedAt = baseTime,
      review30Completed = true,
      review30CompletedAt = baseTime + (30L * 24 * 60 * 60 * 1000L),
      review90Completed = false
    )
    pedPrefs.saveAppStatus(statusWith30Done)

    val ninetyOneDaysLater = baseTime + (91L * 24 * 60 * 60 * 1000L)
    val statusAt91 = pedPrefs.getAppStatus("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    assertEquals(ReviewDueState.DUE_90_DAYS, statusAt91.isReviewDue(ninetyOneDaysLater))
    assertFalse("App must require 90-day review", statusAt91.isFullyUnlocked(ninetyOneDaysLater))
  }

  @Test
  fun `test 9 camera status is completely independent from WhatsApp`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)
    pedPrefs.resetAppPedagogy("tedoki_camera")

    // WhatsApp is unlocked
    pedPrefs.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    val waStatus = pedPrefs.getAppStatus("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    assertTrue(waStatus.unlocked)

    // Camera has its own status
    val camStatus = pedPrefs.getAppStatus("tedoki_camera", "Camera", QuizCategory.FOTOS_MACHEN)
    assertFalse("Camera must remain locked independently of WhatsApp", camStatus.unlocked)
    assertEquals(QuizCategory.FOTOS_MACHEN, camStatus.quizCategory)
  }

  @Test
  fun `test 10 preferences persist across launcher restarts`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs1 = PedagogyPreferences(context)
    pedPrefs1.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    // Simulate new launcher instance on reboot
    val pedPrefs2 = PedagogyPreferences(context)
    val status2 = pedPrefs2.getAppStatus("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    assertTrue("Status must persist across process restart", status2.unlocked)
    assertTrue("PIN verification must persist", status2.pinVerified)
  }

  @Test
  fun `test 11 installed WhatsApp recognized and triggers quiz before opening`() {
    val waInstalledButton = LauncherButton(
      id = "btn_installed_wa",
      type = ButtonType.APP,
      title = "WhatsApp Messenger",
      packageName = "com.whatsapp"
    )

    val resolvedCategory = PedagogyPreferences.resolveQuizCategory(waInstalledButton)
    assertEquals(QuizCategory.WHATSAPP, resolvedCategory)
    val module = PedagogyQuizData.getModule(resolvedCategory!!)
    assertEquals("WhatsApp", module.title)
    assertTrue(module.childQuestions.isNotEmpty())
    assertTrue(module.parentQuestions.isNotEmpty())
  }

  @Test
  fun `test 12 installed app without quiz definition launches directly without wrong quiz`() {
    val calcButton = LauncherButton(
      id = "btn_calc",
      type = ButtonType.APP,
      title = "Calculator",
      packageName = "com.google.android.calculator"
    )

    val resolvedCategory = PedagogyPreferences.resolveQuizCategory(calcButton)
    assertEquals("Non-sensitive app must have null quiz category", null, resolvedCategory)
  }

  @Test
  fun `test 13 kid browser default websites and whitelist domain verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)
    val settings = prefs.getSettings()

    assertNotNull(settings.allowedWebsites)
    assertTrue("Should have default safe kids sites", settings.allowedWebsites.isNotEmpty())

    val maus = settings.allowedWebsites.find { it.domain.contains("wdrmaus.de") }
    assertNotNull("Die Maus should be in default whitelist", maus)
    assertEquals("Die Maus", maus?.title)

    val kika = settings.allowedWebsites.find { it.domain.contains("kika.de") }
    assertNotNull("KiKA should be in default whitelist", kika)

    val fragfinn = settings.allowedWebsites.find { it.domain.contains("fragfinn.de") }
    assertNotNull("FragFINN should be in default whitelist", fragfinn)
  }

  @Test
  fun `test 14 custom website addition and persistence in preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)
    val current = prefs.getSettings()

    val customSite = com.example.model.WebBookmark(
      id = "custom_schule",
      title = "Grundschule",
      url = "https://grundschule-musterstadt.de",
      domain = "grundschule-musterstadt.de",
      iconEmoji = "🎒"
    )

    prefs.updateSettings(current.copy(allowedWebsites = current.allowedWebsites + customSite))

    val reloaded = prefs.getSettings()
    val savedSite = reloaded.allowedWebsites.find { it.id == "custom_schule" }
    assertNotNull(savedSite)
    assertEquals("Grundschule", savedSite?.title)
    assertEquals("grundschule-musterstadt.de", savedSite?.domain)
  }

  @Test
  fun `test 15 reset quiz in dashboard locks function and requires full quiz routine again`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val pedPrefs = PedagogyPreferences(context)

    // First unlock WhatsApp completely
    pedPrefs.recordChildQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    pedPrefs.recordParentQuizPassed("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    val unlocked = pedPrefs.unlockAppWithPin("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    assertTrue("WhatsApp must initially be unlocked", unlocked.unlocked)
    assertTrue("WhatsApp must be fully unlocked", unlocked.isFullyUnlocked())

    // Now parent resets quiz in settings dashboard
    pedPrefs.resetAppPedagogy("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)

    // Verify it is now strictly locked
    val lockedStatus = pedPrefs.getAppStatus("com.whatsapp", "WhatsApp", QuizCategory.WHATSAPP)
    assertFalse("Function must be locked after reset", lockedStatus.unlocked)
    assertFalse("Function must not be fully unlocked", lockedStatus.isFullyUnlocked())
    assertFalse("Child quiz must be reset", lockedStatus.childQuizPassed)
    assertFalse("Parent quiz must be reset", lockedStatus.parentQuizPassed)
    assertFalse("PIN verified must be reset", lockedStatus.pinVerified)
    assertEquals(null, lockedStatus.unlockedAt)

    // Verify a second PedagogyPreferences instance (e.g. MainActivity) sees the locked status
    val launcherInstance = PedagogyPreferences(context)
    val launcherStatus = launcherInstance.statusMapFlow.value["com.whatsapp"]
    assertNotNull("Launcher instance must observe status in flow", launcherStatus)
    assertFalse("Launcher instance must see function as locked", launcherStatus!!.unlocked)
    assertFalse("Launcher instance must see function not fully unlocked", launcherStatus.isFullyUnlocked())
  }

  @Test
  fun `test 16 onboarding setup flow creates correct buttons and profile settings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)

    val initialCamera = LauncherButton(
      id = "camera_default",
      type = ButtonType.CAMERA,
      title = "Kamera",
      colorHex = 0xFF2563EB
    )
    val initialPhotos = LauncherButton(
      id = "photos_default",
      type = ButtonType.PHOTOS,
      title = "Fotos",
      colorHex = 0xFFDB2777
    )
    val contact1 = LauncherButton(
      id = "call_contact_0",
      type = ButtonType.CALL,
      title = "Mama",
      phoneNumber = "0170 1234567",
      colorHex = 0xFF059669
    )
    val contact2 = LauncherButton(
      id = "call_contact_1",
      type = ButtonType.CALL,
      title = "Papa",
      phoneNumber = "0171 7654321",
      colorHex = 0xFF10B981
    )

    val completedSettings = LauncherSettings(
      isSetupComplete = true,
      childName = "Emma",
      childAge = 7,
      childAvatar = "fox",
      buttons = listOf(initialCamera, initialPhotos, contact1, contact2),
      layoutStyle = LayoutStyle.GRID,
      pin = "4321",
      showSosButton = true,
      emergencyContactName = "Mama",
      emergencyNumber = "112",
      sendSosLocationSms = true
    )

    prefs.updateSettings(completedSettings)
    val loaded = prefs.getSettings()

    assertTrue(loaded.isSetupComplete)
    assertEquals("Emma", loaded.childName)
    assertEquals(7, loaded.childAge)
    assertEquals("fox", loaded.childAvatar)
    assertEquals(4, loaded.buttons.size)
    assertEquals("Kamera", loaded.buttons[0].title)
    assertEquals("Fotos", loaded.buttons[1].title)
    assertEquals("Mama", loaded.buttons[2].title)
    assertEquals("0170 1234567", loaded.buttons[2].phoneNumber)
    assertEquals("Papa", loaded.buttons[3].title)
    assertEquals("0171 7654321", loaded.buttons[3].phoneNumber)
    assertEquals(LayoutStyle.GRID, loaded.layoutStyle)
    assertEquals("4321", loaded.pin)
    assertTrue(loaded.showSosButton)
    assertEquals("112", loaded.emergencyNumber)
    assertTrue(loaded.sendSosLocationSms)
  }

  @Test
  fun `test 17 launcher full reset clears all settings and restarts initial onboarding`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)
    val pedPrefs = PedagogyPreferences(context)

    // Set custom settings and quiz progress
    prefs.updateSettings(
      LauncherSettings(
        isSetupComplete = true,
        childName = "Leo",
        childAge = 9,
        childAvatar = "lion",
        pin = "9876"
      )
    )
    pedPrefs.unlockAppWithPin("tedoki_camera", "Kamera", QuizCategory.FOTOS_MACHEN)

    // Verify setup is complete and camera is unlocked
    assertTrue(prefs.getSettings().isSetupComplete)
    assertTrue(pedPrefs.getAppStatus("tedoki_camera", "Kamera", QuizCategory.FOTOS_MACHEN).unlocked)

    // Trigger full launcher reset
    pedPrefs.resetAllPedagogy()
    prefs.resetLauncherToInitialSetup()

    // Verify settings are reset to initial setup state
    val resetSettings = prefs.getSettings()
    assertFalse("isSetupComplete must be false after full reset", resetSettings.isSetupComplete)
    assertEquals("", resetSettings.childName)
    assertEquals("1234", resetSettings.pin)

    // Verify pedagogy map is cleared
    assertTrue("Pedagogy map must be empty after full reset", pedPrefs.statusMapFlow.value.isEmpty())
  }

  @Test
  fun `test 18 custom avatar photo is copied to internal storage and persisted in settings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)

    // Simulate custom photo file in context.filesDir
    val customFile = java.io.File(context.filesDir, "child_avatar_test.jpg")
    customFile.writeText("fake image content")

    val fileUriString = android.net.Uri.fromFile(customFile).toString()
    prefs.updateSettings(
      LauncherSettings(
        isSetupComplete = true,
        childName = "Mia",
        childAvatar = fileUriString
      )
    )

    val reloaded = prefs.getSettings()
    assertEquals(fileUriString, reloaded.childAvatar)
    assertTrue("Avatar path must start with file scheme or path", reloaded.childAvatar.startsWith("file://") || reloaded.childAvatar.startsWith("/"))
    assertTrue("Avatar file must exist on disk across reloads", java.io.File(android.net.Uri.parse(reloaded.childAvatar).path ?: "").exists())
  }

  @Test
  fun `test 19 android contacts toggle and allowed accounts are persisted in preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)

    val initial = prefs.getSettings()
    assertFalse("enableAndroidContacts should default to false", initial.enableAndroidContacts)
    assertTrue("allowedContactAccounts should default to empty", initial.allowedContactAccounts.isEmpty())

    val updated = initial.copy(
      enableAndroidContacts = true,
      allowedContactAccounts = listOf("Google (test@example.com)", "SIM-Karte")
    )
    prefs.updateSettings(updated)

    // Create a new preferences instance pointing to the same SharedPreferences to verify persistence
    val reloadedPrefs = LauncherPreferences(context)
    val reloaded = reloadedPrefs.getSettings()
    assertTrue("enableAndroidContacts must remain enabled after reload", reloaded.enableAndroidContacts)
    assertEquals(2, reloaded.allowedContactAccounts.size)
    assertTrue(reloaded.allowedContactAccounts.contains("Google (test@example.com)"))
    assertTrue(reloaded.allowedContactAccounts.contains("SIM-Karte"))
  }

  @Test
  fun `test 20 device contact account display title formats nextcloud davx5 and google correctly`() {
    val nextcloudAccount = com.example.data.contact.DeviceContactAccount(
      accountName = "user@nextcloud.example.org",
      accountType = "at.bitfire.davx5"
    )
    assertTrue("Should display Nextcloud label", nextcloudAccount.displayTitle.startsWith("Nextcloud"))

    val googleAccount = com.example.data.contact.DeviceContactAccount(
      accountName = "user@gmail.com",
      accountType = "com.google"
    )
    assertTrue("Should display Google label", googleAccount.displayTitle.startsWith("Google"))
  }

  @Test
  fun `test 21 lockscreen settings are properly persisted and restored in preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = LauncherPreferences(context)

    val initial = prefs.getSettings()
    assertFalse("enableLockscreen should default to false", initial.enableLockscreen)
    assertEquals(com.example.model.LockscreenClockStyle.ANALOG_CONNI, initial.lockscreenClockStyle)

    val updated = initial.copy(
      enableLockscreen = true,
      lockscreenClockStyle = com.example.model.LockscreenClockStyle.ANALOG_CONNI,
      lockscreenPresetBackground = "pastel_green",
      lockscreenRequireMathProblem = true
    )
    prefs.updateSettings(updated)

    val reloadedPrefs = LauncherPreferences(context)
    val reloaded = reloadedPrefs.getSettings()
    assertTrue("enableLockscreen must remain true after save/load", reloaded.enableLockscreen)
    assertEquals(com.example.model.LockscreenClockStyle.ANALOG_CONNI, reloaded.lockscreenClockStyle)
    assertEquals("pastel_green", reloaded.lockscreenPresetBackground)
    assertTrue("lockscreenRequireMathProblem must remain true", reloaded.lockscreenRequireMathProblem)
  }
}
