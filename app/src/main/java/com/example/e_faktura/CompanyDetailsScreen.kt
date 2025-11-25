package com.example.e_faktura

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CompanyQrData(
    val type: CompanyType,
    val nip: String,
    val address: String,
    val ownerFullName: String? = null,
    val businessName: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreen(navController: NavController, company: Company, invoiceViewModel: InvoiceViewModel) {
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }
    val qrColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val qrBgColor = MaterialTheme.colorScheme.surface.toArgb()

    if (showIconPicker) {
        IconPickerDialog(onDismiss = { showIconPicker = false }) { newIcon ->
            invoiceViewModel.updateCompanyIcon(company, newIcon)
        }
    }

    LaunchedEffect(company, qrColor, qrBgColor) {
        val companyQrData = CompanyQrData(
            type = company.type,
            nip = company.nip,
            address = company.address,
            ownerFullName = company.ownerFullName,
            businessName = company.businessName
        )
        val companyJson = Json.encodeToString(companyQrData)
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(companyJson, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) qrColor else qrBgColor)
                }
            }
            qrCodeBitmap = bmp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(company.displayName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        invoiceViewModel.deleteCompany(company)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Usuń")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { showIconPicker = true }
                ) {
                    when (company.icon.type) {
                        IconType.PREDEFINED -> {
                            Icon(
                                imageVector = IconProvider.getIcon(company.icon.iconName),
                                contentDescription = "Ikona firmy",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconType.CUSTOM -> {
                            Image(
                                painter = rememberAsyncImagePainter(model = Uri.parse(company.icon.iconName)),
                                contentDescription = "Ikona firmy",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            item {
                qrCodeBitmap?.let {
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(250.dp)
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailItem(icon = Icons.Filled.Info, label = "NIP", value = company.nip)
                        DetailItem(icon = Icons.Filled.LocationOn, label = "Adres", value = company.address)
                        if (company.type == CompanyType.SOLE_PROPRIETORSHIP) {
                            company.ownerFullName?.let { owner ->
                                DetailItem(icon = Icons.Filled.Person, label = "Właściciel", value = owner)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}