package com.turkcell.ticketpass.screen


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen() {
    val context = LocalContext.current;

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) {
            result ->
        val contents = result.contents
        if (contents == null) {
            print("İşlem iptal edildi")
        }// işlemi iptal et
        else {
            // QR Okundu contents = qr

            // API'ye contentsi /scan alanına gönder
        }
    }

    fun startCameraScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE) // yalnızca qr kodu tara
            setPrompt("QR Kodu Çerçeveye Getir")
            setBeepEnabled(true)
            setOrientationLocked(false)
            setBarcodeImageEnabled(false)
        }
        scanLauncher.launch(options)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
            granted ->
        if(granted) startCameraScan()
        else print("İzin verilmedi..")
    }

    fun onScanClick() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        if(granted) startCameraScan()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }


    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            uri ->
        if(uri!=null)
        {
            print("QR okundu: {uri}")
        }else{
            print("QR Okunamadı.")
        }
    }

    fun onGalleryClick() {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }


    Scaffold(
        topBar = { TopAppBar(title={ Text("Görevli Ekranı -QR Check-in") }) }
    ) {
            innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Button(
                onClick = { onScanClick() }
            ) {
                Text("QR Kodu OKU")
            }

            Button(
                onClick = { onGalleryClick() }
            ){
                Text("QR Kodu OKU - Galeri")
            }
        }
    }
}