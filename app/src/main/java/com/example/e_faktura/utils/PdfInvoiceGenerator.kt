package com.example.e_faktura.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.e_faktura.model.Invoice
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfInvoiceGenerator {

    fun generateAndOpenPdf(context: Context, invoice: Invoice) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        // Strona A4 (595x842 pts)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // --- Rysowanie treści ---
        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true
        canvas.drawText("FAKTURA NR: ${invoice.invoiceNumber}", 50f, 50f, titlePaint)

        paint.textSize = 14f
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        canvas.drawText("Data wystawienia: ${sdf.format(Date(invoice.dueDate))}", 50f, 80f, paint)

        canvas.drawLine(50f, 100f, 545f, 100f, paint)

        paint.isFakeBoldText = true
        canvas.drawText("NABYWCA:", 50f, 130f, paint)
        paint.isFakeBoldText = false
        canvas.drawText(invoice.buyerName, 50f, 150f, paint)
        canvas.drawText("NIP: ${invoice.buyerNip}", 50f, 170f, paint)

        canvas.drawLine(50f, 250f, 545f, 250f, paint)
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("SUMA DO ZAPŁATY: ${String.format("%.2f", invoice.amount)} PLN", 50f, 280f, paint)

        pdfDocument.finishPage(page)

        // --- Zapis i otwieranie ---
        val file = File(context.cacheDir, "Faktura_${invoice.invoiceNumber.replace("/", "_")}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            context.startActivity(Intent.createChooser(intent, "Otwórz fakturę:"))
        } catch (e: Exception) {
            Toast.makeText(context, "Błąd PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}