package com.anil.sansiri.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anil.sansiri.data.model.User
import com.anil.sansiri.data.model.UserRole
import com.anil.sansiri.ui.theme.SanSiriTheme

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.currentUser) {
        uiState.currentUser?.let { user ->
            onLoginSuccess(user)
        }
    }

    LoginContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onPhoneNumberChanged = viewModel::onPhoneNumberChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onOtpCodeChanged = viewModel::onOtpCodeChanged,
        onSendOtpClicked = {
            activity?.let { viewModel.sendOtp(it) }
        },
        onVerifyOtpClicked = viewModel::verifyOtp,
        onLoginClicked = viewModel::login,
        onToggleMode = viewModel::toggleMode,
        onResetOtpState = viewModel::resetState,
        onLogout = viewModel::logout,
        modifier = modifier
    )
}

@Composable
fun LoginContent(
    uiState: AuthUiState,
    onNameChanged: (String) -> Unit,
    onPhoneNumberChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onOtpCodeChanged: (String) -> Unit,
    onSendOtpClicked: () -> Unit,
    onVerifyOtpClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onToggleMode: () -> Unit,
    onResetOtpState: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScatteredIconsBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Branding Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = "San Siri Logo",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "San Siri Residency",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "Society & Maintenance Management Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Elevated Login / Signup Card (8.dp elevation with 24.dp rounded corners)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { if (!uiState.isLoginMode) onToggleMode() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Login",
                                fontWeight = if (uiState.isLoginMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.isLoginMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { if (uiState.isLoginMode) onToggleMode() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Sign Up",
                                fontWeight = if (!uiState.isLoginMode) FontWeight.Bold else FontWeight.Normal,
                                color = if (!uiState.isLoginMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isLoginMode) {
                        // Login Mode
                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = { newValue ->
                                onPhoneNumberChanged(newValue.filter { it.isDigit() }.take(10))
                            },
                            label = { Text("Phone Number") },
                            placeholder = { Text("10-digit number") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null
                                )
                            },
                            supportingText = if (uiState.phoneNumber.isNotEmpty() && uiState.phoneNumber.length != 10) {
                                { Text("Phone number must be exactly 10 digits", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            isError = uiState.phoneNumber.isNotEmpty() && uiState.phoneNumber.length != 10,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = onPasswordChanged,
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onLoginClicked,
                            enabled = !uiState.isLoading && uiState.phoneNumber.length == 10 && uiState.password.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Login", fontSize = 16.sp)
                            }
                        }
                    } else {
                        // Signup Mode
                        if (!uiState.isOtpSent) {
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = onNameChanged,
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = uiState.phoneNumber,
                                onValueChange = { newValue ->
                                    onPhoneNumberChanged(newValue.filter { it.isDigit() }.take(10))
                                },
                                label = { Text("Phone Number") },
                                placeholder = { Text("10-digit number") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null
                                    )
                                },
                                supportingText = if (uiState.phoneNumber.isNotEmpty() && uiState.phoneNumber.length != 10) {
                                    { Text("Phone number must be exactly 10 digits", color = MaterialTheme.colorScheme.error) }
                                } else null,
                                isError = uiState.phoneNumber.isNotEmpty() && uiState.phoneNumber.length != 10,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = onPasswordChanged,
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = onConfirmPasswordChanged,
                                label = { Text("Confirm Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onSendOtpClicked,
                                enabled = !uiState.isLoading && uiState.phoneNumber.length == 10 && uiState.password.isNotBlank() && uiState.name.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Sign Up & Verify OTP", fontSize = 16.sp)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = uiState.otpCode,
                                onValueChange = onOtpCodeChanged,
                                label = { Text("6-Digit OTP Code") },
                                supportingText = { Text("Demo OTP Code: 123456 or Anil@102") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onVerifyOtpClicked,
                                enabled = !uiState.isLoading && uiState.otpCode.length >= 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify & Complete Setup", fontSize = 16.sp)
                                }
                            }

                            TextButton(
                                onClick = onResetOtpState,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Back")
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (uiState.currentUser != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Currently Logged In",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${uiState.currentUser.name} (${uiState.currentUser.role})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        TextButton(onClick = onLogout) {
                            Text("Logout")
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ScatteredIconsBackground(modifier: Modifier = Modifier) {
    val icons = remember {
        listOf(
            Icons.Default.Home,
            Icons.Default.Build,
            Icons.Default.Park,
            Icons.Default.Security,
            Icons.Default.AccountBalance,
            Icons.Default.Key,
            Icons.Default.FlashOn,
            Icons.Default.WaterDrop,
            Icons.Default.Apartment
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val columns = 5
        val rows = 9
        val cellWidth = maxWidth / columns
        val cellHeight = maxHeight / rows

        for (r in 0 until rows) {
            for (c in 0 until columns) {
                val index = (r * columns + c) % icons.size
                val icon = icons[index]
                val rotation = ((r * 17 + c * 23) % 51 - 25).toFloat()
                val alpha = 0.08f + ((r * 7 + c * 13) % 5) * 0.01f
                val offsetX = ((r * 11 + c * 7) % 15 - 7).dp
                val offsetY = ((r * 13 + c * 19) % 15 - 7).dp

                Box(
                    modifier = Modifier
                        .size(cellWidth, cellHeight)
                        .offset(
                            x = cellWidth * c + offsetX,
                            y = cellHeight * r + offsetY
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(34.dp)
                            .rotate(rotation)
                            .alpha(alpha),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SanSiriTheme {
        Surface {
            LoginContent(
                uiState = AuthUiState(
                    phoneNumber = "9876543210",
                    isOtpSent = false,
                    isLoginMode = true
                ),
                onNameChanged = {},
                onPhoneNumberChanged = {},
                onPasswordChanged = {},
                onConfirmPasswordChanged = {},
                onOtpCodeChanged = {},
                onSendOtpClicked = {},
                onVerifyOtpClicked = {},
                onLoginClicked = {},
                onToggleMode = {},
                onResetOtpState = {},
                onLogout = {}
            )
        }
    }
}
