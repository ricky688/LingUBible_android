package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.LocalComponentActivity
import com.lingubible.app.core.navigation.*
import com.lingubible.app.core.settings.*
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.components.SecurityVerification
import com.lingubible.app.ui.components.fadingVerticalScroll
import com.lingubible.app.ui.viewmodels.*
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PasswordRecoveryScreen(viewModel: PasswordRecoveryViewModel, onSignIn: () -> Unit, onInfo: (InformationPage) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = koinInject<AppSettingsManager>()
    val language by settings.appLanguage.collectAsStateWithLifecycle()
    val isZh = language == AppLanguage.ZH_TW
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    var email by rememberSaveable { mutableStateOf(state.email) }
    var link by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val activity = LocalComponentActivity.current
    DisposableEffect(activity) {
        val wasSecure = activity?.window?.attributes?.flags?.and(android.view.WindowManager.LayoutParams.FLAG_SECURE) != 0
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { if (!wasSecure) activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE) }
    }
    LaunchedEffect(state.stage) {
        if (state.stage in setOf(RecoveryStage.COMPLETE, RecoveryStage.VALIDATING, RecoveryStage.REQUEST)) { password = ""; confirmation = ""; showPassword = false }
    }
    Column(Modifier.fillMaxSize().imePadding().fadingVerticalScroll(rememberScrollState(), LocalScreenBottomClearance.current)
        .padding(20.dp).screenBottomPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(if (isZh) "找回您的帳戶" else "Recover your account", style = MaterialTheme.typography.headlineSmall)
        Text(if (isZh) "輸入嶺南電郵接收重設連結，或開啟收到的連結以設定新密碼。" else "Request a reset link for your Lingnan email, or open a received link to set a new password.")
        AnimatedContent(state.stage to state.verificationId, transitionSpec = { fadeIn(spring(stiffness = 400f)) togetherWith fadeOut(spring(stiffness = 400f)) }, label = "passwordRecovery") { (stage, verificationId) ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (stage) {
                    RecoveryStage.REQUEST, RecoveryStage.SENT -> {
                        if (stage == RecoveryStage.SENT) Text(if (isZh) "如該電郵已註冊，您將收到重設連結。請查看垃圾郵件；連結一小時後失效且只能使用一次。" else "If an account exists, a reset link will arrive by email. Check spam too. The link expires in one hour and can be used once.", color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(email, { email = it }, label = { Text("嶺南電郵 Lingnan email") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                        Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { viewModel.beginRequest(email) }) { Text(if (stage == RecoveryStage.SENT) "重新寄送 Resend link" else "寄送重設連結 Send reset link") }
                        HorizontalDivider()
                        Text(if (isZh) "已有連結？貼上電郵中的完整重設連結。" else "Already have a link? Paste the full reset link from your email.")
                        OutlinedTextField(link, { if (it.length <= 2048) link = it }, label = { Text("重設連結 Reset link") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                        FilledTonalButton(shapes = AppButtonDefaults.shapes(), enabled = link.isNotBlank(), onClick = hapticClick { viewModel.openLink(link); link = "" }) { Text("開啟連結 Open link") }
                    }
                    RecoveryStage.VERIFYING -> {
                        key(verificationId) { SecurityVerification("forgotPassword", isZh, { viewModel.send(it, if (isZh) "zh-TW" else "en", dark) }, viewModel::verificationFailed) }
                        TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(viewModel::newRequest)) { Text("取消 Cancel") }
                    }
                    RecoveryStage.SENDING, RecoveryStage.VALIDATING -> {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text(if (stage == RecoveryStage.VALIDATING) "正在驗證連結… Checking link…" else "正在處理… Processing…")
                    }
                    RecoveryStage.RESET -> {
                        Text("12–40 字元，包含大寫、小寫、數字及符號；避免常見密碼。\n12–40 characters, with upper/lowercase letters, a number and a symbol. Avoid common passwords.", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(password, { password = it }, label = { Text("新密碼 New password") }, singleLine = true, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(confirmation, { confirmation = it }, label = { Text("再次輸入密碼 Confirm password") }, singleLine = true, isError = confirmation.isNotEmpty() && confirmation != password, visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                        TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { showPassword = !showPassword }) { Text(if (showPassword) "隱藏密碼 Hide passwords" else "顯示密碼 Show passwords") }
                        Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { viewModel.complete(password, confirmation) }) { Text("重設密碼 Reset password") }
                    }
                    RecoveryStage.COMPLETE -> {
                        Text("密碼已重設，請使用新密碼登入。\nPassword reset. Sign in with your new password.", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        state.error?.let { SupportErrorText(it) }
        TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { viewModel.newRequest(); onSignIn() }) { Text("返回登入 Back to sign in") }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(InformationPage.FAQ, InformationPage.CONTACT, InformationPage.TERMS, InformationPage.PRIVACY).forEach { page ->
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onInfo(page) }) { Text(page.title(isZh)) }
            }
        }
    }
}

@Composable
internal fun SupportErrorText(error: SupportError) {
    val text = when (error) {
        SupportError.EMAIL -> "請輸入有效的嶺南電郵。\nEnter a valid @ln.hk or @ln.edu.hk email."
        SupportError.PASSWORD -> "密碼不符合要求，請使用 12–40 字元及所需字元種類，並避免常見密碼。\nUse 12–40 characters with the required character types and avoid common passwords."
        SupportError.MISMATCH -> "兩次輸入的密碼不相符。\nPasswords do not match."
        SupportError.INVALID_LINK -> "連結無效、已過期或已使用，請重新申請。\nThis reset link is invalid, expired or already used. Request a new one."
        SupportError.RATE_LIMIT -> "操作太頻繁，請稍後再試。\nToo many requests. Please try again later."
        SupportError.CAPTCHA -> "安全驗證未完成，請檢查連線並重試。\nSecurity verification could not finish. Check your connection and retry."
        SupportError.CONTACT -> "請填妥姓名（最多 10 個詞）、有效電郵及至少 30 個以空格分隔的詞的訊息。\nEnter a name (up to 10 words), valid email and message of at least 30 space-separated words."
        SupportError.NETWORK -> "連線中斷或逾時，請檢查連線後再試。\nConnection failed or timed out. Check your connection and retry."
        SupportError.SERVICE -> "服務暫時無法完成要求，請稍後重試。\nThe service could not complete this request. Please try again later."
    }
    Text(text, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite })
}
