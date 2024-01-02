package com.arcxp.thearcxp.web

import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.arcxp.thearcxp.LocalWebViewModel
import com.arcxp.thearcxp.article.cleanup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    showTopBar: Boolean,
    hideNavBars: Boolean,
    backNavigation: () -> Unit,
    enableJavascript: Boolean
) {
    val context = LocalContext.current
    val webViewModel= LocalWebViewModel.current
    val lifecycleObserver = rememberUpdatedState(
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (hideNavBars) {
                        webViewModel.setHideBars(true)
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    if (hideNavBars) {
                        webViewModel.setHideBars(false)
                    }
                }

                else -> { /* Do nothing for other lifecycle events */
                }
            }
        }
    )
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver.value)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver.value)
        }
    }

    val webView = remember {
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(
                    view: WebView,
                    handler: SslErrorHandler,
                    error: SslError
                ) {
                    // Handle SSL error appropriately here
                    handler.cancel()
                }
            }
            webChromeClient = WebChromeClient()
            settings.apply {
                javaScriptEnabled = enableJavascript

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    allowFileAccessFromFileURLs = false
                    @Suppress("DEPRECATION")
                    allowUniversalAccessFromFileURLs = false
                }//these values default to false in oreo+, deprecated in 30

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    allowContentAccess = false
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = true
                }
            }
        }
    }

    Column {
        if (showTopBar) {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            backNavigation()
                            cleanup()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                },
            )
        }
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        ) {
            it.loadUrl(url)
        }
    }

}