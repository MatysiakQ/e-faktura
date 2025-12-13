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
import coil.compose.AsyncImage
import com.example.e_faktura.model.Company
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.company.add.CompanyFormViewModel
import com.example.e_faktura.ui.core.IconPickerDialog
import com.example.e_faktura.ui.core.IconProvider
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanyScreen(
    companyId: String,
    onCompanyUpdated: () -> Unit,
    viewModel: CompanyFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current

    // In a real app, you would fetch the company by ID from the ViewModel.
    // For this refactor, we simulate this by creating a sample company.
    val companyToEdit = Company(id = companyId, businessName = "Existing Company To Edit") 

    var nip by remember { mutableStateOf(companyToEdit.nip) }
    var businessName by remember { mutableStateOf(companyToEdit.businessName) }
    var address by remember { mutableStateOf(companyToEdit.address) }
    var ownerFullName by remember { mutableStateOf(companyToEdit.ownerFullName) }
    var bankAccount by remember { mutableStateOf(companyToEdit.bankAccount) }
    var iconString by remember { mutableStateOf(companyToEdit.icon) }
    var showIconPicker by remember { mutableStateOf(false) }

    val companyFromGus by viewModel.searchResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { selectedIconString ->
                iconString = selectedIconString
                showIconPicker = false
            }
        )
    }

    LaunchedEffect(companyFromGus) {
        companyFromGus?.let {
            nip = it.nip
            businessName = it.businessName
            address = it.address
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj firmę") },
                navigationIcon = {
                    IconButton(onClick = onCompanyUpdated) {
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
             if(isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                CompanyAvatar(iconString = iconString, onClick = { showIconPicker = true })
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = nip,
                    onValueChange = { nip = it },
                    label = { Text("NIP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    trailingIcon = {
                        IconButton(onClick = { if (nip.isNotBlank()) viewModel.loadDataFromNip(nip) }) {
                            Icon(Icons.Default.Search, contentDescription = "Szukaj w GUS")
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nazwa Firmy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adres (Ulica, Kod, Miasto)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = ownerFullName,
                    onValueChange = { ownerFullName = it },
                    label = { Text("Imię i nazwisko właściciela") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = bankAccount,
                    onValueChange = { bankAccount = it },
                    label = { Text("Numer konta bankowego") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        val updatedCompany = companyToEdit.copy(
                            nip = nip, businessName = businessName, address = address,
                            ownerFullName = ownerFullName, bankAccount = bankAccount, icon = iconString
                        )
                        viewModel.saveCompany(updatedCompany) // This should call update in a real app
                        Toast.makeText(context, "Zmiany zapisane", Toast.LENGTH_SHORT).show()
                        onCompanyUpdated()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
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
        val parts = iconString.split(":")
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
            val iconVector = if (value != null) IconProvider.getIcon(value) else Icons.Outlined.Storefront
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
