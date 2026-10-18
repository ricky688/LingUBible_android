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
import com.lingubible.app.data.repository.SearchHistoryStore
import com.lingubible.app.ui.common.hapticClick

@Composable
fun RecentSearchesButton(store: SearchHistoryStore, kind: String, onSearch: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var entries by remember { mutableStateOf(emptyList<String>()) }
    IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { entries = store.read(kind); open = true }) { Icon(Icons.Default.History, "搜尋記錄 Recent searches") }
    if (open) AlertDialog(onDismissRequest = { open = false }, title = { Text("搜尋記錄 Recent searches") },
        text = {
            FadingLazyColumn(Modifier.heightIn(max = 360.dp), bottomInset = 0.dp) {
                if (entries.isEmpty()) item { Text("尚無搜尋記錄 No recent searches") }
                items(entries, key = { it }) { query ->
                    ListItem(headlineContent = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { store.add(kind, query); onSearch(query); open = false }) { Text(query) } },
                        trailingContent = { IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { store.remove(kind, query); entries = store.read(kind) }) { Icon(Icons.Default.Close, "移除搜尋 Remove search: $query") } })
                }
            }
        }, confirmButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { open = false }) { Text("關閉 Close") } },
        dismissButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { store.clear(kind); entries = emptyList() }, enabled = entries.isNotEmpty()) { Text("清除全部 Clear all") } })
}
