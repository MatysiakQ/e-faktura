package com.example.e_faktura.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import java.util.Date
import java.util.Locale

/**
 * UNIFIED utility for generating and sharing PDF invoices.
 * This is the single source of truth for all PDF generation.
 */
object PdfInvoiceGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    /**
     * Generates a PDF from an Invoice object, saves it to the cache, and triggers a system share intent.
     */
    suspend fun generateAndSharePdf(
        context: Context,
        invoice: Invoice
    ) = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Konfiguracja pędzla do tytułu
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 20f
            isFakeBoldText = true
            color = Color.BLACK
        }

        // Konfiguracja pędzla do treści
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
            color = Color.DKGRAY
        }

        var yPosition = 50f

        // --- Tytuł ---
        canvas.drawText("Faktura nr: ${invoice.invoiceNumber}", 40f, yPosition, titlePaint)
        yPosition += 40f

        // --- Formatowanie Daty ---
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date(invoice.date))

        // --- Treść Faktury (Zaktualizowana do nowego modelu) ---
        // Używamy StaticLayout do ładnego łamania linii
        val invoiceDetails = """
            Typ: ${if (invoice.type == "SALE") "Sprzedaż" else "Zakup"}
            Data wystawienia: $dateStr
            Status: ${if (invoice.isPaid) "Opłacona" else "Do zapłaty"}
            
            ------------------------------------------------
            NABYWCA:
            ${invoice.buyerName}
            NIP: ${invoice.buyerNip}
            ------------------------------------------------
            
            SZCZEGÓŁY PŁATNOŚCI:
            
            Kwota Netto:   ${String.format("%.2f", invoice.netValue)} PLN
            Stawka VAT:    ${(invoice.vatRate * 100).toInt()}%
            Kwota VAT:     ${String.format("%.2f", invoice.vatValue)} PLN
            
            SUMA BRUTTO:   ${String.format("%.2f", invoice.grossValue)} PLN
        """.trimIndent()

        // Rysowanie tekstu wieloliniowego
        val textLayout = StaticLayout.Builder.obtain(
            invoiceDetails,
            0,
            invoiceDetails.length,
            textPaint,
            PAGE_WIDTH - 80
        ).build()

        canvas.save()
        canvas.translate(40f, yPosition)
        textLayout.draw(canvas)
        canvas.restore()

        document.finishPage(page)

        // --- Zapis i Udostępnianie ---
        try {
            val fileName = "faktura_${invoice.invoiceNumber.replace("/", "_")}.pdf"
            val pdfFile = File(context.cacheDir, fileName)

            // Nadpisz jeśli istnieje
            if (pdfFile.exists()) pdfFile.delete()

            document.writeTo(FileOutputStream(pdfFile))

            // Pobranie URI przez FileProvider (wymaga konfiguracji w AndroidManifest.xml)
            val pdfUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // Upewnij się, że to pasuje do authorities w Manifest
                pdfFile
            )

            // Przełączamy się na wątek główny, aby uruchomić Activity
            withContext(Dispatchers.Main) {
                sharePdf(context, pdfUri)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    private fun sharePdf(context: Context, uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Udostępnij fakturę PDF...")
        // Dodajemy flagę na wypadek wywołania spoza Activity Context
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooser)
    }
}