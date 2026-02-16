@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.e_faktura.ui.ksef
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.e_faktura.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KsefSetupScreen(
    navController: NavController,
    viewModel: KsefSetupViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    var tokenVisible by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Pokaż Snackbar przy błędzie / sukcesie
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            viewModel.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            viewModel.clearMessages()
        }
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Rozłącz z KSeF?") },
            text = { Text("Sesja zostanie zakończona. Dane logowania (NIP, token) pozostaną zapisane.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.disconnect(); showDisconnectDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Rozłącz") }
            },
            dismissButton = { TextButton(onClick = { showDisconnectDialog = false }) { Text("Anuluj") } }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Usuń dane KSeF?") },
            text = { Text("Zostaną usunięte: NIP, token KSeF i token sesji. Tej operacji nie można cofnąć.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllData(); showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Anuluj") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Połączenie z KSeF") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ─── Karta statusu połączenia ──────────────────────────────────
            StatusCard(isConnected = state.isConnected, nip = state.nip, companyName = state.companyName)

            // ─── Środowisko (test/prod) ────────────────────────────────────
            EnvironmentSelector(
                isProduction = state.isProduction,
                onEnvironmentChange = { viewModel.setEnvironment(it) },
                enabled = !state.isLoading
            )

            HorizontalDivider()

            // ─── Formularz danych ──────────────────────────────────────────
            Text(
                "Dane autoryzacji",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Pole nazwy firmy (opcjonalne)
            OutlinedTextField(
                value = state.companyName,
                onValueChange = { viewModel.updateCompanyName(it) },
                label = { Text("Nazwa firmy (opcjonalnie)") },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !state.isLoading
            )

            // Pole NIP
            OutlinedTextField(
                value = state.nip,
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) viewModel.updateNip(it) },
                label = { Text("NIP firmy") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                placeholder = { Text("10 cyfr bez kresek") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !state.isLoading
            )

            // Pole Token KSeF
            OutlinedTextField(
                value = state.ksefToken,
                onValueChange = { viewModel.updateKsefToken(it) },
                label = { Text("Token KSeF") },
                leadingIcon = { Icon(Icons.Default.Key, null) },
                placeholder = { Text("Token z portalu podatki.gov.pl") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { tokenVisible = !tokenVisible }) {
                        Icon(
                            if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                singleLine = true,
                enabled = !state.isLoading
            )

            // Informacja jak uzyskać token
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Token KSeF wygeneruj na stronie:\nksef-test.mf.gov.pl (testowe)\nlub podatki.gov.pl (produkcja)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ─── Przyciski akcji ───────────────────────────────────────────

            // Test połączenia
            OutlinedButton(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.NetworkCheck, null)
                Spacer(Modifier.width(8.dp))
                Text("TESTUJ POŁĄCZENIE Z API")
            }

            // Autoryzuj
            Button(
                onClick = { viewModel.authorize() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Link, null)
                    Spacer(Modifier.width(8.dp))
                    Text("POŁĄCZ Z KSEF", fontWeight = FontWeight.Bold)
                }
            }

            // Rozłącz (tylko gdy połączono)
            AnimatedVisibility(visible = state.isConnected) {
                OutlinedButton(
                    onClick = { showDisconnectDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.LinkOff, null)
                    Spacer(Modifier.width(8.dp))
                    Text("ROZŁĄCZ SESJĘ")
                }
            }

            // Usuń dane
            TextButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text(
                    "Usuń wszystkie dane KSeF",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusCard(isConnected: Boolean, nip: String, companyName: String) {
    val containerColor = if (isConnected)
        Color(0xFF4CAF50).copy(alpha = 0.12f)
    else
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)

    val contentColor = if (isConnected)
        Color(0xFF2E7D32)
    else
        MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = if (isConnected) "Połączono z KSeF" else "Brak połączenia z KSeF",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                if (isConnected && nip.isNotBlank()) {
                    Text(
                        text = "${companyName.ifBlank { "Firma" }} · NIP: $nip",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                } else if (!isConnected) {
                    Text(
                        text = "Wprowadź dane aby połączyć się z systemem KSeF",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentSelector(
    isProduction: Boolean,
    onEnvironmentChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Środowisko",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !isProduction,
                onClick = { onEnvironmentChange(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                enabled = enabled
            ) {
                Text("Testowe")
            }
            SegmentedButton(
                selected = isProduction,
                onClick = { onEnvironmentChange(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                enabled = enabled,
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = if (isProduction)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text("Produkcyjne")
            }
        }
        if (isProduction) {
            Text(
                "⚠️ Tryb produkcyjny — faktury trafiają do systemu MF",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
