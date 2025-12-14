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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.e_faktura.model.CompanyIcon
import com.example.e_faktura.model.IconType
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.company.add.CompanyFormEvent
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

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
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
                val iconString = "${selectedIcon.type}:${selectedIcon.value}"
                viewModel.onEvent(CompanyFormEvent.IconChanged(iconString))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                CompanyAvatar(iconString = state.icon, onClick = { showIconPicker = true })
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.nip,
                    onValueChange = { viewModel.onEvent(CompanyFormEvent.NipChanged(it)) },
                    label = { Text("NIP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.loadDataFromNip(state.nip) }) {
                            Icon(Icons.Default.Search, contentDescription = "Szukaj w GUS")
                        }
                    },
                    isError = state.error?.contains("NIP") == true
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.businessName,
                    onValueChange = { viewModel.onEvent(CompanyFormEvent.BusinessNameChanged(it)) },
                    label = { Text("Nazwa Firmy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    isError = state.error?.contains("Nazwa") == true
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.onEvent(CompanyFormEvent.AddressChanged(it)) },
                    label = { Text("Adres (Ulica, Kod, Miasto)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.ownerFullName,
                    onValueChange = { viewModel.onEvent(CompanyFormEvent.OwnerFullNameChanged(it)) },
                    label = { Text("Imię i nazwisko właściciela") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.bankAccount,
                    onValueChange = { viewModel.onEvent(CompanyFormEvent.BankAccountChanged(it)) },
                    label = { Text("Numer konta bankowego") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveCompany() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Zapisz zmiany")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

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
        val type = if (parts.getOrNull(0) == "CUSTOM") IconType.CUSTOM else IconType.PREDEFINED
        val value = parts.getOrNull(1) ?: "Business"
        val companyIcon = CompanyIcon(type, value)

        if (companyIcon.type == IconType.CUSTOM) {
            AsyncImage(
                model = Uri.parse(companyIcon.value),
                contentDescription = "Logo firmy",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val iconVector = IconProvider.getIcon(companyIcon.value)
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
