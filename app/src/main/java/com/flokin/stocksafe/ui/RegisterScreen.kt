package com.flokin.stocksafe.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flokin.stocksafe.ui.theme.Teal40
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.viewmodel.AuthViewModel
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var showPassword    by remember { mutableStateOf(false) }
    var showPasswordConfirm by remember { mutableStateOf(false) }

    var nameError            by remember { mutableStateOf<String?>(null) }
    var emailError           by remember { mutableStateOf<String?>(null) }
    var passwordError        by remember { mutableStateOf<String?>(null) }
    var passwordConfirmError by remember { mutableStateOf<String?>(null) }

    val registerState     by authViewModel.registerState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is Result.Success -> {
                snackbarHostState.showSnackbar(
                    "Registrasi berhasil! Silakan login."
                )
                authViewModel.resetRegisterState()
                onRegisterSuccess()
            }
            is Result.Error -> {
                snackbarHostState.showSnackbar(state.message)
                authViewModel.resetRegisterState()
            }
            else -> {}
        }
    }

    fun validateAll(): Boolean {
        nameError = when {
            name.isBlank()         -> "Nama wajib diisi"
            name.trim().length < 3 -> "Nama minimal 3 karakter"
            else                   -> null
        }
        emailError = when {
            email.isBlank() -> "Email wajib diisi"
            !RegisterPatterns.EMAIL_ADDRESS.matcher(email).matches()
                            -> "Format email tidak valid"
            else            -> null
        }
        passwordError = when {
            password.isBlank()    -> "Password wajib diisi"
            password.length < 6   -> "Password minimal 6 karakter"
            else                  -> null
        }
        passwordConfirmError = when {
            passwordConfirm.isBlank()       -> "Konfirmasi password wajib diisi"
            passwordConfirm != password     -> "Password tidak cocok"
            else                            -> null
        }
        return listOf(nameError, emailError, passwordError, passwordConfirmError)
            .all { it == null }
    }

    val isLoading = registerState is Result.Loading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                            )
                        )
                    )
                    .zIndex(0f)
            )

            Scaffold(
                modifier = Modifier.zIndex(1f),
                topBar = {
                    TopAppBar(
                        title          = { Text("Daftar Akun") },
                        navigationIcon = {
                            IconButton(onClick = onBackToLogin) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali ke Login"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor             = MaterialTheme.colorScheme.primary,
                            titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                snackbarHost = {
                    SnackbarHost(snackbarHostState) {
                        Snackbar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(it.visuals.message)
                        }
                    }
                }
            ) { paddingValues ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = "Daftar",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            Text(
                                text  = "Buat Akun Baru",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                            )
                            Text(
                                text  = "Isi data di bawah untuk mendaftar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            RegisterField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    nameError = when {
                                        it.isBlank()       -> "Nama wajib diisi"
                                        it.trim().length < 3 -> "Nama minimal 3 karakter"
                                        else               -> null
                                    }
                                },
                                label = "Nama Lengkap",
                                error = nameError
                            )

                            RegisterField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = when {
                                        it.isBlank() -> "Email wajib diisi"
                                        !RegisterPatterns.EMAIL_ADDRESS.matcher(it).matches()
                                                     -> "Format email tidak valid"
                                        else         -> null
                                    }
                                },
                                label = "Email",
                                error = emailError,
                                keyboardType = KeyboardType.Email
                            )

                            RegisterField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = when {
                                        it.isBlank()  -> "Password wajib diisi"
                                        it.length < 6 -> "Password minimal 6 karakter"
                                        else          -> null
                                    }
                                    if (passwordConfirm.isNotBlank()) {
                                        passwordConfirmError = if (passwordConfirm != it)
                                            "Password tidak cocok" else null
                                    }
                                },
                                label = "Password",
                                error = passwordError,
                                isPassword = true,
                                showPassword = showPassword,
                                onTogglePassword = { showPassword = !showPassword }
                            )

                            RegisterField(
                                value = passwordConfirm,
                                onValueChange = {
                                    passwordConfirm = it
                                    passwordConfirmError = when {
                                        it.isBlank()    -> "Konfirmasi password wajib diisi"
                                        it != password  -> "Password tidak cocok"
                                        else            -> null
                                    }
                                },
                                label = "Konfirmasi Password",
                                error = passwordConfirmError,
                                isPassword = true,
                                showPassword = showPasswordConfirm,
                                onTogglePassword = { showPasswordConfirm = !showPasswordConfirm }
                            )

                            val isFormValid = nameError == null &&
                                emailError == null &&
                                passwordError == null &&
                                passwordConfirmError == null &&
                                name.isNotBlank() &&
                                email.isNotBlank() &&
                                password.isNotBlank() &&
                                passwordConfirm.isNotBlank()

                            Button(
                                onClick  = {
                                    if (validateAll()) {
                                        authViewModel.register(
                                            name            = name.trim(),
                                            email           = email.trim(),
                                            password        = password,
                                            passwordConfirm = passwordConfirm
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled  = !isLoading && isFormValid,
                                shape    = RoundedCornerShape(16.dp),
                                colors   = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor   = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color    = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(
                                        text = "Daftar Sekarang",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text  = "Sudah punya akun?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = onBackToLogin) {
                                    Text("Masuk di sini")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        isError       = error != null,
        supportingText = {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            } else if (isPassword.not() && value.isNotBlank() && error == null) {
                Text(
                    "✓",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine      = true,
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(14.dp),
        visualTransformation = if (isPassword && !showPassword)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector        = if (showPassword)
                            Icons.Filled.LockOpen else Icons.Filled.Lock,
                        contentDescription = if (showPassword)
                            "Sembunyikan" else "Tampilkan"
                    )
                }
            }
        } else null
    )
}

private object RegisterPatterns {
    val EMAIL_ADDRESS: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )
}
