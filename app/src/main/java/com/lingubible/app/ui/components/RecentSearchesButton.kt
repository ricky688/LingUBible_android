package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.data.repository.SearchHistoryStore
import com.lingubible.app.ui.common.hapticClick
import org.koin.compose.koinInject

@Composable
fun RecentSearchesButton(store: SearchHistoryStore, kind: String, onSearch: (String) -> Unit) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    var open by remember { mutableStateOf(false) }
    var entries by remember { mutableStateOf(emptyList<String>()) }
    IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { entries = store.read(kind); open = true }) { Icon(Icons.Default.History, if (isZh) "搜尋記錄 Recent searches" else "Recent searches") }
    if (open) AlertDialog(onDismissRequest = { open = false }, title = { Text(if (isZh) "搜尋記錄 Recent searches" else "Recent searches") },
        text = {
            FadingLazyColumn(Modifier.heightIn(max = 360.dp), bottomInset = 0.dp) {
                if (entries.isEmpty()) item { Text(if (isZh) "尚無搜尋記錄 No recent searches" else "No recent searches") }
                items(entries, key = { it }) { query ->
                    ListItem(headlineContent = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { store.add(kind, query); onSearch(query); open = false }) { Text(query) } },
                        trailingContent = { IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { store.remove(kind, query); entries = store.read(kind) }) { Icon(Icons.Default.Close, if (isZh) "移除搜尋 Remove search: $query" else "Remove search: $query") } })
                }
            }
        }, confirmButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { open = false }) { Text(if (isZh) "關閉 Close" else "Close") } },
        dismissButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { store.clear(kind); entries = emptyList() }, enabled = entries.isNotEmpty()) { Text(if (isZh) "清除全部 Clear all" else "Clear all") } })
}
