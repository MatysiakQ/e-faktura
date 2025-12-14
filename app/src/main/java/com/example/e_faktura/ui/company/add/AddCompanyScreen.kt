package com.example.e_faktura.ui.company.add

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.example.e_faktura.model.Company
import com.example.e_faktura.model.CompanyIcon
import com.example.e_faktura.model.IconType
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.components.IconPickerDialog
import com.example.e_faktura.ui.components.IconProvider
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompanyScreen(
    onCompanyAdded: () -> Unit,
    viewModel: CompanyFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current

    var nip by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var ownerFullName by remember { mutableStateOf("") }
    var bankAccount by remember { mutableStateOf("") }
    var companyIcon by remember { mutableStateOf(CompanyIcon(IconType.PREDEFINED, "Business")) }
    var showIconPicker by remember { mutableStateOf(false) }

    val companyFromGus by viewModel.searchResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { selectedIcon ->
                companyIcon = selectedIcon
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
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nowa Firma", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onCompanyAdded) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
             Button(
                onClick = {
                    val newCompany = Company(
                        id = UUID.randomUUID().toString(),
                        nip = nip,
                        businessName = businessName,
                        address = address,
                        ownerFullName = ownerFullName,
                        bankAccount = bankAccount,
                        icon = "${companyIcon.type}:${companyIcon.value}"
                    )
                    viewModel.saveCompany(newCompany)
                    Toast.makeText(context, "Firma dodana", Toast.LENGTH_SHORT).show()
                    onCompanyAdded()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("ZAPISZ FIRMĘ", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            VisualIdentitySection(
                companyIcon = companyIcon,
                onIconClick = { showIconPicker = true }
            )

            GusSmartHeader(
                nip = nip,
                onNipChange = { nip = it },
                onSearchClick = { viewModel.loadDataFromNip(nip) },
                isLoading = isLoading
            )

            CompanyDetailsCard(
                businessName = businessName,
                onBusinessNameChange = { businessName = it },
                address = address,
                onAddressChange = { address = it },
                ownerFullName = ownerFullName,
                onOwnerFullNameChange = { ownerFullName = it },
                bankAccount = bankAccount,
                onBankAccountChange = { bankAccount = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GusSmartHeader(nip: String, onNipChange: (String) -> Unit, onSearchClick: () -> Unit, isLoading: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = nip,
            onValueChange = onNipChange,
            label = { Text("NIP Firmy") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            trailingIcon = {
                if(isLoading){
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = onSearchClick) {
                        Text("SZUKAJ")
                    }
                }
            },
            singleLine = true
        )
    }
}

@Composable
private fun VisualIdentitySection(companyIcon: CompanyIcon, onIconClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onIconClick),
            contentAlignment = Alignment.Center
        ) {
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
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        TextButton(onClick = onIconClick) {
            Text("Zmień logo")
        }
    }
}

@Composable
private fun CompanyDetailsCard(
    businessName: String, onBusinessNameChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    ownerFullName: String, onOwnerFullNameChange: (String) -> Unit,
    bankAccount: String, onBankAccountChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            label = { Text("Nazwa Firmy") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Adres") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = ownerFullName,
            onValueChange = onOwnerFullNameChange,
            label = { Text("Właściciel (opcjonalnie)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = bankAccount,
            onValueChange = onBankAccountChange,
            label = { Text("Konto bankowe (opcjonalnie)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )
    }
}
