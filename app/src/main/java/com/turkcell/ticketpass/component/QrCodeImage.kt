package com.turkcell.ticketpass.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(content) { generateQRBitmap(content) }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Bilet QR Kodu",
            modifier = modifier,
            filterQuality = FilterQuality.None
        )
    }
}

private fun generateQRBitmap(content: String): Bitmap? = runCatching {
    val encoder = BarcodeEncoder()
    encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 600, 600)
}.getOrNull()