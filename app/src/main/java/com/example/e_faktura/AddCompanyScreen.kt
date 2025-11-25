package com.example.e_faktura

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
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
            } && companyViewModel.nip.isNotBlank()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj nowy podmiot") },
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
            Spacer(modifier = Modifier.height(16.dp))
            CompanyTypeSelection(companyViewModel)
            Spacer(modifier = Modifier.height(24.dp))

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
                onValueChange = { companyViewModel.onNipChange(it) },
                label = { Text("NIP") },
                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = companyViewModel.nip.isBlank()
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