package com.example.e_faktura.ui.company.edit

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.e_faktura.model.IconType
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.company.add.CompanyFormViewModel
import com.example.e_faktura.ui.company.add.UiEvent
import com.example.e_faktura.ui.components.IconPickerDialog
import com.example.e_faktura.ui.components.IconProvider
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanyScreen(
    navController: NavController,
    viewModel: CompanyFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showIconPicker by remember { mutableStateOf(false) }

    // Launcher do galerii zdjęć
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateIcon("CUSTOM:$uri")
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                UiEvent.SaveSuccess -> {
                    Toast.makeText(context, "Zmiany zapisane pomyślnie!", Toast.LENGTH_SHORT).show()
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
                viewModel.updateIcon("${selectedIcon.type}:${selectedIcon.value}")
                showIconPicker = false
            },
            // ✅ NAPRAWIONO: Dodano brakujący parametr galerii
            onPickFromGallery = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                showIconPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edytuj dane firmy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Awatar z obsługą CUSTOM/VECTOR
                CompanyAvatar(iconString = state.icon, onClick = { showIconPicker = true })

                Spacer(Modifier.height(24.dp))

                // Pole NIP
                OutlinedTextField(
                    value = state.nip,
                    onValueChange = { viewModel.updateNip(it) },
                    label = { Text("NIP") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.error?.contains("NIP") == true,
                    supportingText = {
                        if (state.error?.contains("NIP") == true) Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.loadDataFromNip(state.nip) }) {
                            Icon(Icons.Default.Search, contentDescription = "Szukaj w GUS")
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Nazwa Firmy
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Pełna nazwa firmy") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.error?.contains("Nazwa") == true,
                    supportingText = {
                        if (state.error?.contains("Nazwa") == true) Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Adres
                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    label = { Text("Ulica i numer") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Właściciel
                OutlinedTextField(
                    value = state.ownerFullName,
                    onValueChange = { viewModel.updateOwner(it) },
                    label = { Text("Właściciel / Reprezentant") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Konto bankowe
                OutlinedTextField(
                    value = state.bankAccount,
                    onValueChange = { viewModel.updateBankAccount(it) },
                    label = { Text("Numer konta bankowego") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveCompany() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ZAPISZ ZMIANY", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(40.dp))
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun CompanyAvatar(iconString: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val parts = iconString.split(":", limit = 2)
        // ✅ NAPRAWIONO: Zmiana PREDEFINED na VECTOR
        val type = if (parts.getOrNull(0) == "CUSTOM") IconType.CUSTOM else IconType.VECTOR
        val value = parts.getOrNull(1) ?: "Business"

        if (type == IconType.CUSTOM) {
            AsyncImage(
                model = Uri.parse(value),
                contentDescription = "Logo firmy",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // ✅ NAPRAWIONO: Bezpieczne pobieranie ikony (Business zamiast Storefront)
            val iconVector = try {
                IconProvider.getIcon(value)
            } catch (e: Exception) { Icons.Default.Business }

            Icon(
                imageVector = iconVector,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            tonalElevation = 4.dp
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Zmień",
                modifier = Modifier.padding(6.dp),
                tint = Color.White
            )
        }
    }
}