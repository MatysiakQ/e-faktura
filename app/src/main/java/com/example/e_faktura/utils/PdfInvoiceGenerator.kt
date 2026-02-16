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
        val boldPaint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        // Kwoty — używaj gross/net/vat jeśli dostępne, inaczej fallback na amount
        val grossAmount = if (invoice.grossAmount > 0) invoice.grossAmount else invoice.amount
        val netAmount   = if (invoice.netAmount > 0)   invoice.netAmount   else invoice.amount
        val vatAmount   = if (invoice.vatAmount > 0)   invoice.vatAmount   else 0.0
        val vatRate     = invoice.vatRate

        // Dane sprzedawcy — z ustawień KSeF lub placeholder
        val sellerNip  = invoice.sellerNip.ifBlank  { "—" }
        val sellerName = invoice.sellerName.ifBlank { "Uzupełnij dane w Ustawienia → KSeF" }

        // ─── NAGŁÓWEK ───────────────────────────────────────────────────
        titlePaint.textSize = 26f
        titlePaint.isFakeBoldText = true
        titlePaint.color = Color.BLACK
        canvas.drawText("FAKTURA VAT", 50f, 60f, titlePaint)

        paint.textSize = 11f
        paint.color = Color.DKGRAY
        canvas.drawText("Numer: ${invoice.invoiceNumber}", 50f, 85f, paint)

        val invoiceDateStr = if (invoice.invoiceDate > 0)
            sdf.format(Date(invoice.invoiceDate)) else sdf.format(Date())
        canvas.drawText("Data wystawienia: $invoiceDateStr", 370f, 85f, paint)

        paint.strokeWidth = 1.5f
        canvas.drawLine(50f, 100f, 545f, 100f, paint)

        // ─── SPRZEDAWCA / NABYWCA ─────────────────────────────────────
        boldPaint.textSize = 10f
        boldPaint.isFakeBoldText = true
        boldPaint.color = Color.BLACK
        canvas.drawText("SPRZEDAWCA:", 50f, 125f, boldPaint)
        canvas.drawText("NABYWCA:", 310f, 125f, boldPaint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.BLACK

        canvas.drawText(sellerName, 50f, 143f, paint)
        canvas.drawText("NIP: $sellerNip", 50f, 158f, paint)

        canvas.drawText(invoice.buyerName.ifBlank { "—" }, 310f, 143f, paint)
        canvas.drawText("NIP: ${invoice.buyerNip.ifBlank { "—" }}", 310f, 158f, paint)

        canvas.drawLine(50f, 175f, 545f, 175f, paint)

        // ─── TABELA POZYCJI ───────────────────────────────────────────
        boldPaint.textSize = 10f
        canvas.drawText("Lp.", 50f, 198f, boldPaint)
        canvas.drawText("Nazwa usługi/towaru", 80f, 198f, boldPaint)
        canvas.drawText("Netto", 350f, 198f, boldPaint)
        canvas.drawText("VAT", 420f, 198f, boldPaint)
        canvas.drawText("Brutto", 480f, 198f, boldPaint)
        canvas.drawLine(50f, 205f, 545f, 205f, paint)

        paint.textSize = 10f
        val description = invoice.serviceDescription.ifBlank { "Usługa" }
        // Obetnij opis jeśli za długi
        val shortDesc = if (description.length > 38) description.take(35) + "..." else description
        canvas.drawText("1.", 50f, 222f, paint)
        canvas.drawText(shortDesc, 80f, 222f, paint)
        canvas.drawText("${String.format("%.2f", netAmount)} PLN", 340f, 222f, paint)
        canvas.drawText("${vatRate}%", 420f, 222f, paint)
        canvas.drawText("${String.format("%.2f", grossAmount)} PLN", 470f, 222f, paint)

        canvas.drawLine(50f, 232f, 545f, 232f, paint)

        // ─── PODSUMOWANIE VAT ─────────────────────────────────────────
        paint.textSize = 10f
        canvas.drawText("Stawka VAT", 310f, 260f, boldPaint)
        canvas.drawText("Netto", 390f, 260f, boldPaint)
        canvas.drawText("VAT", 445f, 260f, boldPaint)
        canvas.drawText("Brutto", 490f, 260f, boldPaint)

        canvas.drawText(if (vatRate == "ZW") "Zwolniony" else "${vatRate}%", 310f, 276f, paint)
        canvas.drawText("${String.format("%.2f", netAmount)}", 390f, 276f, paint)
        canvas.drawText("${String.format("%.2f", vatAmount)}", 445f, 276f, paint)
        canvas.drawText("${String.format("%.2f", grossAmount)}", 490f, 276f, paint)

        // ─── DO ZAPŁATY ───────────────────────────────────────────────
        canvas.drawLine(50f, 310f, 545f, 310f, paint)

        boldPaint.textSize = 14f
        canvas.drawText("DO ZAPŁATY:", 50f, 340f, boldPaint)

        titlePaint.textSize = 18f
        canvas.drawText("${String.format("%.2f", grossAmount)} PLN", 350f, 340f, titlePaint)

        paint.textSize = 10f
        val dueDateStr = if (invoice.dueDate > 0) sdf.format(Date(invoice.dueDate)) else "—"
        canvas.drawText("Termin płatności: $dueDateStr", 50f, 360f, paint)
        canvas.drawText("Forma płatności: ${invoice.paymentMethod}", 50f, 375f, paint)

        // ─── KSEF ─────────────────────────────────────────────────────
        if (invoice.ksefReferenceNumber.isNotBlank()) {
            paint.color = Color.GRAY
            paint.textSize = 9f
            canvas.drawText("Numer KSeF: ${invoice.ksefReferenceNumber}", 50f, 420f, paint)
        }

        // ─── STOPKA ───────────────────────────────────────────────────
        paint.color = Color.LTGRAY
        canvas.drawLine(50f, 810f, 545f, 810f, paint)
        paint.textSize = 8f
        canvas.drawText("Wygenerowano przez e-Faktura App", 50f, 825f, paint)

        pdfDocument.finishPage(page)

        // ─── ZAPIS I OTWIERANIE ───────────────────────────────────────
        val file = File(context.cacheDir, "Faktura_${invoice.invoiceNumber.replace("/", "_")}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri: Uri = FileProvider.getUriForFile(
                context, "${context.packageName}.provider", file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            context.startActivity(Intent.createChooser(intent, "Otwórz fakturę"))
        } catch (e: Exception) {
            Toast.makeText(context, "Błąd generowania PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
