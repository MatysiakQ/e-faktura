package com.example.e_faktura.utils

import com.example.e_faktura.model.Invoice
import java.text.SimpleDateFormat
import java.util.*

object KsefXmlGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    fun generate(invoice: Invoice, sellerNip: String, sellerName: String): String {
        val now = Date()
        val invoiceDate = if (invoice.invoiceDate > 0) Date(invoice.invoiceDate) else now
        val dueDate = if (invoice.dueDate > 0) Date(invoice.dueDate) else now

        val netAmount = if (invoice.netAmount > 0) invoice.netAmount else invoice.amount
        val vatRate = invoice.vatRate
        val vatAmount = if (invoice.vatAmount > 0) invoice.vatAmount else 0.0
        val grossAmount = if (invoice.grossAmount > 0) invoice.grossAmount else invoice.amount

        return """<?xml version="1.0" encoding="UTF-8"?>
<Faktura xmlns="http://crd.gov.pl/wzor/2023/06/29/12648/"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <Naglowek>
    <KodFormularza kodSystemowy="FA (3)" wersjaSchemy="1-0E">FA</KodFormularza>
    <WariantFormularza>3</WariantFormularza>
    <DataWytworzeniaFa>${dateTimeFormat.format(now)}</DataWytworzeniaFa>
    <SystemInfo>e-Faktura App v1.0</SystemInfo>
  </Naglowek>
  <Podmiot1>
    <DaneIdentyfikacyjne>
      <NIP>${sellerNip.filter { it.isDigit() }}</NIP>
      <Nazwa>${escapeXml(sellerName)}</Nazwa>
    </DaneIdentyfikacyjne>
  </Podmiot1>
  <Podmiot2>
    <DaneIdentyfikacyjne>
      <NIP>${invoice.buyerNip.filter { it.isDigit() }}</NIP>
      <Nazwa>${escapeXml(invoice.buyerName)}</Nazwa>
    </DaneIdentyfikacyjne>
    <Rola>2</Rola>
  </Podmiot2>
  <Fa>
    <KodWaluty>PLN</KodWaluty>
    <P_1>${dateFormat.format(invoiceDate)}</P_1>
    <P_1M>${dateFormat.format(invoiceDate).substring(0, 7)}</P_1M>
    <P_2>${escapeXml(invoice.invoiceNumber)}</P_2>
    <P_6>${dateFormat.format(invoiceDate)}</P_6>
    <FaWiersz>
      <NrWierszaFa>1</NrWierszaFa>
      <P_7>${escapeXml(invoice.serviceDescription.ifBlank { "Usługa" })}</P_7>
      <P_8A>szt.</P_8A>
      <P_8B>1</P_8B>
      <P_9A>${formatAmount(netAmount)}</P_9A>
      <P_11>${formatAmount(netAmount)}</P_11>
      <P_12>${if (vatRate == "ZW") "zw" else vatRate}</P_12>
    </FaWiersz>
    <Rozliczenie>
      <Stawki>
        <Stawka>
          <P_12>${if (vatRate == "ZW") "zw" else vatRate}</P_12>
          <P_13>${formatAmount(netAmount)}</P_13>
          <P_14>${formatAmount(vatAmount)}</P_14>
        </Stawka>
      </Stawki>
      <P_15>${formatAmount(grossAmount)}</P_15>
    </Rozliczenie>
    <Platnosc>
      <TerminPlatnosci>
        <Termin>${dateFormat.format(dueDate)}</Termin>
      </TerminPlatnosci>
      <FormaPlatnosci>${mapPaymentMethod(invoice.paymentMethod)}</FormaPlatnosci>
    </Platnosc>
    <RodzajFaktury>VAT</RodzajFaktury>
  </Fa>
</Faktura>""".trimIndent()
    }

    private fun formatAmount(amount: Double): String =
        String.format(Locale.US, "%.2f", amount)

    private fun mapPaymentMethod(method: String): String = when (method) {
        "PRZELEW"    -> "6"
        "GOTÓWKA"    -> "1"
        "KARTA"      -> "3"
        "KOMPENSATA" -> "5"
        else         -> "6"
    }

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
