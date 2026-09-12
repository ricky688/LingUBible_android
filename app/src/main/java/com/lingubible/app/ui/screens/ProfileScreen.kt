package com.lingubible.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.M3ButtonGroupItem
import com.lingubible.app.ui.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.settings.ThemeMode
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
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.currentUser
    val themeMode by settingsManager.themeMode.collectAsState()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW

    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user?.name?.take(1) ?: "L").uppercase(),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (user != null) {
                    Text(
                        text = user.name.ifBlank { if (isZh) "嶺南大學同學" else "Lingnan Student" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isZh) "嶺南學生已認證" else "Verified Student",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                } else {
                    Text(
                        text = if (isZh) "訪客用戶" else "Guest User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (isZh) "使用嶺南大學學生電郵 (@ln.hk) 登入以解鎖所有功能" else "Sign in with your @ln.hk student email to unlock all features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToAuth,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isZh) "使用嶺南郵箱登入 / 註冊" else "Sign In / Register with LN Email", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Settings & Preferences Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Theme Mode Switcher
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
                                M3ButtonGroupItem(if (isZh) "淺色" else "Light", Icons.Filled.LightMode) { settingsManager.setThemeMode(ThemeMode.LIGHT) },
                                M3ButtonGroupItem(if (isZh) "深色" else "Dark", Icons.Filled.DarkMode) { settingsManager.setThemeMode(ThemeMode.DARK) },
                                M3ButtonGroupItem(if (isZh) "系統" else "Auto", Icons.Filled.BrightnessAuto) { settingsManager.setThemeMode(ThemeMode.SYSTEM) }
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

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.6f), thickness = 0.5.dp)

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

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Appearance & Customization Settings Entry
                        ListItem(
                            headlineContent = {
                                Text(if (isZh) "外觀與個人化自訂" else "Appearance & Customization", fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                val oledBlack by settingsManager.isOledBlack.collectAsState()
                                val colorScheme by settingsManager.colorScheme.collectAsState()
                                val isDynamic by settingsManager.isDynamicColor.collectAsState()
                                Text(
                                    text = buildString {
                                        append(if (isZh) themeMode.titleZh else themeMode.titleEn)
                                        if (isDark && oledBlack) append(if (isZh) "・OLED 純黑" else "・OLED Black")
                                        append("・")
                                        if (isDynamic) append(if (isZh) "動態配色" else "Dynamic Monet")
                                        else append(colorScheme.label(isZh))
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Filled.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigateToSettings() }
                                .pointerHoverIcon(PointerIcon.Hand)
                        )

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // My Reviews
                        ListItem(
                            headlineContent = {
                                Text(if (isZh) "我的評價" else "My Reviews", fontWeight = FontWeight.Medium)
                            },
                            leadingContent = {
                                Icon(Icons.Filled.RateReview, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigateToReviews() }
                                .pointerHoverIcon(PointerIcon.Hand)
                        )

                        HorizontalDivider(color = cardBorderColor.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Favorites
                        ListItem(
                            headlineContent = {
                                Text(if (isZh) "我的收藏" else "Saved Favorites", fontWeight = FontWeight.Medium)
                            },
                            leadingContent = {
                                Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .pointerHoverIcon(PointerIcon.Hand)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (user != null) {
                    OutlinedButton(
                        onClick = { viewModel.logout {} },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
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

                // Footer version & app info
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "LingUBible Android v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) md_theme_dark_onSurfaceVariant else Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(140.dp))
            }
        }
    }
