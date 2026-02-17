@file:OptIn(ExperimentalMaterial3Api::class)

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
import com.example.e_faktura.ui.components.IconPickerDialog
import com.example.e_faktura.ui.components.IconProvider
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EditCompanyScreen(
    navController: NavController,
    viewModel: EditCompanyViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showIconPicker by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.updateIcon("CUSTOM:$uri")
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                EditCompanyEvent.SaveSuccess -> {
                    Toast.makeText(context, "Zmiany zapisane!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is EditCompanyEvent.ShowError -> {
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
                title = { Text("Edytuj firmę", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.notFound -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("Nie znaleziono firmy", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))

                        EditCompanyAvatar(
                            iconString = state.icon,
                            onClick = { showIconPicker = true }
                        )

                        Spacer(Modifier.height(24.dp))

                        // NIP + przycisk GUS
                        OutlinedTextField(
                            value = state.nip,
                            onValueChange = { viewModel.updateNip(it) },
                            label = { Text("NIP") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.error?.contains("NIP") == true,
                            supportingText = {
                                if (state.error?.contains("NIP") == true)
                                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.fetchFromGus() }) {
                                    Icon(Icons.Default.Search, "Odśwież z GUS")
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Nazwa firmy
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Pełna nazwa firmy") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = state.error?.contains("nazwa", true) == true,
                            supportingText = {
                                if (state.error?.contains("nazwa", true) == true)
                                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Adres
                        OutlinedTextField(
                            value = state.address,
                            onValueChange = { viewModel.updateAddress(it) },
                            label = { Text("Ulica i numer") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Kod pocztowy + Miasto
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.postalCode,
                                onValueChange = { viewModel.updatePostalCode(it) },
                                label = { Text("Kod") },
                                modifier = Modifier.weight(0.4f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = state.city,
                                onValueChange = { viewModel.updateCity(it) },
                                label = { Text("Miasto") },
                                modifier = Modifier.weight(0.6f),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Właściciel
                        OutlinedTextField(
                            value = state.ownerFullName,
                            onValueChange = { viewModel.updateOwner(it) },
                            label = { Text("Właściciel / Reprezentant") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Konto bankowe
                        OutlinedTextField(
                            value = state.bankAccount,
                            onValueChange = { viewModel.updateBankAccount(it) },
                            label = { Text("Numer konta bankowego") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Błąd ogólny
                        if (state.error != null && !state.error!!.contains("NIP") && !state.error!!.contains("nazwa", true)) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.saveChanges() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !state.isSaving,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("ZAPISZ ZMIANY", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(40.dp))
                    }

                    if (state.isSaving) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditCompanyAvatar(iconString: String, onClick: () -> Unit) {
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
            val iconVector = try { IconProvider.getIcon(value) } catch (e: Exception) { Icons.Default.Business }
            Icon(iconVector, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.padding(6.dp), tint = Color.White)
        }
    }
}
