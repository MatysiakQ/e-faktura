package com.example.e_faktura.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrCodeGenerator {

    private const val QR_CODE_SIZE = 512

    suspend fun generateQrBitmap(content: String): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                QR_CODE_SIZE,
                QR_CODE_SIZE
            )
            val bitmap = Bitmap.createBitmap(
                QR_CODE_SIZE,
                QR_CODE_SIZE,
                Bitmap.Config.RGB_565
            )
            for (x in 0 until QR_CODE_SIZE) {
                for (y in 0 until QR_CODE_SIZE) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap.asImageBitmap()
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}
