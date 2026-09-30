package com.feder.compose.video

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * InlineYouTubePlayer v1 — WebView + iframe embed.
 *
 * Используется вместо FederVideoPlayer для YouTube, потому что
 * android-youtubeExtractor:v2.1.0 (2019) умер — YouTube поменял API.
 * WebView-embed работает всегда.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InlineYouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = false,
    onClose: (() -> Unit)? = null
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        @Suppress("DEPRECATION")
                        allowFileAccess = true
                    }
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    setBackgroundColor(AndroidColor.BLACK)

                    val html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                          <meta name="viewport"
                                content="width=device-width, initial-scale=1, maximum-scale=1">
                          <style>
                            html,body{margin:0;padding:0;background:#000;height:100%;overflow:hidden}
                            iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0}
                          </style>
                        </head>
                        <body>
                          <iframe
                            src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&rel=0&modestbranding=1"
                            allow="autoplay; encrypted-media; fullscreen; picture-in-picture"
                            allowfullscreen>
                          </iframe>
                        </body>
                        </html>
                    """.trimIndent()

                    loadDataWithBaseURL(
                        "https://www.youtube.com",
                        html,
                        "text/html",
                        "utf-8",
                        null
                    )
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showCloseButton && onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color(0xAA000000), CircleShape)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Закрыть",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                try {
                    loadUrl("about:blank")
                    stopLoading()
                    destroy()
                } catch (_: Exception) {}
            }
            webView = null
        }
    }
}

/**
 * Fullscreen-обёртка над InlineYouTubePlayer.
 * Заменяет старый FullscreenVideoPlayer для YouTube-ссылок.
 */
@Composable
fun FullscreenYouTubePlayer(
    videoId: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        InlineYouTubePlayer(
            videoId = videoId,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            showCloseButton = true,
            onClose = onClose
        )
    }
}
