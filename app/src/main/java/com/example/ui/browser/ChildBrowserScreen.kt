package com.example.ui.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.model.LauncherSettings
import com.example.model.WebBookmark
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme

/**
 * Child-friendly, strictly whitelisted web browser ("Surfen") for tedoki Kids Launcher.
 * Features:
 * - Start page with large bookmarks tiles (Die Maus, KiKA, FragFINN, etc.)
 * - Combined round forward/back pill button
 * - Round Home button (Häuschen)
 * - Round Reload button (zwei Pfeile im Kreis)
 * - Round Close button (roter Kreis mit weißem X)
 * - Instant blocking of all non-whitelisted external URLs
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChildBrowserScreen(
    currentSettings: LauncherSettings,
    onClose: () -> Unit,
    onFullScreenVideoChange: (Boolean) -> Unit = {}
) {
    val allowedWebsites = remember(currentSettings.allowedWebsites) {
        currentSettings.allowedWebsites.filter { it.isEnabled }
    }

    var isShowingBookmarks by rememberSaveable { mutableStateOf(true) }
    var currentUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var currentTitle by rememberSaveable { mutableStateOf("") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isBlocked by remember { mutableStateOf(false) }
    var blockedHost by remember { mutableStateOf("") }
    val isDark = LocalIsDarkTheme.current
    var lastLoadedUrl by remember { mutableStateOf<String?>(null) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var customVideoView by remember { mutableStateOf<android.view.View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    val context = LocalContext.current

    fun isDomainAllowed(host: String): Boolean {
        val cleanHost = host.lowercase().removePrefix("www.")

        // Allow media streaming CDNs and portal subdomains for Die Maus and KiKa so video streams don't get blocked
        val mediaAllowed = cleanHost.endsWith(".wdrmaus.de") || cleanHost == "wdrmaus.de" ||
                cleanHost.endsWith(".die-maus.de") || cleanHost == "die-maus.de" ||
                cleanHost.endsWith(".wdr.de") || cleanHost == "wdr.de" ||
                cleanHost.endsWith(".ard.de") || cleanHost == "ard.de" ||
                cleanHost.endsWith(".ardmediathek.de") || cleanHost == "ardmediathek.de" ||
                cleanHost.endsWith(".zdf.de") || cleanHost == "zdf.de" ||
                cleanHost.endsWith(".kika.de") || cleanHost == "kika.de" ||
                cleanHost.endsWith(".fragfinn.de") || cleanHost == "fragfinn.de" ||
                cleanHost.endsWith(".blinde-kuh.de") || cleanHost == "blinde-kuh.de" ||
                cleanHost.contains("akamai") ||
                cleanHost.contains("cloudfront.net")

        if (mediaAllowed) return true

        return allowedWebsites.any { bookmark ->
            val cleanAllowed = bookmark.domain.lowercase().removePrefix("www.")
            cleanHost == cleanAllowed ||
                    cleanHost.endsWith(".$cleanAllowed") ||
                    cleanAllowed.endsWith(".$cleanHost")
        }
    }

    val persistentWebView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // HARDWARE acceleration enabled so GPU renders HTML5 video surfaces
            // and scrolling is buttery smooth without CPU lag
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = true
            isHorizontalScrollBarEnabled = false
            isScrollbarFadingEnabled = true
            overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS

            // Cookie and third-party cookie support for video player sessions
            try {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)
            } catch (ignored: Exception) {}

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                mediaPlaybackRequiresUserGesture = false // Fix black screen video playback
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                setSupportMultipleWindows(false) // Never open external popup windows
                allowFileAccess = false
                allowContentAccess = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                cacheMode = WebSettings.LOAD_DEFAULT
                @Suppress("DEPRECATION")
                setRenderPriority(WebSettings.RenderPriority.HIGH)
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val uri = request?.url ?: return true
                    val scheme = uri.scheme?.lowercase() ?: ""
                    if (scheme != "http" && scheme != "https") {
                        return true // Block intent://, tel://, market://
                    }

                    val host = uri.host?.lowercase() ?: ""
                    if (isDomainAllowed(host)) {
                        isBlocked = false
                        return false
                    } else {
                        isBlocked = true
                        blockedHost = host
                        return true
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    isLoading = true
                    canGoBack = view?.canGoBack() == true
                    canGoForward = view?.canGoForward() == true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isLoading = false
                    currentUrl = url
                    currentTitle = view?.title ?: ""
                    canGoBack = view?.canGoBack() == true
                    canGoForward = view?.canGoForward() == true
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        isLoading = false
                    }
                }

                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    try {
                        view?.destroy()
                    } catch (ignored: Exception) {}
                    webViewInstance = null
                    isShowingBookmarks = true
                    isBlocked = false
                    isLoading = false
                    return true
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progress = (newProgress / 100f).coerceIn(0f, 1f)
                    isLoading = newProgress < 100
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) {
                        currentTitle = title
                    }
                }

                override fun getDefaultVideoPoster(): Bitmap? {
                    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                }

                override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                    if (customVideoView != null) {
                        callback?.onCustomViewHidden()
                        return
                    }
                    view?.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    customVideoView = view
                    customViewCallback = callback
                }

                override fun onHideCustomView() {
                    try {
                        customViewCallback?.onCustomViewHidden()
                    } catch (ignored: Exception) {}
                    customVideoView = null
                    customViewCallback = null
                }
            }
        }
    }

    LaunchedEffect(persistentWebView) {
        webViewInstance = persistentWebView
    }

    LaunchedEffect(customVideoView) {
        onFullScreenVideoChange(customVideoView != null)
    }

    val window = (context as? android.app.Activity)?.window
    DisposableEffect(customVideoView != null) {
        if (customVideoView != null && window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    /**
     * Completely purges all WebView cache, history, local storage, session storage and cookies
     * so that the browser always restarts with a clean slate on every launch.
     */
    fun clearBrowserSession(ctx: Context, webView: WebView?) {
        try {
            webView?.let { wv ->
                wv.stopLoading()
                wv.clearHistory()
                wv.clearFormData()
                wv.clearSslPreferences()
                wv.clearCache(true)
                wv.loadUrl("about:blank")
                wv.onPause()
                wv.pauseTimers()
                wv.destroy()
            }
        } catch (ignored: Exception) {}

        try {
            WebStorage.getInstance().deleteAllData()
        } catch (ignored: Exception) {}

        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.removeSessionCookies(null)
            cookieManager.flush()
        } catch (ignored: Exception) {}
    }

    val handleExitBrowser = {
        try {
            customViewCallback?.onCustomViewHidden()
        } catch (ignored: Exception) {}
        customVideoView = null
        customViewCallback = null
        onFullScreenVideoChange(false)
        clearBrowserSession(context, persistentWebView)
        webViewInstance = null
        onClose()
    }

    /**
     * Robust backend hard-reload: stops hanging scripts, clears RAM cache,
     * and forces an un-cached reload of the page to unstick frozen sites.
     */
    fun performHardReload() {
        val wv = webViewInstance ?: return
        val target = currentUrl ?: wv.url
        if (!target.isNullOrBlank()) {
            try {
                wv.stopLoading()
                wv.clearCache(false)
                isBlocked = false
                isLoading = true
                progress = 0.15f
                lastLoadedUrl = target
                val headers = mapOf(
                    "Pragma" to "no-cache",
                    "Cache-Control" to "no-cache, no-store, must-revalidate"
                )
                wv.loadUrl(target, headers)
            } catch (e: Exception) {
                try {
                    wv.reload()
                } catch (ignored: Exception) {}
            }
        } else {
            try {
                wv.reload()
            } catch (ignored: Exception) {}
        }
    }

    // Child-friendly Back handling
    BackHandler {
        if (customVideoView != null) {
            try {
                customViewCallback?.onCustomViewHidden()
            } catch (ignored: Exception) {}
            customVideoView = null
            customViewCallback = null
        } else if (isBlocked) {
            isBlocked = false
            if (currentUrl == null) {
                isShowingBookmarks = true
            } else if (webViewInstance?.canGoBack() == true) {
                webViewInstance?.goBack()
            } else {
                isShowingBookmarks = true
            }
        } else if (!isShowingBookmarks) {
            if (webViewInstance?.canGoBack() == true) {
                webViewInstance?.goBack()
            } else {
                isShowingBookmarks = true
            }
        } else {
            handleExitBrowser()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                customViewCallback?.onCustomViewHidden()
            } catch (ignored: Exception) {}
            onFullScreenVideoChange(false)
            clearBrowserSession(context, persistentWebView)
            webViewInstance = null
        }
    }

    fun openBookmark(bookmark: WebBookmark) {
        isBlocked = false
        currentUrl = bookmark.url
        currentTitle = bookmark.title
        isShowingBookmarks = false
        lastLoadedUrl = bookmark.url
        persistentWebView.stopLoading()
        persistentWebView.clearHistory()
        persistentWebView.loadUrl(bookmark.url)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AssistiveBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
        ) {

            // Header status / title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌐", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = if (isShowingBookmarks) stringResource(R.string.browser_bookmarks_title)
                            else if (currentTitle.isNotBlank()) currentTitle
                            else stringResource(R.string.browser_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Geschützter Kinder-Modus",
                                fontSize = 12.sp,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Loading Progress Bar
            AnimatedVisibility(
                visible = isLoading && !isShowingBookmarks && !isBlocked,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = AssistiveBlue,
                    trackColor = Color(0xFFE2E8F0)
                )
            }

            // Main Content Area (Bookmarks / WebView / Blocked Screen)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // 1. Bookmarks Start Page
                if (isShowingBookmarks) {
                    BrowserBookmarksView(
                        bookmarks = allowedWebsites,
                        onBookmarkClick = { openBookmark(it) }
                    )
                } else if (isBlocked) {
                    // 2. Child-friendly Blocked Screen Overlay
                    BrowserBlockedView(
                        blockedHost = blockedHost,
                        onBackHome = {
                            isBlocked = false
                            isShowingBookmarks = true
                        },
                        onGoBack = {
                            isBlocked = false
                            if (webViewInstance?.canGoBack() == true) {
                                webViewInstance?.goBack()
                            } else {
                                isShowingBookmarks = true
                            }
                        }
                    )
                } else {
                    // 3. Embedded Safe WebView with Hardware Acceleration for 60fps scrolling & Video Playback
                    AndroidView(
                        factory = {
                            (persistentWebView.parent as? ViewGroup)?.removeView(persistentWebView)
                            persistentWebView
                        },
                        update = { view ->
                            webViewInstance = view
                            view.requestLayout()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Bottom Navigation Control Bar
            // User requested:
            // 1. Vor & Zurück: "zwei Pfeile dafür in einem großen, runden, Button"
            // 2. Home: "Häuschen"
            // 3. Neu laden: "diese zwei Pfeile im Kreis"
            // 4. Beenden: "roter Kreis mit weißen x"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues()),
                color = AssistiveSurface,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, AssistiveBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Control 1: Combined Big Round Pill for Back & Forward ("zwei Pfeile dafür in einem großen, runden Button")
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.5.dp, AssistiveBorder),
                        modifier = Modifier
                            .height(58.dp)
                            .testTag("browser_nav_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            // Back Arrow
                            IconButton(
                                onClick = {
                                    if (isBlocked) {
                                        isBlocked = false
                                        webViewInstance?.goBack()
                                    } else if (!isShowingBookmarks && webViewInstance?.canGoBack() == true) {
                                        webViewInstance?.goBack()
                                    } else {
                                        isShowingBookmarks = true
                                    }
                                },
                                enabled = !isShowingBookmarks || isBlocked,
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("browser_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.browser_back),
                                    tint = if (!isShowingBookmarks || isBlocked) AssistiveBlue else Color(0xFF94A3B8),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Subtle Divider
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height(28.dp)
                                    .background(AssistiveBorder)
                            )

                            // Forward Arrow
                            IconButton(
                                onClick = {
                                    if (!isShowingBookmarks && webViewInstance?.canGoForward() == true) {
                                        webViewInstance?.goForward()
                                    }
                                },
                                enabled = canGoForward && !isShowingBookmarks && !isBlocked,
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("browser_forward_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = stringResource(R.string.browser_forward),
                                    tint = if (canGoForward && !isShowingBookmarks && !isBlocked) AssistiveBlue else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    // Control 2: Home Button ("Häuschen")
                    Surface(
                        shape = CircleShape,
                        color = if (isShowingBookmarks) AssistiveBlue else (if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                        border = BorderStroke(1.5.dp, if (isShowingBookmarks) AssistiveBlue else AssistiveBorder),
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .clickable {
                                isBlocked = false
                                isShowingBookmarks = true
                            }
                            .testTag("browser_home_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = stringResource(R.string.browser_home),
                                tint = if (isShowingBookmarks) Color.White else AssistiveBlue,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Control 3: Reload ("zwei Pfeile im Kreis")
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.5.dp, AssistiveBorder),
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !isShowingBookmarks) {
                                performHardReload()
                            }
                            .testTag("browser_reload_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.browser_reload),
                                tint = if (!isShowingBookmarks) AssistiveBlue else Color(0xFFCBD5E1),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Control 4: Close / Exit ("roter Kreis mit weißen x")
                    Surface(
                        shape = CircleShape,
                        color = AssistiveRed,
                        border = BorderStroke(1.5.dp, Color(0xFFDC2626)),
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .clickable { handleExitBrowser() }
                            .testTag("browser_close_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.browser_close),
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Custom Video Player Overlay
        if (customVideoView != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        android.widget.FrameLayout(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)
                            (customVideoView?.parent as? ViewGroup)?.removeView(customVideoView)
                            customVideoView?.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            customVideoView?.let { addView(it) }
                        }
                    },
                    update = { container ->
                        val cv = customVideoView
                        if (cv != null && cv.parent != container) {
                            (cv.parent as? ViewGroup)?.removeView(cv)
                            cv.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            container.addView(cv)
                        }
                        container.requestLayout()
                        cv?.requestLayout()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Child-friendly exit fullscreen button in upper corner
                IconButton(
                    onClick = {
                        try {
                            customViewCallback?.onCustomViewHidden()
                        } catch (ignored: Exception) {}
                        customVideoView = null
                        customViewCallback = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        .testTag("exit_fullscreen_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Vollbild beenden",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * Visual bookmarks grid shown on the browser start screen.
 */
@Composable
private fun BrowserBookmarksView(
    bookmarks: List<WebBookmark>,
    onBookmarkClick: (WebBookmark) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AssistiveBackground)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌟", fontSize = 24.sp)
                }
                Column {
                    Text(
                        text = stringResource(R.string.browser_bookmarks_title),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Text(
                        text = stringResource(R.string.browser_bookmarks_subtitle),
                        fontSize = 14.sp,
                        color = AssistiveTextSecondary
                    )
                }
            }
        }

        if (bookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.browser_settings_no_sites),
                    fontSize = 16.sp,
                    color = AssistiveTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bookmarks, key = { it.id }) { bookmark ->
                    BookmarkCard(
                        bookmark = bookmark,
                        onClick = { onBookmarkClick(bookmark) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: WebBookmark,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag("bookmark_${bookmark.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(bookmark.colorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = bookmark.iconEmoji, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = bookmark.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = bookmark.domain,
                fontSize = 13.sp,
                color = AssistiveTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Child-friendly block screen shown when child taps a link to an unapproved external domain.
 */
@Composable
private fun BrowserBlockedView(
    blockedHost: String,
    onBackHome: () -> Unit,
    onGoBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛡️", fontSize = 54.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.browser_blocked_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.browser_blocked_desc),
                fontSize = 16.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            if (blockedHost.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Text(
                        text = blockedHost,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.browser_blocked_help),
                fontSize = 14.sp,
                color = AssistiveTextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onBackHome,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AssistiveBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("browser_blocked_back_home")
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.browser_blocked_back_home),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGoBack,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = AssistiveTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.browser_back), fontSize = 16.sp)
            }
        }
    }
}
