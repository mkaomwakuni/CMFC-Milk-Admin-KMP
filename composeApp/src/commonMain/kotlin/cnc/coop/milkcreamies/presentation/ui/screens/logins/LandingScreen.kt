/*
 * Copyright 2025  MkaoCodes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cnc.coop.milkcreamies.presentation.ui.screens.logins

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.presentation.viewmodel.auth.AuthViewModel
import org.koin.compose.koinInject

// Color scheme
val GreenPrimary = Color(0xFF059669)
val GreenSecondary = Color(0xFF047857)
val GreenLight = Color(0xFFECFDF5)
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray300 = Color(0xFFD1D5DB)
val Gray400 = Color(0xFF9CA3AF)
val Gray500 = Color(0xFF6B7280)
val Gray600 = Color(0xFF4B5563)
val Gray700 = Color(0xFF374151)
val Gray900 = Color(0xFF111827)

data class FormData(
    // Fields for UserRegistration (signup)
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val confirmPassword: String = ""
)

enum class AuthTab(val label: String, val icon: ImageVector) {
    LOGIN("Login", Icons.Default.Person),
    SIGNUP("Sign Up", Icons.Default.Email)
}

@Composable
fun LandingScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = koinInject()
) {
    var activeTab by remember { mutableStateOf(AuthTab.LOGIN) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var formData by remember { mutableStateOf(FormData()) }
    var rememberMe by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Handle successful login
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    // Clear messages after some time
    LaunchedEffect(authState.errorMessage) {
        if (authState.errorMessage != null) {
            kotlinx.coroutines.delay(5000)
            authViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(authState.successMessage) {
        if (authState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            authViewModel.clearSuccessMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Spacer to center content when not scrolled
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .width(400.dp)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                AuthHeader(activeTab = activeTab)

                Spacer(modifier = Modifier.height(32.dp))

                // Error/Success Messages
                authState.errorMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                color = Color(0xFFDC2626),
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                authState.successMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                color = Color(0xFF059669),
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Auth Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Tab Navigation
                        TabNavigation(
                            activeTab = activeTab,
                            onTabChange = { activeTab = it }
                        )

                        // Form Content
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Username field (required for both login and signup)
                            InputField(
                                label = if (activeTab == AuthTab.LOGIN) "Username" else "Username",
                                value = formData.username,
                                onValueChange = { formData = formData.copy(username = it) },
                                placeholder = "Enter your username",
                                leadingIcon = Icons.Default.Person
                            )

                            // Password field (required for both login and signup)
                            PasswordField(
                                label = "Password",
                                value = formData.password,
                                onValueChange = { formData = formData.copy(password = it) },
                                placeholder = "Enter your password",
                                showPassword = showPassword,
                                onTogglePassword = { showPassword = !showPassword }
                            )

                            // Signup specific fields
                            if (activeTab == AuthTab.SIGNUP) {
                                // Email (required for signup)
                                InputField(
                                    label = "Email Address",
                                    value = formData.email,
                                    onValueChange = { formData = formData.copy(email = it) },
                                    placeholder = "Enter your email",
                                    leadingIcon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email
                                )

                                // First Name (required for signup)
                                InputField(
                                    label = "First Name",
                                    value = formData.firstName,
                                    onValueChange = { formData = formData.copy(firstName = it) },
                                    placeholder = "Enter your first name",
                                    leadingIcon = Icons.Default.Person
                                )

                                // Last Name (required for signup)
                                InputField(
                                    label = "Last Name",
                                    value = formData.lastName,
                                    onValueChange = { formData = formData.copy(lastName = it) },
                                    placeholder = "Enter your last name",
                                    leadingIcon = Icons.Default.Person
                                )

                                // Confirm Password (signup only)
                                PasswordField(
                                    label = "Confirm Password",
                                    value = formData.confirmPassword,
                                    onValueChange = { formData = formData.copy(confirmPassword = it) },
                                    placeholder = "Confirm your password",
                                    showPassword = showConfirmPassword,
                                    onTogglePassword = { showConfirmPassword = !showConfirmPassword }
                                )
                            }

                            // Remember Me / Forgot Password (Login only)
                            if (activeTab == AuthTab.LOGIN) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = rememberMe,
                                            onCheckedChange = { rememberMe = it },
                                            colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Remember me",
                                            fontSize = 14.sp,
                                            color = Gray700
                                        )
                                    }

                                    Text(
                                        text = "Forgot password?",
                                        fontSize = 14.sp,
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable { /* Handle forgot password */ }
                                    )
                                }
                            }

                            // Terms and Conditions (Signup only)
                            if (activeTab == AuthTab.SIGNUP) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = acceptTerms,
                                        onCheckedChange = { acceptTerms = it },
                                        colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "I agree to the ",
                                            fontSize = 14.sp,
                                            color = Gray700
                                        )
                                        Row {
                                            Text(
                                                text = "Terms and Conditions",
                                                fontSize = 14.sp,
                                                color = GreenPrimary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.clickable { /* Handle terms */ }
                                            )
                                            Text(
                                                text = " and ",
                                                fontSize = 14.sp,
                                                color = Gray700
                                            )
                                            Text(
                                                text = "Privacy Policy",
                                                fontSize = 14.sp,
                                                color = GreenPrimary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.clickable { /* Handle privacy */ }
                                            )
                                        }
                                    }
                                }
                            }

                            // Submit Button
                            Button(
                                onClick = {
                                    when (activeTab) {
                                        AuthTab.LOGIN -> {
                                            // UserLogin requires: username, password
                                            authViewModel.login(formData.username, formData.password)
                                        }
                                        AuthTab.SIGNUP -> {
                                            // Validate required fields for UserRegistration
                                            if (formData.password != formData.confirmPassword) {
                                                // Handle password mismatch - could set error in viewmodel
                                                return@Button
                                            }
                                            if (!acceptTerms) {
                                                // Handle terms not accepted
                                                return@Button
                                            }
                                            // UserRegistration requires: username, email, password, firstName, lastName
                                            authViewModel.signup(
                                                username = formData.username,
                                                email = formData.email,
                                                password = formData.password,
                                                firstName = formData.firstName,
                                                lastName = formData.lastName
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(4.dp),
                                enabled = !authState.isLoading
                            ) {
                                if (authState.isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        text = when (activeTab) {
                                            AuthTab.LOGIN -> "Sign In"
                                            AuthTab.SIGNUP -> "Create Account"
                                        },
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Alternative Sign In Options (Login only)
                            if (activeTab == AuthTab.LOGIN) {
                                AlternativeSignInOptions()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Footer Text
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (activeTab) {
                            AuthTab.LOGIN -> "Don't have an account? "
                            AuthTab.SIGNUP -> "Already have an account? "
                        },
                        fontSize = 14.sp,
                        color = Gray600
                    )
                    Text(
                        text = when (activeTab) {
                            AuthTab.LOGIN -> "Sign up here"
                            AuthTab.SIGNUP -> "Sign in here"
                        },
                        fontSize = 14.sp,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            activeTab = when (activeTab) {
                                AuthTab.LOGIN -> AuthTab.SIGNUP
                                AuthTab.SIGNUP -> AuthTab.LOGIN
                            }
                        }
                    )
                }
            }

            // Bottom spacer to ensure content can scroll above bottom
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun AuthHeader(activeTab: AuthTab) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(GreenPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (activeTab == AuthTab.LOGIN) "Sign in to your account" else "Create your account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (activeTab == AuthTab.LOGIN)
                "Welcome back! Please enter your details."
            else
                "Join us today! Please fill in your information.",
            fontSize = 14.sp,
            color = Gray600,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TabNavigation(
    activeTab: AuthTab,
    onTabChange: (AuthTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
            )
    ) {
        listOf(AuthTab.LOGIN, AuthTab.SIGNUP).forEach { tab ->
            val isActive = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabChange(tab) }
                    .background(
                        if (isActive) GreenLight else Color.Transparent
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isActive) Color(0xFF047857) else Gray500,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = tab.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) Color(0xFF047857) else Gray500
                    )
                }
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Gray700
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Gray400) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(16.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Gray300
            ),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showPassword: Boolean,
    onTogglePassword: () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Gray700
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Gray400) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(16.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = Gray300
            ),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun AlternativeSignInOptions() {
    Column {
        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300)
            Text(
                text = "Or continue with",
                fontSize = 14.sp,
                color = Gray500,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Gray300)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Google button
            OutlinedButton(
                onClick = { /* Handle Google sign in */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Gray500
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray300),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Google", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            // Facebook button
            OutlinedButton(
                onClick = { /* Handle Facebook sign in */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Gray500
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray300),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Facebook", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
