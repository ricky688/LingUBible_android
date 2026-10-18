package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.BuildConfig
import com.lingubible.app.core.navigation.*
import com.lingubible.app.core.settings.*
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.components.FadingLazyColumn
import com.lingubible.app.ui.components.SecurityVerification
import com.lingubible.app.ui.viewmodels.ContactViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InformationScreen(page: InformationPage, onNavigate: (InformationPage) -> Unit) {
    val context = LocalContext.current
    val settings = koinInject<AppSettingsManager>()
    val language by settings.appLanguage.collectAsStateWithLifecycle()
    val isZh = language == AppLanguage.ZH_TW
    val document by produceState<InformationDocument?>(null, page) {
        value = if (page in setOf(InformationPage.TERMS, InformationPage.PRIVACY)) withContext(Dispatchers.IO) {
            runCatching { context.assets.open("information/${page.name.lowercase()}.json").bufferedReader().use { Json.decodeFromString<InformationDocument>(it.readText()) } }.getOrNull()
        } else null
    }
    var expanded by rememberSaveable(page) { mutableStateOf("") }
    FadingLazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = screenContentPadding(20.dp, 16.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item(key = "intro") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(page.title(isZh), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Text(page.title(!isZh), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (page == InformationPage.CHANGELOG) {
                    Text("LingUBible Android ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.titleMedium)
                    Text(if (isZh) "此處僅記錄 Android 客戶端改動；本地版本的功能可能尚未在 Google Play 發布。" else "Android client changes only. Local-build features may not yet be available on Google Play.")
                }
                document?.let { BilingualParagraph(it.subtitle, isZh); BilingualParagraph(it.updated, isZh) }
            }
        }
        if (page == InformationPage.CONTACT) {
            item(key = "contact-form") { ContactContent(isZh) }
        } else {
            val sections = when (page) { InformationPage.FAQ -> ANDROID_FAQ; InformationPage.CHANGELOG -> ANDROID_CHANGELOG; else -> document?.sections.orEmpty() }
            if (page in setOf(InformationPage.TERMS, InformationPage.PRIVACY)) {
                item(key = "policy-context") {
                    Text(if (isZh) "以下為平台政策的內置副本，保留原有更新日期。Android 補充說明列於頁末。" else "A bundled copy of the platform policy follows, with its original update date. Android-specific notes appear below.", style = MaterialTheme.typography.bodySmall)
                }
            }
            sections.forEach { section -> item(key = section.id) {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (page == InformationPage.FAQ) {
                            ListItem(headlineContent = { Text(section.title.primary(isZh)) }, supportingContent = { Text(section.title.secondary(isZh)) },
                                trailingContent = { Icon(Icons.Default.ExpandMore, null) }, colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                                modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = if (isZh) "展開或收起答案" else "Expand or collapse answer", onClick = hapticClick { expanded = if (expanded == section.id) "" else section.id }).semantics { stateDescription = if (expanded == section.id) "已展開 Expanded" else "已收起 Collapsed" })
                            AnimatedVisibility(expanded == section.id, enter = expandVertically(spring(stiffness = 400f)) + fadeIn(spring(stiffness = 400f)), exit = shrinkVertically(spring(stiffness = 400f)) + fadeOut(spring(stiffness = 400f))) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { section.paragraphs.forEach { BilingualParagraph(it, isZh) } }
                            }
                        } else {
                            Column(Modifier.semantics { heading() }) {
                                Text(section.title.primary(isZh), style = MaterialTheme.typography.titleMedium)
                                Text(section.title.secondary(isZh), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            section.paragraphs.forEach { BilingualParagraph(it, isZh) }
                        }
                    }
                }
            } }
            if (page in setOf(InformationPage.TERMS, InformationPage.PRIVACY)) item(key = "android-note") {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Android 補充說明 · Android notes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                        BilingualParagraph(InformationText("App 會在裝置保存外觀設定、首次指南狀態、課表、手動 GPA 資料及帳戶專屬評價草稿（七天）。匯出的檔案由您選擇儲存位置；解除安裝不會刪除您另行匯出的檔案。", "The app stores appearance settings, guide status, timetable selections, manual GPA data and account-specific review drafts (seven days) on the device. You choose where exports are saved; uninstalling does not remove files you exported separately."), isZh)
                        BilingualParagraph(InformationText("密碼重設連結和新密碼不會寫入 App 偏好設定或備份。安全驗證會連接 Google reCAPTCHA；平台政策中的 Cookie 說明適用於網頁及嵌入的驗證內容。", "Reset links and new passwords are not written to app preferences or backups. Security verification connects to Google reCAPTCHA. The platform's cookie information applies to web pages and embedded verification content."), isZh)
                        if (page == InformationPage.TERMS) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onNavigate(InformationPage.PRIVACY) }) { Text("閱讀隱私政策 Privacy Policy") }
                    }
                }
            }
        }
        item(key = "related") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HorizontalDivider()
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InformationPage.entries.filter { it != page }.forEach { target ->
                        TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onNavigate(target) }) { Text(target.title(isZh)) }
                    }
                }
                Text(if (isZh) "安全驗證受 Google 隱私政策及服務條款約束。" else "Security verification is subject to Google's Privacy Policy and Terms of Service.", style = MaterialTheme.typography.bodySmall)
                FlowRow {
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { openSupportLink(context, "https://policies.google.com/privacy") }) { Text("Google Privacy") }
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { openSupportLink(context, "https://policies.google.com/terms") }) { Text("Google Terms") }
                }
            }
        }
    }
}

@Composable
private fun BilingualParagraph(text: InformationText, isZh: Boolean) {
    SelectionContainer { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text.primary(isZh), style = MaterialTheme.typography.bodyMedium)
        Text(text.secondary(isZh), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactContent(isZh: Boolean) {
    val viewModel: ContactViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("general") }
    var message by rememberSaveable { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    val labels = listOf("一般查詢 General", "錯誤回報 Bug report", "評價回報 Review report", "帳戶問題 Account", "功能建議 Feature request", "其他 Other")
    val busy = state.verifying || state.sending
    LaunchedEffect(state.sent) { if (state.sent) { name = ""; email = ""; message = ""; type = "general" } }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (isZh) "請勿提交密碼、重設連結或其他敏感資料。" else "Do not send passwords, reset links or other sensitive information.")
        TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { openSupportLink(context, "mailto:support@lingubible.com") }) { Text("support@lingubible.com") }
        TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { openSupportLink(context, "https://github.com/ricky688/LingUBible_android/issues") }) { Text("Android 問題回報 · GitHub issues") }
        if (state.sent) {
            Text("訊息已成功寄出。感謝您的回饋！\nYour message was sent. Thank you for your feedback.", color = MaterialTheme.colorScheme.primary)
            TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(viewModel::reset)) { Text("另一則訊息 New message") }
        } else {
            OutlinedTextField(name, { if (it.length <= 200) name = it }, enabled = !busy, label = { Text("姓名 Name · ≤10 words") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(email, { if (it.length <= 254) email = it }, enabled = !busy, label = { Text("回覆電郵 Reply email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true)
            ExposedDropdownMenuBox(menu, { if (!busy) menu = !menu }) {
                OutlinedTextField(labels[CONTACT_TYPES.indexOf(type).coerceAtLeast(0)], {}, readOnly = true, enabled = !busy, label = { Text("類別 Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menu) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))
                ExposedDropdownMenu(menu, { menu = false }) { CONTACT_TYPES.forEachIndexed { index, key -> DropdownMenuItem(text = { Text(labels[index]) }, onClick = hapticClick { type = key; menu = false }) } }
            }
            OutlinedTextField(message, { if (it.length <= 10000) message = it }, enabled = !busy, label = { Text("訊息 Message") }, minLines = 5, supportingText = { Text("至少 30 個以空格分隔的詞 · At least 30 space-separated words") }, modifier = Modifier.fillMaxWidth())
            state.error?.let { SupportErrorText(it) }
            when {
                state.verifying -> {
                    SecurityVerification("contact", isZh, { viewModel.send(it, if (isZh) "zh-TW" else "en", dark) }, viewModel::verificationFailed)
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(viewModel::cancelVerification)) { Text("取消 Cancel") }
                }
                state.sending -> LinearProgressIndicator(Modifier.fillMaxWidth())
                else -> Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { viewModel.begin(ContactDraft(name, email, type, message)) }) { Text("傳送訊息 Send message") }
            }
        }
    }
}

private fun openSupportLink(context: Context, url: String) {
    try {
        val intent = Intent(if (url.startsWith("mailto:")) Intent.ACTION_SENDTO else Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        android.widget.Toast.makeText(context, "找不到可開啟的應用程式 · No compatible app available", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
fun InformationSettingsEntries(isZh: Boolean, onNavigate: (InformationPage) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        InformationPage.entries.forEachIndexed { index, page ->
            Surface(shape = RoundedCornerShape(topStart = if (index == 0) 24.dp else 4.dp, topEnd = if (index == 0) 24.dp else 4.dp, bottomStart = if (index == InformationPage.entries.lastIndex) 24.dp else 4.dp, bottomEnd = if (index == InformationPage.entries.lastIndex) 24.dp else 4.dp)) {
                ListItem(headlineContent = { Text(page.title(isZh)) }, supportingContent = { Text(page.title(!isZh)) }, leadingContent = { Icon(Icons.Default.Info, null) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = hapticClick { onNavigate(page) }))
            }
        }
    }
}
