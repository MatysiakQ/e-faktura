package com.example.e_faktura

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddCompanyScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel,
    companyViewModel: CompanyViewModel = viewModel()
) {
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
        IconPickerDialog(companyViewModel) { showIconPicker = false }
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
                    IconSelection(companyViewModel)
                    Text("Kliknij, aby zmienić ikonę", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            CompanyTypeSelection(companyViewModel)
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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
                    val newCompany = companyViewModel.saveCompany()
                    invoiceViewModel.addCompany(newCompany)
                    navController.popBackStack()
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
private fun IconSelection(companyViewModel: CompanyViewModel) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        when (companyViewModel.icon.type) {
            IconType.PREDEFINED -> {
                Icon(
                    imageVector = IconProvider.getIcon(companyViewModel.icon.iconName),
                    contentDescription = "Ikona firmy",
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconType.CUSTOM -> {
                Image(
                    painter = rememberAsyncImagePainter(model = Uri.parse(companyViewModel.icon.iconName)),
                    contentDescription = "Ikona firmy",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun IconPickerDialog(companyViewModel: CompanyViewModel, onDismiss: () -> Unit) {
    val galleryPermissionState = rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                companyViewModel.onIconChange(CompanyIcon(IconType.CUSTOM, uri.toString()))
                onDismiss()
            }
        }
    )
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Wybierz ikonę", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(IconProvider.icons.keys.toList()) { iconName ->
                        Icon(
                            imageVector = IconProvider.getIcon(iconName),
                            contentDescription = iconName,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    companyViewModel.onIconChange(CompanyIcon(IconType.PREDEFINED, iconName))
                                    onDismiss()
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { 
                        if (galleryPermissionState.status.isGranted) {
                            galleryLauncher.launch("image/*") 
                        } else {
                            galleryPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Wybierz z galerii")
                }
            }
        }
    }
}
