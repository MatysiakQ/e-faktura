package com.example.e_faktura.ui.company.edit

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // <--- NOWY IMPORT
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// USUNIĘTO: import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
// USUNIĘTO: import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.company.add.CompanyFormViewModel
import com.example.e_faktura.ui.company.add.UiEvent
import com.example.e_faktura.ui.components.IconPickerDialog
import com.example.e_faktura.ui.components.IconProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanyScreen(
    navController: NavController,
    companyId: String,
    // ZMIANA: Używamy hiltViewModel() zamiast fabryki
    viewModel: CompanyFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showIconPicker by remember { mutableStateOf(false) }

    // 1. Ładowanie danych firmy na starcie
    LaunchedEffect(companyId) {
        viewModel.loadCompany(companyId)
    }

    // 2. Obsługa zdarzeń (Zapis / Błąd)
    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UiEvent.SaveSuccess -> {
                    Toast.makeText(context, "Zmiany zapisane", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { selectedIcon ->
                // Formatujemy ikonę jako String "TYPE:VALUE" zgodnie z ViewModelem
                viewModel.updateIcon("${selectedIcon.type}:${selectedIcon.value}")
                showIconPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj firmę") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Awatar
                CompanyAvatar(iconString = state.icon, onClick = { showIconPicker = true })

                Spacer(Modifier.height(24.dp))

                // NIP + Przycisk GUS
                OutlinedTextField(
                    value = state.nip,
                    onValueChange = { viewModel.updateNip(it) },
                    label = { Text("NIP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (state.nip.isNotBlank()) viewModel.searchByNip()
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Szukaj w GUS")
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))

                // Nazwa Firmy
                OutlinedTextField(
                    value = state.businessName,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Nazwa Firmy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                // Adres (Ulica)
                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    label = { Text("Adres (Ulica i numer)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                // Kod Pocztowy i Miasto
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.postalCode,
                        onValueChange = { viewModel.updatePostalCode(it) },
                        label = { Text("Kod") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = state.city,
                        onValueChange = { viewModel.updateCity(it) },
                        label = { Text("Miasto") },
                        modifier = Modifier.weight(2f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Właściciel
                OutlinedTextField(
                    value = state.ownerFullName,
                    onValueChange = { viewModel.updateOwner(it) },
                    label = { Text("Imię i nazwisko właściciela") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                // Konto bankowe
                OutlinedTextField(
                    value = state.bankAccount,
                    onValueChange = { viewModel.updateBankAccount(it) },
                    label = { Text("Numer konta bankowego") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                Spacer(Modifier.height(32.dp))

                // Przycisk Zapisz
                Button(
                    onClick = { viewModel.saveCompany() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !state.isLoading
                ) {
                    Text("Zapisz zmiany")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// Lokalna wersja komponentu Avatar
@Composable
private fun CompanyAvatar(iconString: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val parts = iconString.split(":", limit = 2)
        val type = parts.getOrNull(0)
        val value = parts.getOrNull(1)

        if (type == "CUSTOM" && value != null) {
            AsyncImage(
                model = Uri.parse(value),
                contentDescription = "Logo firmy",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val iconVector = if (value != null) {
                try { IconProvider.getIcon(value) } catch (e: Exception) { Icons.Outlined.Storefront }
            } else Icons.Outlined.Storefront

            Icon(
                imageVector = iconVector,
                contentDescription = "Logo firmy",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Zmień ikonę",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}