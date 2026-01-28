package com.example.e_faktura.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
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

        // --- NAGŁÓWEK ---
        titlePaint.textSize = 26f
        titlePaint.isFakeBoldText = true
        titlePaint.color = Color.BLACK
        canvas.drawText("FAKTURA VAT", 50f, 60f, titlePaint)

        paint.textSize = 12f
        paint.color = Color.DKGRAY
        canvas.drawText("Numer: ${invoice.invoiceNumber}", 50f, 85f, paint) //

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        canvas.drawText("Data wystawienia: ${sdf.format(Date(invoice.dueDate))}", 380f, 85f, paint)

        // Linia oddzielająca
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 110f, 545f, 110f, paint)

        // --- SEKRETY: SPRZEDAWCA I NABYWCA ---
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.color = Color.BLACK
        canvas.drawText("SPRZEDAWCA:", 50f, 140f, paint)
        canvas.drawText("NABYWCA:", 300f, 140f, paint)

        paint.isFakeBoldText = false
        // Tutaj powinieneś przekazać dane swojej firmy
        canvas.drawText("Twoja Firma Sp. z o.o.", 50f, 160f, paint)
        canvas.drawText("ul. Testowa 1, 00-000 Miasto", 50f, 175f, paint)
        canvas.drawText("NIP: 1234567890", 50f, 190f, paint)

        // Dane nabywcy z obiektu invoice
        canvas.drawText(invoice.buyerName, 300f, 160f, paint)
        canvas.drawText("NIP: ${invoice.buyerNip}", 300f, 175f, paint)

        // --- TABELA / PODSUMOWANIE ---
        paint.strokeWidth = 1f
        canvas.drawLine(50f, 220f, 545f, 220f, paint)

        paint.isFakeBoldText = true
        canvas.drawText("Nazwa usługi/towaru", 50f, 240f, paint)
        canvas.drawText("Kwota brutto", 450f, 240f, paint)
        paint.isFakeBoldText = false

        canvas.drawLine(50f, 250f, 545f, 250f, paint)

        canvas.drawText("Usługa handlowa / Sprzedaż towaru", 50f, 275f, paint)
        canvas.drawText("${String.format("%.2f", invoice.amount)} PLN", 450f, 275f, paint) //

        // --- DO ZAPŁATY ---
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("RAZEM DO ZAPŁATY:", 250f, 350f, paint)
        canvas.drawText("${String.format("%.2f", invoice.amount)} PLN", 420f, 350f, paint)

        pdfDocument.finishPage(page)

        // --- ZAPIS I OTWIERANIE ---
        val file = File(context.cacheDir, "Faktura_${invoice.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            context.startActivity(Intent.createChooser(intent, "Otwórz fakturę:"))
        } catch (e: Exception) {
            Toast.makeText(context, "Błąd generowania PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}