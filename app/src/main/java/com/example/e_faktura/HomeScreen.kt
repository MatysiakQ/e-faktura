package com.example.e_faktura

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    invoiceViewModel: InvoiceViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val companies by invoiceViewModel.companies.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val isUserLoggedIn = authViewModel.isUserLoggedIn()
    val context = LocalContext.current
    var companyToDelete by remember { mutableStateOf<Company?>(null) }

    if (companyToDelete != null) {
        DeleteConfirmationDialog(
            companyName = companyToDelete!!.displayName,
            onConfirm = {
                invoiceViewModel.deleteCompany(companyToDelete!!)
                companyToDelete = null
            },
            onDismiss = { companyToDelete = null }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo aplikacji",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("E-Faktura")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (isUserLoggedIn) {
                            DropdownMenuItem(text = { Text("Moje konto") }, onClick = { navController.navigate("my_account") })
                            DropdownMenuItem(text = { Text("Ustawienia") }, onClick = { navController.navigate("settings") })
                            DropdownMenuItem(text = { Text("Wyloguj") }, onClick = {
                                authViewModel.signOut()
                                Toast.makeText(context, "Wylogowano pomyślnie", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            })
                        } else {
                            DropdownMenuItem(text = { Text("Zaloguj") }, onClick = { navController.navigate("login") })
                            DropdownMenuItem(text = { Text("Ustawienia") }, onClick = { navController.navigate("settings") })
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(onClick = { navController.navigate("add_company") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Dodaj firmę")
                }
                FloatingActionButton(onClick = { navController.navigate("qr_code_scanner") }) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = "Skanuj kod QR")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Wyszukaj firmę...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val filteredCompanies = companies.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                        it.nip.contains(searchQuery, ignoreCase = true)
            }

            if (filteredCompanies.isEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) "Brak wyników wyszukiwania" else "Brak zapisanych podmiotów.\nKliknij \"+\", aby dodać nowy.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredCompanies, key = { it.nip }) { company ->
                        CompanyItem(
                            company = company,
                            onClick = {
                                navController.navigate("company_details/${company.nip}")
                            },
                            onDelete = { companyToDelete = company }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(companyName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Potwierdź usunięcie") },
        text = { Text("Czy na pewno chcesz usunąć firmę \"$companyName\" ze swojej listy?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Usuń")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.CompanyItem(company: Company, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (company.icon.type) {
                IconType.PREDEFINED -> {
                    Icon(
                        imageVector = IconProvider.getIcon(company.icon.iconName),
                        contentDescription = "Ikona firmy",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconType.CUSTOM -> {
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(company.icon.iconName)),
                        contentDescription = "Ikona firmy",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = company.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "NIP: ${company.nip}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Usuń firmę", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}