package com.iistable.atreus.ui
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
@Composable
fun QrScannerScreen(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current; val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var scanned by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    LaunchedEffect(Unit) { if (!hasPermission) launcher.launch(Manifest.permission.CAMERA) }
    if (!hasPermission) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("Grant Camera") } }; return }
    AndroidView(factory = { ctx ->
        val pv = PreviewView(ctx); val cf = ProcessCameraProvider.getInstance(ctx)
        cf.addListener({ val cp = cf.get(); val p = Preview.Builder().build().also { it.setSurfaceProvider(pv.surfaceProvider) }
            val ia = ImageAnalysis.Builder().build(); ia.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy -> if (!scanned) { proxy.image?.let { img -> BarcodeScanning.getClient().process(InputImage.fromMediaImage(img, proxy.imageInfo.rotationDegrees)).addOnSuccessListener { barcodes -> barcodes.firstOrNull()?.rawValue?.let { if (!scanned) { scanned = true; onQrScanned(it) } } }.addOnCompleteListener { proxy.close() } } ?: proxy.close() } else proxy.close() }
            try { cp.unbindAll(); cp.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, p, ia) } catch (e: Exception) {}
        }, ContextCompat.getMainExecutor(ctx)); pv
    }, modifier = Modifier.fillMaxSize())
}
