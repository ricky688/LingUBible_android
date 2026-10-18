package com.lingubible.app.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.lingubible.app.domain.model.publicDetailUrl
import com.lingubible.app.ui.common.hapticClick

@Composable
fun DetailShareButton(kind: String, value: String) {
    val context = LocalContext.current
    IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick {
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, publicDetailUrl(kind, value))
            }
            context.startActivity(Intent.createChooser(intent, "分享 Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { Toast.makeText(context, "無法分享連結 Could not share link", Toast.LENGTH_LONG).show() }
    }) { Icon(Icons.Default.Share, "分享連結 Share link") }
}
