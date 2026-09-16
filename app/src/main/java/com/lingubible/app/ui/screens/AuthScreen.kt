package com.lingubible.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.LocalComponentActivity
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.M3LoadingIndicator
import com.lingubible.app.ui.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    initialMode: String = "login",
    onAuthSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val activity = LocalComponentActivity.current ?: remember(context, view) {
        context.findActivity() ?: view.context.findActivity()
    }
    var isLoginMode by remember(initialMode) { mutableStateOf(initialMode == "login") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "嶺南大學課程評價平台",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "LingUBible",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Reg科聖經 • 博訪博覽，慎選課席",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Material 3 Expressive Mode Selector
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    M3ButtonGroup(
                        selectedIndex = if (isLoginMode) 0 else 1,
                        onLeadingClick = {
                            isLoginMode = true
                            viewModel.clearError()
                        },
                        onTrailingClick = {
                            isLoginMode = false
                            viewModel.clearError()
                        },
                        leadingText = "登入 Sign In",
                        trailingText = "註冊 Register",
                        height = 42.dp,
                        activeCornerRadius = 21.dp,
                        inactiveCornerRadius = 10.dp,
                        showCheckmarkOnSelected = true
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        activity?.let { viewModel.loginWithGoogle(it, onAuthSuccess) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isLoading && activity != null,
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("G", fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (uiState.isLoading) "Google 登入中… Signing in…" else "使用 Google 繼續 Continue with Google",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column {
                        AnimatedVisibility(
                            visible = !isLoginMode,
                            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("姓名或暱稱 · Name", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = authFieldColors(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("嶺南大學電郵 · Lingnan Email", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            placeholder = { Text("@ln.hk / @ln.edu.hk", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密碼 · Password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = authFieldColors(),
                            singleLine = true
                        )

                        AnimatedVisibility(
                            visible = !isLoginMode,
                            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("確認密碼 · Confirm Password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                                supportingText = {
                                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                                        Text("Passwords do not match", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = authFieldColors(),
                                singleLine = true
                            )
                            }
                        }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (isLoginMode) {
                            viewModel.login(email, password, onAuthSuccess)
                        } else {
                            viewModel.register(email, password, confirmPassword, name, onAuthSuccess)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        M3LoadingIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (isLoginMode) "登入 Sign In" else "註冊帳號 Create Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary
)

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

