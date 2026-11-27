package com.lingubible.app.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.domain.model.publicDetailUrl
import com.lingubible.app.ui.common.hapticClick
import org.koin.compose.koinInject

@Composable
fun DetailShareButton(kind: String, value: String) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    val context = LocalContext.current
    IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick {
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, publicDetailUrl(kind, value))
            }
            context.startActivity(Intent.createChooser(intent, if (isZh) "分享 Share" else "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { Toast.makeText(context, if (isZh) "無法分享連結 Could not share link" else "Could not share link", Toast.LENGTH_LONG).show() }
    }) { Icon(Icons.Default.Share, if (isZh) "分享連結 Share link" else "Share link") }
}
