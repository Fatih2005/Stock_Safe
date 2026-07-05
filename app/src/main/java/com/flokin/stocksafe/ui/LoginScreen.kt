package com.flokin.stocksafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.foundation.layout.Row
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flokin.stocksafe.ui.theme.Teal40
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.viewmodel.AuthViewModel
import java.util.regex.Pattern

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var emailError    by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val loginState        by authViewModel.loginState.collectAsState()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Result.Success -> onLoginSuccess()
            is Result.Error   -> {
                snackbarHostState.showSnackbar(state.message)
                authViewModel.resetLoginState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-100).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                        )
                    )
                )
                .zIndex(0f)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 120.dp)
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = "Logo StockSafe",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text  = "StockSafe",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                        )
                        Text(
                            text  = "Manajemen Inventaris Aman",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value         = email,
                            onValueChange = {
                                email      = it
                                emailError = if (it.isNotBlank() &&
                                    !LoginPatterns.EMAIL_ADDRESS.matcher(it).matches()
                                ) "Format email tidak valid" else null
                            },
                            label           = { Text("Email") },
                            isError         = emailError != null,
                            supportingText  = {
                                if (emailError != null) {
                                    Text(emailError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine      = true,
                            modifier        = Modifier.fillMaxWidth(),
                            shape           = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value         = password,
                            onValueChange = {
                                password      = it
                                passwordError = if (it.isNotBlank() && it.length < 6)
                                    "Password minimal 6 karakter" else null
                            },
                            label                = { Text("Password") },
                            isError              = passwordError != null,
                            supportingText       = {
                                if (passwordError != null) {
                                    Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            visualTransformation = if (showPassword)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon         = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector        = if (showPassword)
                                            Icons.Filled.LockOpen
                                        else
                                            Icons.Filled.Lock,
                                        contentDescription = if (showPassword)
                                            "Sembunyikan password"
                                        else
                                            "Tampilkan password"
                                    )
                                }
                            },
                            keyboardOptions      = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            singleLine           = true,
                            modifier             = Modifier.fillMaxWidth(),
                            shape                = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val isLoading   = loginState is Result.Loading
                        val isFormValid = emailError == null &&
                            passwordError == null &&
                            email.isNotBlank() &&
                            password.isNotBlank()

                        Button(
                            onClick  = { authViewModel.login(email.trim(), password) },
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
                                    text = "Masuk",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text  = "Belum punya akun?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onNavigateToRegister) {
                                Text("Daftar sekarang")
                            }
                        }
                    }
                }
            }
        }
    }
}

private object LoginPatterns {
    val EMAIL_ADDRESS: Pattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )
}
