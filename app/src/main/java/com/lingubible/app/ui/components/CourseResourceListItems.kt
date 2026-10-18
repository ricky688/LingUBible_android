package com.lingubible.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.util.FileUtils
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange

@Composable
private fun courseListShape(source: MutableInteractionSource, index: Int, count: Int): RoundedCornerShape {
    val pressed by source.collectIsPressedAsState()
    val outer by animateDpAsState(if (pressed) 12.dp else 24.dp,
        spring(stiffness = Spring.StiffnessMedium), label = "courseListCorner")
    return getGroupedItemShape(index, count, outerRadius = outer)
}

@Composable
internal fun TeachingRecordListItem(record: TeachingRecord, index: Int, count: Int, onInstructor: (String) -> Unit) {
    val source = remember { MutableInteractionSource() }
    val shape = courseListShape(source, index, count)
    val name = record.instructorName.trim()
    val chinese = record.instructorNameZh.trim()
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        ListItem(
            headlineContent = { Text(chinese.ifBlank { name.ifBlank { "講師未知 Unknown instructor" } }, style = MaterialTheme.typography.titleMedium) },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (chinese.isNotBlank() && name.isNotBlank() && chinese != name) Text(name)
                    teachingSessionLabel(record.section).takeIf { it.isNotBlank() }?.let { Text(it) }
                }
            },
            leadingContent = { Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Default.Person, null, Modifier.padding(12.dp).size(24.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            } },
            trailingContent = { if (name.isNotBlank()) Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).clip(shape).clickable(
                interactionSource = source, indication = ripple(), enabled = name.isNotBlank(), role = Role.Button,
                onClickLabel = "查看講師評價 View $name reviews", onClick = hapticClick { onInstructor(name) }
            )
        )
    }
}

@Composable
internal fun PastPaperListItem(paper: PastPaper, instructors: List<String>, index: Int, count: Int,
    selectable: Boolean, selected: Boolean, busy: Boolean, onSelected: (Boolean) -> Unit,
    onView: () -> Unit, onDownload: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val shape = courseListShape(source, index, count)
    Surface(shape = shape, color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow) {
        ListItem(
            overlineContent = { Text(listOf("PDF", FileUtils.formatFileSize(paper.fileSize).takeIf { paper.fileSize > 0 }).filterNotNull().joinToString(" · ")) },
            headlineContent = { Text(courseTermLabel(paper.term), style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(instructors.joinToString().ifBlank { "講師未知 Instructor unknown" })
                Text(paper.fileName, maxLines = 2, overflow = TextOverflow.Ellipsis)
            } },
            leadingContent = {
                if (selectable) Checkbox(selected, hapticChange(onSelected), enabled = !busy,
                    modifier = Modifier.semantics { contentDescription = "選取試題 Select ${paper.fileName}" })
                else Icon(Icons.Default.Description, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = { IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick(onDownload), enabled = !busy) {
                Icon(Icons.Default.Download, if (selectable) "下載試題 Download ${paper.fileName}" else "登入下載 Sign in to download ${paper.fileName}")
            } },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().clip(shape).clickable(interactionSource = source, indication = ripple(), role = Role.Button,
                onClickLabel = "檢視試題 View ${paper.fileName}", onClick = hapticClick(onView))
        )
    }
}
