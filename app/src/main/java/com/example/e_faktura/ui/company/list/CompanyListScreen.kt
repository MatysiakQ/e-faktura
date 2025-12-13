package com.example.e_faktura.ui.company.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.e_faktura.model.Company
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.company.CompanyViewModel
import com.example.e_faktura.utils.QrCodeGenerator

@Composable
fun CompanyListScreen(
    modifier: Modifier = Modifier,
    companyViewModel: CompanyViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val companies by companyViewModel.companies.collectAsState()
    var showQrDialog by remember { mutableStateOf<Company?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Moje Firmy", style = MaterialTheme.typography.headlineMedium)
        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(companies, key = { it.id }) { company ->
                CompanyItem(company = company, onShowQr = { showQrDialog = company })
            }
        }
    }

    showQrDialog?.let { company ->
        var qrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(company) {
            qrBitmap = QrCodeGenerator.generateQrBitmap(company.nip)
        }

        Dialog(onDismissRequest = { showQrDialog = null }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                qrBitmap?.let {
                    Image(bitmap = it, contentDescription = "Kod QR dla NIP ${company.nip}")
                }
                Text(text = "NIP: ${company.nip}", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun CompanyItem(
    company: Company,
    onShowQr: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = company.businessName, style = MaterialTheme.typography.titleMedium)
            Text(text = "NIP: ${company.nip}", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onShowQr, modifier = Modifier.padding(top = 8.dp)) {
                Text("Pokaż kod QR")
            }
        }
    }
}
