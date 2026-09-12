package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.core.theme.md_theme_dark_outlineVariant
import com.lingubible.app.core.theme.md_theme_dark_surfaceContainer
import com.lingubible.app.core.theme.md_theme_dark_surfaceContainerLow
import com.lingubible.app.domain.model.TimetableTerm
import com.lingubible.app.domain.model.academicYear
import com.lingubible.app.domain.model.termLabelEn
import com.lingubible.app.domain.model.termLabelZh

/**
 * Material 3 Term Selector Menu.
 *
 * Concise, clean Material 3 dropdown menu grouped by Academic Year
 * with an elegant downward expand transition (unfold) instead of a pop-up,
 * driven by pure spring physics.
 */
@Composable
fun Material3TermSelector(
    selectedTerm: TimetableTerm,
    availableTerms: List<TimetableTerm>,
    onSelectTerm: (TimetableTerm) -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedState = remember { MutableTransitionState(false) }
    val isDark = isAppDarkTheme()
    val density = LocalDensity.current
    val yOffsetPx = with(density) { 40.dp.roundToPx() }

    val cardBorder = if (isDark) md_theme_dark_outlineVariant else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val menuBg = if (isDark) md_theme_dark_surfaceContainer else Color.White
    val triggerBg = if (isDark) md_theme_dark_surfaceContainerLow else Color.White

    // Animated chevron rotation with pure spring physics
    val arrowRotation by animateFloatAsState(
        targetValue = if (expandedState.targetState) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "termMenuChevronRotation"
    )

    // Group terms by academic year (e.g. "2026–27", "2025–26", "2024–25")
    val termsByYear = remember(availableTerms) {
        availableTerms.groupBy { it.academicYear }
    }

    Box(modifier = modifier) {
        // ── 1. Concise Material 3 Trigger Button ──
        OutlinedButton(
            onClick = {
                expandedState.targetState = !expandedState.targetState
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = triggerBg
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (expandedState.targetState) MaterialTheme.colorScheme.primary else cardBorder
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = Modifier
                .height(36.dp)
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(
                imageVector = Icons.Outlined.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectedTerm.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Term",
                tint = textMuted,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation)
            )
        }

        // ── 2. Expandable Dropdown Menu with Expand Transition (Unfold Downward) ──
        if (expandedState.currentState || expandedState.targetState) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(x = 0, y = yOffsetPx),
                onDismissRequest = {
                    expandedState.targetState = false
                },
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                AnimatedVisibility(
                    visibleState = expandedState,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + fadeOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = menuBg,
                        tonalElevation = 3.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.widthIn(min = 210.dp, max = 250.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val yearList = termsByYear.keys.toList()
                            yearList.forEachIndexed { index, year ->
                                // Academic Year Subheader
                                Text(
                                    text = "$year 學年",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )

                                // Term Options
                                val terms = termsByYear[year] ?: emptyList()
                                terms.forEach { term ->
                                    val isSelected = term.id == selectedTerm.id

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${term.termLabelZh} ${term.termLabelEn}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else textPrimary,
                                                fontSize = 13.sp
                                            )
                                        },
                                        trailingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            onSelectTerm(term)
                                            expandedState.targetState = false
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .height(40.dp)
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    )
                                }

                                // Subtle divider between academic years
                                if (index < yearList.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = cardBorder.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
