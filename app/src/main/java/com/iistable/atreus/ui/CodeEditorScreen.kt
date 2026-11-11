package com.iistable.atreus.ui
import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
@SuppressLint("SetJavaScriptEnabled")
@Composable fun CodeEditorScreen() { var c by remember { mutableStateOf("<h1>Hello</h1>") }; var p by remember { mutableStateOf(false) }; if (p) { Column(Modifier.fillMaxSize()) { Button(onClick = { p = false }, Modifier.padding(8.dp)) { Text("Back") }; AndroidView(factory = { WebView(it).apply { webViewClient = WebViewClient(); settings.javaScriptEnabled = true } }, update = { it.loadDataWithBaseURL(null, c, "text/html", "UTF-8", null) }, modifier = Modifier.weight(1f)) } } else { Column(Modifier.fillMaxSize().padding(16.dp)) { OutlinedTextField(value = c, onValueChange = { c = it }, modifier = Modifier.weight(1f)); Button(onClick = { p = true }, Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Run") } } } }
