package com.lingubible.app.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream

// Public website key; secrets and verification decisions stay in the existing backend.
private const val SITE_KEY = "6LevaFkrAAAAAF6zzK1djMY4Mik8xJbttS-BcMsT"
private const val ORIGIN = "https://www.lingubible.com"

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun SecurityVerification(action: String, isZh: Boolean, onToken: (String) -> Unit, onError: () -> Unit) {
    require(action in setOf("forgotPassword", "contact"))
    val tokenCallback by rememberUpdatedState(onToken)
    val errorCallback by rememberUpdatedState(onError)
    val handshake = remember { java.util.UUID.randomUUID().toString() }
    var completed by remember { mutableStateOf(false) }
    var port by remember { mutableStateOf<WebMessagePort?>(null) }
    fun finish(token: String?) {
        if (completed) return
        completed = true
        if (token != null && token.length in 20..8192) tokenCallback(token) else errorCallback()
    }
    LaunchedEffect(Unit) { delay(30000); finish(null) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (isZh) "正在進行安全驗證…" else "Verifying your request…", style = MaterialTheme.typography.bodyMedium)
        AndroidView(modifier = Modifier.fillMaxWidth().height(100.dp), factory = { context ->
            try { WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.setSupportMultipleWindows(false)
                var channelConnected = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = request.isForMainFrame
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                        val host = request.url.host.orEmpty()
                        val allowed = request.url.scheme == "https" && (host == "www.google.com" || host == "www.gstatic.com" || host == "recaptcha.google.com")
                        return if (allowed) null else WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                    }
                    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                        if (request.isForMainFrame) finish(null)
                    }
                    override fun onPageFinished(view: WebView, url: String) {
                        if (completed || channelConnected || url != "$ORIGIN/android-security-check") return
                        channelConnected = true
                        val channels = view.createWebMessageChannel()
                        port?.close(); port = channels[0]
                        channels[0].setWebMessageCallback(object : WebMessagePort.WebMessageCallback() {
                            override fun onMessage(port: WebMessagePort, message: WebMessage?) { finish(message?.data?.takeUnless { it == "ERROR" }) }
                        })
                        view.postWebMessage(WebMessage(handshake, arrayOf(channels[1])), Uri.parse(ORIGIN))
                    }
                }
                // Native messages have no web sender origin; bind this app-owned page with a nonce and exact target origin.
                val html = """<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1"><script>
                    window.addEventListener('message', function(e) {
                        if(e.data !== '$handshake' || e.ports.length !== 1 || window.nativeConnected) return;
                        window.nativeConnected=true;
                        var p=e.ports[0];
                        try { grecaptcha.ready(function(){ grecaptcha.execute('$SITE_KEY',{action:'$action'}).then(function(t){p.postMessage(t)},function(){p.postMessage('ERROR')}) }); }
                        catch(e){p.postMessage('ERROR')}
                    });
                    </script><script src="https://www.google.com/recaptcha/api.js?render=$SITE_KEY&hl=${if (isZh) "zh-TW" else "en"}"></script></head><body></body></html>"""
                loadDataWithBaseURL("$ORIGIN/android-security-check", html, "text/html", "UTF-8", "$ORIGIN/android-security-check")
            } } catch (_: RuntimeException) {
                finish(null)
                android.widget.Space(context)
            }
        }, onRelease = { view ->
            completed = true; port?.close(); port = null
            (view as? WebView)?.let { it.stopLoading(); it.destroy() }
        })
    }
}
