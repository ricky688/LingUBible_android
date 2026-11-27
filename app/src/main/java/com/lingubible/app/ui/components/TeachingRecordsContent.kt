package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.navigation.screenContentPadding
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.domain.model.*
import org.koin.compose.koinInject

@Composable
fun TeachingRecordsContent(records: List<TeachingRecord>, onInstructor: (String) -> Unit) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    val groups = remember(records) { records.groupBy { it.term to it.academicYear } }
    FadingLazyColumn(Modifier.fillMaxSize(), contentPadding = screenContentPadding(start = 16.dp, top = 8.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (records.isEmpty()) item { Text(if (isZh) "暫無任教記錄 · No teaching records available" else "No teaching records available") }
        groups.forEach { (term, group) ->
            item(key = "term:${term.first}:${term.second}") {
                Text(
                    teachingTermLabel(group.first()).let { if (isZh) it else it.replace(Regex("[\\u4e00-\\u9fff\\u3000-\\u303f\\uff00-\\uffef]+\\s*"), "").trim() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(group, key = { _, record -> "record:${record.id}" }) { index, record ->
                TeachingRecordListItem(record, index, group.size, onInstructor)
            }
        }
    }
}
