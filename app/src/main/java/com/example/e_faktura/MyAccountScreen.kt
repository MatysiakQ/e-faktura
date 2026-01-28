package com.example.e_faktura.ui.account

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.e_faktura.ui.AppViewModelProvider
import com.example.e_faktura.ui.auth.AuthUiState
import com.example.e_faktura.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val user by authViewModel.user.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) } // ✅ NOWE

    LaunchedEffect(uiState.passwordChangeSuccess, uiState.profileUpdateSuccess, uiState.emailChangeSuccess, uiState.error) {
        if (uiState.passwordChangeSuccess) {
            Toast.makeText(context, "Hasło zostało zmienione", Toast.LENGTH_SHORT).show()
            showChangePasswordDialog = false
            authViewModel.resetAuthState()
        }
        if (uiState.emailChangeSuccess) { //Obsługa sukcesu email
            Toast.makeText(context, "Email został zaktualizowany", Toast.LENGTH_SHORT).show()
            showChangeEmailDialog = false
            authViewModel.resetAuthState()
        }
        if (uiState.profileUpdateSuccess) {
            Toast.makeText(context, "Zdjęcie profilowe zaktualizowane", Toast.LENGTH_SHORT).show()
            authViewModel.resetAuthState()
        }
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.resetAuthState()
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            uiState = uiState,
            onConfirm = { newPassword -> authViewModel.changePassword(newPassword) }
        )
    }

    // Dialog zmiany email
    if (showChangeEmailDialog) {
        ChangeEmailDialog(
            currentEmail = user?.email ?: "",
            onDismiss = { showChangeEmailDialog = false },
            uiState = uiState,
            onConfirm = { newEmail -> authViewModel.changeEmail(newEmail) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Moje Konto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            ProfileHeroSection(user?.photoUrl, user?.displayName) { uri -> authViewModel.updateProfilePicture(uri) }
            Spacer(Modifier.height(40.dp))

            SecuritySection(
                email = user?.email ?: "Brak adresu",
                onChangeEmailClick = { showChangeEmailDialog = true },
                onChangePasswordClick = { showChangePasswordDialog = true }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { authViewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(12.dp))
                Text("WYLOGUJ SIĘ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileHeroSection(photoUrl: Uri?, login: String?, onImageSelected: (Uri) -> Unit) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let(onImageSelected) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = photoUrl ?: "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                contentDescription = "Avatar",
                placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
                modifier = Modifier.size(130.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentScale = ContentScale.Crop
            )
            SmallFloatingActionButton(onClick = { imagePicker.launch("image/*") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(text = login ?: "Gość", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SecuritySection(
    email: String,
    onChangeEmailClick: () -> Unit,
    onChangePasswordClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Zabezpieczenia", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Adres e-mail", style = MaterialTheme.typography.labelSmall)
                    Text(email, style = MaterialTheme.typography.bodyLarge)
                }
                Button(onClick = onChangeEmailClick, shape = RoundedCornerShape(10.dp)) {
                    Text("ZMIEŃ")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // WIERSZ HASŁA
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(16.dp))
                Text("Hasło dostępowe", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onChangePasswordClick, shape = RoundedCornerShape(10.dp)) {
                    Text("ZMIEŃ")
                }
            }
        }
    }
}

@Composable
fun ChangeEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    uiState: AuthUiState,
    onConfirm: (String) -> Unit
) {
    var newEmail by remember { mutableStateOf(currentEmail) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmień adres e-mail") },
        text = {
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("Nowy e-mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newEmail) },
                enabled = !uiState.isLoading && newEmail.isNotBlank()
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Zapisz")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, uiState: AuthUiState, onConfirm: (String) -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmień hasło") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(newPassword, { newPassword = it }, label = { Text("Nowe hasło") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation())
                OutlinedTextField(confirmPassword, { confirmPassword = it }, label = { Text("Powtórz hasło") }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newPassword.length < 6) Toast.makeText(context, "Min. 6 znaków", Toast.LENGTH_SHORT).show()
                else if (newPassword != confirmPassword) Toast.makeText(context, "Hasła się różnią", Toast.LENGTH_SHORT).show()
                else onConfirm(newPassword)
            }, enabled = !uiState.isLoading) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Zapisz")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}