package com.iistable.atreus.ui
import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
@SuppressLint("SetJavaScriptEnabled")
@Composable fun PlaygroundScreen() { AndroidView(factory = { WebView(it).apply { webViewClient = WebViewClient(); settings.javaScriptEnabled = true; loadUrl("https://itch.io/games/html5") } }, modifier = Modifier.fillMaxSize()) }
