package com.example.e_faktura.ui.company.add

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.e_faktura.ui.components.IconPickerDialog
import com.example.e_faktura.ui.components.IconProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompanyScreen(
    navController: NavController,
    viewModel: CompanyFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UiEvent.SaveSuccess -> {
                    Toast.makeText(context, "Zapisano firmę!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is UiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var showIconPicker by remember { mutableStateOf(false) }

    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { selectedIcon ->
                viewModel.updateIcon("${selectedIcon.type}:${selectedIcon.value}")
                showIconPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj firmę") },
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
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().zIndex(1f))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                CompanyAvatar(iconString = state.icon, onClick = { showIconPicker = true })

                Spacer(Modifier.height(24.dp))

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

                OutlinedTextField(
                    value = state.businessName,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Nazwa Firmy") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.updateAddress(it) },
                    label = { Text("Adres (Ulica i numer)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                Spacer(Modifier.height(16.dp))

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

                OutlinedTextField(
                    value = state.ownerFullName,
                    onValueChange = { viewModel.updateOwner(it) },
                    label = { Text("Właściciel") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.bankAccount,
                    onValueChange = { viewModel.updateBankAccount(it) },
                    label = { Text("Numer konta") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveCompany() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !state.isLoading
                ) {
                    Text("Zapisz firmę")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CompanyAvatar(iconString: String, onClick: () -> Unit) {
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
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val iconVector = if (value != null) {
                try { IconProvider.getIcon(value) } catch (e: Exception) { Icons.Outlined.Storefront }
            } else Icons.Outlined.Storefront

            Icon(
                imageVector = iconVector,
                contentDescription = "Logo",
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edytuj",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
