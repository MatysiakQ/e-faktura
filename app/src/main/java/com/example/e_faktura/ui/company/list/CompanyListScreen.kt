package com.example.e_faktura.ui.company.list

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // ✅ Upewnij się, że to ten import
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.e_faktura.model.Company
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.components.IconProvider

@Composable
fun CompanyListScreen(
    navController: NavController,
    viewModel: CompanyListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // ✅ Zmieniono na companies, aby było spójne ze stanem ViewModelu
            uiState.companies.isEmpty() -> {
                Text(
                    text = "Brak firm. Dodaj nową.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ✅ Poprawiona składnia items, aby uniknąć błędu 'Argument type mismatch'
                    items(items = uiState.companies, key = { company -> company.id }) { company ->
                        CompanyListItem(company = company) {
                            navController.navigate("company_details/${company.id}")
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("add_company") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj firmę")
        }
    }
}

@Composable
private fun CompanyListItem(company: Company, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompanyAvatar(iconString = company.icon)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "NIP: ${company.nip}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompanyAvatar(iconString: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val parts = iconString.split(":")
        val type = parts.getOrNull(0)
        val value = parts.getOrNull(1)

        if (type == "CUSTOM" && value != null) {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(value)),
                contentDescription = "Logo firmy",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val iconVector = if (value != null) try {
                IconProvider.getIcon(value)
            } catch (e: Exception) {
                Icons.Outlined.Storefront
            } else Icons.Outlined.Storefront
            Icon(
                imageVector = iconVector,
                contentDescription = "Logo firmy",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}