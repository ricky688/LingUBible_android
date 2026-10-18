package com.lingubible.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.core.navigation.LocalScreenBottomClearance
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.rememberCardPressShape
import org.koin.compose.koinInject

@Composable
fun GuideEntry(onClick: () -> Unit, isZh: Boolean) {
    val source = remember { MutableInteractionSource() }
    val shape = rememberCardPressShape(source)
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainer) {
        ListItem(headlineContent = { Text(if (isZh) "入門指南 Guide" else "Getting started") },
            supportingContent = { Text(if (isZh) "重看 7 個主題，了解 App 功能" else "Explore the app in seven topics") },
            leadingContent = { Icon(Icons.Default.Explore, null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, Modifier.size(18.dp)) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().clip(shape).clickable(interactionSource = source, indication = ripple(),
                role = Role.Button, onClickLabel = if (isZh) "開啟入門指南" else "Open guide", onClick = hapticClick(onClick)))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GettingStartedGuideSheet(onDismiss: () -> Unit, onNavigate: (Screen) -> Unit) {
    val settings = koinInject<AppSettingsManager>()
    val language by settings.appLanguage.collectAsStateWithLifecycle()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        CompositionLocalProvider(LocalScreenBottomClearance provides 0.dp) {
            Column(Modifier.fillMaxWidth().fillMaxHeight(.94f)) {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (language == AppLanguage.ZH_TW) "入門指南 Getting started" else "Getting started", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick(onDismiss)) { Icon(Icons.Default.Close, "關閉指南 Close guide") }
                }
                GettingStartedGuide(onClose = onDismiss, onNavigate = onNavigate, modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GettingStartedGuide(onClose: () -> Unit, onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier) {
    val settings = koinInject<AppSettingsManager>()
    val language by settings.appLanguage.collectAsStateWithLifecycle()
    val isZh = language == AppLanguage.ZH_TW
    var current by rememberSaveable { mutableIntStateOf(0) }
    var previous by rememberSaveable { mutableIntStateOf(current) }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val steps = GETTING_STARTED_STEPS
    val step = steps[current]
    LaunchedEffect(current) {
        if (previous != current) { listState.scrollToItem(0); previous = current }
    }
    Column(modifier.fillMaxSize()) {
        FadingLazyColumn(Modifier.fillMaxWidth().weight(1f), state = listState, bottomInset = 0.dp,
            contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item(key = "progress") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isZh) "步驟 ${current + 1} / ${steps.size}" else "Step ${current + 1} of ${steps.size}", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { current = 0 }) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                            Text(if (isZh) "重看 Replay" else "Replay")
                        }
                    }
                    LinearProgressIndicator(progress = { (current + 1f) / steps.size }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "指南進度 Guide progress" })
                    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                        OutlinedTextField(step.title.primary(isZh), {}, readOnly = true, label = { Text(if (isZh) "導覽主題 Guide topics" else "Guide topics") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, maxLines = 2,
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))
                        ExposedDropdownMenu(expanded, { expanded = false }) {
                            steps.forEachIndexed { index, topic -> DropdownMenuItem(
                                text = { Text("${index + 1}. ${topic.title.primary(isZh)}") },
                                onClick = hapticClick { current = index; expanded = false }) }
                        }
                    }
                }
            }
            item(key = "topic") {
                AnimatedContent(current, transitionSpec = {
                    val direction = if (targetState >= initialState) 1 else -1
                    (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it / 8 * direction }) togetherWith
                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                }, label = "guideTopic") { index ->
                    val topic = steps[index]
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
                        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(topic.icon, null, Modifier.padding(20.dp).size(40.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(Modifier.semantics { heading() }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(topic.title.primary(isZh), style = MaterialTheme.typography.headlineSmall)
                            Text(topic.title.secondary(isZh), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(topic.description.primary(isZh), style = MaterialTheme.typography.bodyLarge)
                        Text(topic.description.secondary(isZh), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        topic.bullets.forEachIndexed { number, bullet ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text("${number + 1}", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelLarge)
                                }
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(bullet.primary(isZh))
                                    Text(bullet.secondary(isZh), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            topic.links.forEach { link ->
                                FilledTonalButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onNavigate(link.destination) }) { Text(link.label.primary(isZh)) }
                            }
                        }
                    }
                }
            }
            item(key = "reopen-hint") {
                Text(if (isZh) "可隨時從用戶設定或外觀與設定重看。" else "Reopen this guide from User Settings or Appearance & Settings.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider()
        FlowRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(shapes = AppButtonDefaults.shapes(), enabled = current > 0, onClick = hapticClick { current-- }) { Text(if (isZh) "上一步 Previous" else "Previous") }
            Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { if (current == steps.lastIndex) onClose() else current++ }) {
                Text(if (current == steps.lastIndex) { if (isZh) "完成 Done" else "Done" } else { if (isZh) "下一步 Next" else "Next" })
            }
            TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onClose)) { Text(if (isZh) "關閉指南 Close" else "Close guide") }
        }
    }
}
