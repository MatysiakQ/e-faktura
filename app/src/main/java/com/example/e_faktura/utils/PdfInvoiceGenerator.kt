package com.example.e_faktura.utils

import android.content.Context
import android.content.Intent
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

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 20f
            isFakeBoldText = true
            color = android.graphics.Color.BLACK
        }

        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 12f
            color = android.graphics.Color.DKGRAY
        }

        var yPosition = 50f

        // --- Title ---
        canvas.drawText("Faktura nr: ${invoice.invoiceNumber}", 40f, yPosition, titlePaint)
        yPosition += 60f

        // --- Invoice Details using StaticLayout for robust text handling ---
        val invoiceDetails = """
            Nabywca: ${invoice.buyerName}
            NIP Nabywcy: ${invoice.buyerNip}
            
            Data wystawienia: ${java.text.SimpleDateFormat("dd-MM-yyyy").format(java.util.Date(invoice.date))}
            Status: ${if (invoice.isPaid) "Zapłacono" else "Nie zapłacono"}
            
            Kwota: ${String.format("%.2f", invoice.amount)} PLN
        """.trimIndent()

        val textLayout = StaticLayout.Builder.obtain(invoiceDetails, 0, invoiceDetails.length, textPaint, canvas.width - 80)
            .build()

        canvas.save()
        canvas.translate(40f, yPosition)
        textLayout.draw(canvas)
        canvas.restore()

        document.finishPage(page)

        // --- Save and Share ---
        try {
            val pdfFile = File(context.cacheDir, "invoice_${invoice.id}.pdf")
            document.writeTo(FileOutputStream(pdfFile))

            val pdfUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            
            sharePdf(context, pdfUri)

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exception, e.g., show a toast
        } finally {
            document.close()
        }
    }

    private fun sharePdf(context: Context, uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Udostępnij fakturę PDF..."))
    }
}
