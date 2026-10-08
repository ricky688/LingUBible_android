package com.lingubible.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.settings.ThemeMode
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.AvatarPresets
import com.lingubible.app.ui.components.AvatarCustomizerModal
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.M3ButtonGroupItem
import com.lingubible.app.ui.components.SmartAvatar
import com.lingubible.app.ui.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: AuthViewModel = koinViewModel(),
    settingsManager: AppSettingsManager = koinInject()
) {
    val context = LocalContext.current
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.currentUser
    val customAvatar = uiState.currentAvatar
    val themeMode by settingsManager.themeMode.collectAsState()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW

    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)

    // Avatar customizer dialog state
    var showAvatarModal by remember { mutableStateOf(false) }

    // Username edit state
    var isEditingUsername by remember { mutableStateOf(false) }
    var editUsernameValue by remember(user?.name) { mutableStateOf(user?.name ?: "") }

    // Password edit state
    var isEditingPassword by remember { mutableStateOf(false) }
    var currentPasswordValue by remember { mutableStateOf("") }
    var newPasswordValue by remember { mutableStateOf("") }
    var confirmPasswordValue by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open URL: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Page Title matching web screenshot: "用戶設定"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isZh) "用戶設定" else "User Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (user != null) {
                // ==========================================
                // Card 1: 個人資料 (Personal Profile)
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isZh) "個人資料" else "Personal Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isZh) "更新您的個人資料資訊" else "Update your personal profile information",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // 1.1 頭像 (Avatar Section)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (isZh) "頭像" else "Avatar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = if (isZh) "自定義您的個人資料頭像" else "Customize your personal profile avatar",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showAvatarModal = true },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isZh) "自定義頭像" else "Customize",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Avatar Preview Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    SmartAvatar(
                                        avatar = customAvatar,
                                        userId = user.id,
                                        size = 56.dp,
                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                    )

                                    Column {
                                        Text(
                                            text = if (isZh) "當前頭像" else "Current Avatar",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (customAvatar != null) {
                                                if (isZh) "自定義頭像" else "Custom Avatar"
                                            } else {
                                                if (isZh) "預設頭像" else "Default Avatar"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // 1.2 電子郵件 (Email Section - Readonly)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isZh) "電子郵件" else "Email Address",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }

                            Text(
                                text = if (isZh) "電子郵件地址無法修改" else "Email address cannot be changed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // 1.3 用戶名 (Username Section)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AlternateEmail,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isZh) "用戶名" else "Username",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (!isEditingUsername) {
                                    OutlinedButton(
                                        onClick = {
                                            editUsernameValue = user.name
                                            isEditingUsername = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isZh) "修改用戶名" else "Change",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (!isEditingUsername) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = user.name.ifBlank { if (isZh) "未設定用戶名" else "No username set" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = editUsernameValue,
                                        onValueChange = { editUsernameValue = it },
                                        placeholder = { Text(if (isZh) "輸入新的用戶名" else "Enter new username") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = !uiState.isUpdatingUsername
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { isEditingUsername = false },
                                            enabled = !uiState.isUpdatingUsername
                                        ) {
                                            Text(if (isZh) "取消" else "Cancel")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                viewModel.updateUsername(
                                                    newUsername = editUsernameValue.trim(),
                                                    onSuccess = {
                                                        isEditingUsername = false
                                                        Toast.makeText(context, if (isZh) "用戶名更新成功" else "Username updated successfully", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onError = { err ->
                                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            enabled = !uiState.isUpdatingUsername && editUsernameValue.isNotBlank()
                                        ) {
                                            if (uiState.isUpdatingUsername) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (isZh) "儲存" else "Save")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // 1.4 密碼 (Password Section)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isZh) "密碼" else "Password",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (!isEditingPassword) {
                                    OutlinedButton(
                                        onClick = {
                                            currentPasswordValue = ""
                                            newPasswordValue = ""
                                            confirmPasswordValue = ""
                                            isEditingPassword = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isZh) "修改密碼" else "Change Password",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (isEditingPassword) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = currentPasswordValue,
                                        onValueChange = { currentPasswordValue = it },
                                        placeholder = { Text(if (isZh) "目前密碼" else "Current password") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                    )

                                    OutlinedTextField(
                                        value = newPasswordValue,
                                        onValueChange = { newPasswordValue = it },
                                        placeholder = { Text(if (isZh) "新密碼 (至少8位)" else "New password (min 8 chars)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                    )

                                    OutlinedTextField(
                                        value = confirmPasswordValue,
                                        onValueChange = { confirmPasswordValue = it },
                                        placeholder = { Text(if (isZh) "確認新密碼" else "Confirm new password") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        visualTransformation = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { showPasswords = !showPasswords }
                                        ) {
                                            Checkbox(
                                                checked = showPasswords,
                                                onCheckedChange = { showPasswords = it }
                                            )
                                            Text(if (isZh) "顯示密碼" else "Show password", style = MaterialTheme.typography.bodySmall)
                                        }

                                        Row {
                                            TextButton(
                                                onClick = { isEditingPassword = false },
                                                enabled = !uiState.isUpdatingPassword
                                            ) {
                                                Text(if (isZh) "取消" else "Cancel")
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Button(
                                                onClick = {
                                                    if (newPasswordValue.length < 8) {
                                                        Toast.makeText(context, if (isZh) "新密碼長度至少需8個字元" else "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    if (newPasswordValue != confirmPasswordValue) {
                                                        Toast.makeText(context, if (isZh) "兩次輸入的新密碼不相符" else "Passwords do not match", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }

                                                    viewModel.updatePassword(
                                                        oldPassword = currentPasswordValue,
                                                        newPassword = newPasswordValue,
                                                        onSuccess = {
                                                            isEditingPassword = false
                                                            Toast.makeText(context, if (isZh) "密碼更新成功！" else "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        onError = { err ->
                                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                        }
                                                    )
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                enabled = !uiState.isUpdatingPassword && currentPasswordValue.isNotBlank() && newPasswordValue.isNotBlank()
                                            ) {
                                                if (uiState.isUpdatingPassword) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                } else {
                                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(if (isZh) "儲存密碼" else "Save")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // Card 2: Google 帳戶 (Google Account Link)
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header with Google icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🌐", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = if (isZh) "Google 帳戶" else "Google Account",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isZh) "連結您的 Google 帳戶以便快速登入" else "Link your Google account for faster sign-in",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Link Status Alert Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                1.dp,
                                if (uiState.isGoogleLinked) Color(0xFF22C55E).copy(alpha = 0.4f) else cardBorderColor
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isGoogleLinked) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (uiState.isGoogleLinked) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (uiState.isGoogleLinked) {
                                            if (isZh) "Google 帳戶已連結" else "Google Account Linked"
                                        } else {
                                            if (isZh) "未連結 Google 帳戶" else "Google Account Not Linked"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (uiState.isGoogleLinked) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = if (isZh) "✔ 已連結" else "✔ Linked",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF166534),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = if (uiState.isGoogleLinked) {
                                if (isZh) "您現在可以使用 Google 登入您的帳戶" else "You can now use Google to sign in to your account"
                            } else {
                                if (isZh) "連結後即可使用 Google 帳戶一鍵登入" else "Link to enable 1-click Google login"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Action button (Unlink / Link)
                        if (uiState.isGoogleLinked) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.unlinkGoogle(
                                        onSuccess = {
                                            Toast.makeText(context, if (isZh) "已取消連結 Google 帳戶" else "Google account unlinked", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !uiState.isGoogleLoading
                            ) {
                                if (uiState.isGoogleLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isZh) "取消連結 Google 帳戶" else "Unlink Google Account")
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val activity = context as? ComponentActivity
                                    if (activity != null) {
                                        viewModel.linkGoogle(
                                            activity = activity,
                                            onSuccess = {
                                                Toast.makeText(context, if (isZh) "Google 帳戶連結成功！" else "Google account linked successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !uiState.isGoogleLoading
                            ) {
                                if (uiState.isGoogleLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isZh) "連結 Google 帳戶" else "Link Google Account")
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // Guest User State
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SmartAvatar(
                            avatar = customAvatar,
                            userId = "guest",
                            size = 72.dp,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            onClick = { showAvatarModal = true }
                        )

                        Text(
                            text = if (isZh) "訪客用戶" else "Guest User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedButton(
                            onClick = { showAvatarModal = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isZh) "自定義頭像" else "Customize Avatar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = if (isZh) "使用嶺南大學學生電郵 (@ln.hk) 登入以解鎖所有功能" else "Sign in with your @ln.hk student email to unlock all features",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onNavigateToAuth,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isZh) "使用嶺南郵箱登入 / 註冊" else "Sign In / Register with LN Email",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // Card 3: App Appearance & Settings Preferences
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Theme Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isZh) "外觀主題" else "Theme",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        val themeItems = listOf(
                            M3ButtonGroupItem(if (isZh) "淺色" else "Light", Icons.Default.LightMode) { settingsManager.setThemeMode(ThemeMode.LIGHT) },
                            M3ButtonGroupItem(if (isZh) "深色" else "Dark", Icons.Default.DarkMode) { settingsManager.setThemeMode(ThemeMode.DARK) },
                            M3ButtonGroupItem(if (isZh) "系統" else "Auto", Icons.Default.BrightnessAuto) { settingsManager.setThemeMode(ThemeMode.SYSTEM) }
                        )
                        val selectedThemeIndex = when (themeMode) {
                            ThemeMode.LIGHT -> 0
                            ThemeMode.DARK -> 1
                            ThemeMode.SYSTEM -> 2
                        }

                        M3ButtonGroup(
                            selectedIndex = selectedThemeIndex,
                            items = themeItems,
                            height = 34.dp,
                            spacing = 3.dp,
                            activeCornerRadius = 17.dp,
                            inactiveCornerRadius = 8.dp,
                            showCheckmarkOnSelected = false
                        )
                    }

                    HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Language Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isZh) "顯示語言" else "Language",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        val langItems = listOf(
                            M3ButtonGroupItem(AppLanguage.ZH_TW.label) { settingsManager.setLanguage(AppLanguage.ZH_TW) },
                            M3ButtonGroupItem(AppLanguage.EN.label) { settingsManager.setLanguage(AppLanguage.EN) }
                        )
                        val selectedLangIndex = when (appLanguage) {
                            AppLanguage.ZH_TW -> 0
                            AppLanguage.EN -> 1
                        }

                        M3ButtonGroup(
                            selectedIndex = selectedLangIndex,
                            items = langItems,
                            height = 34.dp,
                            spacing = 3.dp,
                            activeCornerRadius = 17.dp,
                            inactiveCornerRadius = 8.dp,
                            showCheckmarkOnSelected = false
                        )
                    }

                    HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Navigation to full Appearance & Settings Screen
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = {
                            Text(if (isZh) "色彩與外觀自訂" else "Appearance & Customization", fontWeight = FontWeight.SemiBold)
                        },
                        supportingContent = {
                            Text(
                                text = if (isZh) "OLED純黑、Material You、調色盤" else "OLED Black, Material You, Palettes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNavigateToSettings() }
                            .pointerHoverIcon(PointerIcon.Hand)
                    )

                    HorizontalDivider(color = cardBorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // My Reviews
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = {
                            Text(if (isZh) "我的評價" else "My Reviews", fontWeight = FontWeight.Medium)
                        },
                        leadingContent = {
                            Icon(Icons.Default.RateReview, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onNavigateToReviews() }
                            .pointerHoverIcon(PointerIcon.Hand)
                    )
                }
            }

            if (user != null) {
                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = { viewModel.logout {} },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isZh) "登出帳號" else "Sign Out", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // Card 4: Footer Section (Matching web screenshot and user prompt)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Pills: 贊助 + 正常
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 贊助 (Donate) pink pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFCE7F3),
                        border = BorderStroke(1.dp, Color(0xFFF472B6).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { openUrl("https://ko-fi.com/lingubible") }
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "❤️", fontSize = 13.sp)
                            Text(
                                text = if (isZh) "贊助" else "Support",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBE185D)
                            )
                        }
                    }

                    // 正常 (Status) green pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Text(
                                text = if (isZh) "正常 ✓" else "Operational ✓",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // Quick Links Row: 入門指南 · 更新日誌 · 常見問題 · 聯絡 · 條款 · 隱私
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val links = listOf(
                        Pair(if (isZh) "入門指南" else "Guide", "https://lingubible.com"),
                        Pair(if (isZh) "更新日誌" else "Changelog", "https://lingubible.com"),
                        Pair(if (isZh) "常見問題" else "FAQ", "https://lingubible.com"),
                        Pair(if (isZh) "聯絡" else "Contact", "https://lingubible.com"),
                        Pair(if (isZh) "條款" else "Terms", "https://lingubible.com"),
                        Pair(if (isZh) "隱私" else "Privacy", "https://lingubible.com")
                    )

                    links.forEachIndexed { index, pair ->
                        Text(
                            text = pair.first,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { openUrl(pair.second) }
                                .pointerHoverIcon(PointerIcon.Hand)
                        )
                        if (index < links.size - 1) {
                            Text(
                                text = "  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Attribution line explicitly co-founding with Anson Lo
                Text(
                    text = if (isZh) {
                        "開源工具構建 · 由 Ricky Poon 與 Anson Lo 共同創辦開發"
                    } else {
                        "Built with open-source tools · Co-founded by Ricky Poon & Anson Lo"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // University Disclaimer
                Text(
                    text = if (isZh) {
                        "本應用程式與嶺南大學（LU）並無任何隸屬關係及未獲其認可"
                    } else {
                        "This app is not affiliated with or endorsed by Lingnan University (LU)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                // Separator with dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                // License Badge + Year
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "MIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "2026 LingUBible",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // GitHub icon & link: https://github.com/ricky688/LingUBible_android.git
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .clickable { openUrl("https://github.com/ricky688/LingUBible_android.git") }
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🐙", fontSize = 15.sp)
                        Text(
                            text = "GitHub",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Avatar Customizer Dialog
    AvatarCustomizerModal(
        isOpen = showAvatarModal,
        onDismiss = { showAvatarModal = false },
        initialAnimal = customAvatar?.animal ?: "🐢",
        initialBackgroundIndex = customAvatar?.backgroundIndex ?: 57,
        isZh = isZh,
        onSave = { animal, bgIndex ->
            viewModel.saveCustomAvatar(animal, bgIndex) { success ->
                if (success) {
                    Toast.makeText(context, if (isZh) "頭像保存成功！" else "Avatar saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, if (isZh) "頭像保存失敗，請重試" else "Failed to save avatar", Toast.LENGTH_LONG).show()
                }
            }
        }
    )
}
