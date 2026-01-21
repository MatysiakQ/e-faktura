package com.example.e_faktura.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.e_faktura.model.Invoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfInvoiceGenerator {

    private const val PAGE_WIDTH = 595 // A4 w punktach
    private const val PAGE_HEIGHT = 842

    suspend fun generateAndSharePdf(context: Context, invoice: Invoice) = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // --- KONFIGURACJA PĘDZLI ---
        val titlePaint = TextPaint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val headerPaint = TextPaint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val bodyPaint = TextPaint().apply {
            textSize = 12f
            color = android.graphics.Color.BLACK
        }

        val linePaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 60f
        val margin = 40f

        // 1. NAGŁÓWEK
        canvas.drawText("FAKTURA VAT", margin, y, titlePaint)
        y += 30f
        canvas.drawText("Numer: ${invoice.invoiceNumber}", margin, y, bodyPaint)
        y += 40f

        // LINIA PODZIAŁU
        canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, linePaint)
        y += 30f

        // 2. DATY
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        canvas.drawText("Data wystawienia: ${dateFormat.format(Date(invoice.date))}", margin, y, bodyPaint)
        y += 20f
        // Dodajemy termin płatności, jeśli masz go w modelu
        canvas.drawText("Termin płatności: ${dateFormat.format(Date(invoice.dueDate))}", margin, y, bodyPaint)
        y += 40f

        // 3. NABYWCA
        canvas.drawText("NABYWCA:", margin, y, headerPaint)
        y += 20f

        val buyerInfo = """
            ${invoice.buyerName}
            NIP: ${invoice.buyerNip}
        """.trimIndent()

        drawStaticText(canvas, buyerInfo, margin, y, bodyPaint)
        y += 60f

        // 4. PODSUMOWANIE FINANSOWE
        canvas.drawLine(margin, y, PAGE_WIDTH - margin, y, linePaint)
        y += 40f

        canvas.drawText("PODSUMOWANIE:", margin, y, headerPaint)
        y += 25f

        // Kwota pogrubiona
        val totalAmount = String.format("%.2f", invoice.amount)
        canvas.drawText("Do zapłaty: $totalAmount PLN", margin, y, headerPaint)
        y += 20f
        canvas.drawText("Status: ${if (invoice.isPaid) "ZAPŁACONO" else "DO ZAPŁATY"}", margin, y, bodyPaint)

        // STOPKA
        canvas.drawText("Wygenerowano z aplikacji e-Faktura", margin, PAGE_HEIGHT - 40f, TextPaint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
        })

        document.finishPage(page)

        // --- ZAPIS I UDOSTĘPNIANIE ---
        try {
            val pdfFile = File(context.cacheDir, "Faktura_${invoice.invoiceNumber.replace("/", "_")}.pdf")
            document.writeTo(FileOutputStream(pdfFile))

            val pdfUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            sharePdf(context, pdfUri)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    private fun drawStaticText(canvas: Canvas, text: String, x: Float, y: Float, paint: TextPaint) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, PAGE_WIDTH - 80)
            .build()
        canvas.save()
        canvas.translate(x, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun sharePdf(context: Context, uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Udostępnij fakturę...")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}