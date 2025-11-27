package com.example.e_faktura

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompanyScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel,
    companyViewModel: CompanyViewModel = viewModel()
) {
    val context = LocalContext.current

    val isFormValid by remember(companyViewModel.companyType, companyViewModel.businessName, companyViewModel.ownerFullName, companyViewModel.nip) {
        mutableStateOf(
            when (companyViewModel.companyType) {
                CompanyType.FIRM -> companyViewModel.businessName.isNotBlank()
                CompanyType.SOLE_PROPRIETORSHIP -> companyViewModel.ownerFullName.isNotBlank()
            } && companyViewModel.nip.length == 10
        )
    }

    var showIconPicker by remember { mutableStateOf(false) }

    if (showIconPicker) {
        IconPickerDialog(onDismiss = { showIconPicker = false }) { newIcon ->
            companyViewModel.onIconChange(newIcon)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowa firma") },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.clickable { showIconPicker = true }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconSelection(companyViewModel.icon)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kliknij, aby zmienić ikonę", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            CompanyTypeSelection(companyViewModel)
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (companyViewModel.companyType == CompanyType.SOLE_PROPRIETORSHIP) {
                        OutlinedTextField(
                            value = companyViewModel.ownerFullName,
                            onValueChange = { companyViewModel.onOwnerFullNameChange(it) },
                            label = { Text("Imię i nazwisko właściciela") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = companyViewModel.businessName,
                            onValueChange = { companyViewModel.onBusinessNameChange(it) },
                            label = { Text("Nazwa firmy") },
                            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = companyViewModel.nip,
                        onValueChange = {
                            if (it.length <= 10) {
                                companyViewModel.onNipChange(it.filter { char -> char.isDigit() })
                            }
                        },
                        label = { Text("NIP") },
                        leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = companyViewModel.nip.isNotBlank() && companyViewModel.nip.length != 10,
                        supportingText = {
                            if (companyViewModel.nip.isNotBlank() && companyViewModel.nip.length != 10) {
                                Text("NIP musi składać się z 10 cyfr.")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = companyViewModel.address,
                        onValueChange = { companyViewModel.onAddressChange(it) },
                        label = { Text("Adres") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                enabled = isFormValid,
                onClick = {
                    try {
                        val newCompany = companyViewModel.saveCompany()
                        invoiceViewModel.addCompany(newCompany)
                        Toast.makeText(context, "Firma dodana!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp)
            ) {
                Text(text = "Zapisz", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompanyTypeSelection(companyViewModel: CompanyViewModel) {
    val options = listOf("Firma", "Działalność jednoosobowa")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = {
                    val type = if (index == 0) CompanyType.FIRM else CompanyType.SOLE_PROPRIETORSHIP
                    companyViewModel.onCompanyTypeChange(type)
                },
                selected = (if (index == 0) CompanyType.FIRM else CompanyType.SOLE_PROPRIETORSHIP) == companyViewModel.companyType
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun IconSelection(icon: CompanyIcon) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when (icon.type) {
            IconType.PREDEFINED -> {
                Icon(
                    imageVector = IconProvider.getIcon(icon.iconName),
                    contentDescription = "Ikona firmy",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconType.CUSTOM -> {
                Image(
                    painter = rememberAsyncImagePainter(model = Uri.parse(icon.iconName)),
                    contentDescription = "Ikona firmy",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }
    }
}