package com.lingubible.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.lingubible.app.domain.model.TimeConflict
import com.lingubible.app.domain.model.TimetableMeeting
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.domain.model.TimetableTerm
import com.lingubible.app.ui.components.DAYS_SUN_TO_SAT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class TimetableExportOrientation(
    val titleZh: String,
    val titleEn: String,
    val subtitleZh: String,
    val subtitleEn: String,
    val resolutionLabel: String
) {
    LANDSCAPE(
        titleZh = "橫向寬螢幕",
        titleEn = "Landscape",
        subtitleZh = "適合電腦 / 平板檢視與列印",
        subtitleEn = "For PC, Tablet & Print",
        resolutionLabel = "2400 × 1400 (16:9 寬螢幕)"
    ),
    PORTRAIT(
        titleZh = "直向標準手機",
        titleEn = "Portrait",
        subtitleZh = "標準手機比例，適合桌布與社群分享",
        subtitleEn = "Standard phone ratio (9:16) for wallpaper & stories",
        resolutionLabel = "1440 × 2560 (9:16 手機比例)"
    )
}

object TimetableImageExport {

    const val LANDSCAPE_WIDTH = 2400
    const val LANDSCAPE_HEIGHT = 1400

    const val PORTRAIT_WIDTH = 1440
    const val PORTRAIT_HEIGHT = 2560

    // Backward-compatibility aliases
    const val EXPORT_WIDTH = LANDSCAPE_WIDTH
    const val EXPORT_HEIGHT = LANDSCAPE_HEIGHT

    private const val HEADER_HEIGHT = 130f
    private const val DAY_HEADER_HEIGHT = 75f
    private const val TIME_COL_WIDTH = 140f
    private val DAY_COL_WIDTH = (EXPORT_WIDTH - TIME_COL_WIDTH) / 7f

    data class TimetableHourRange(
        val startHour: Int,
        val endHour: Int
    ) {
        val totalHours: Int get() = endHour - startHour
        val startMinuteTotal: Int get() = startHour * 60 + 30
        val totalMinutes: Int get() = totalHours * 60
    }

    /**
     * Computes dynamic start and end hour slots based on the meetings of selected sections,
     * leaving at least 1 hour of breathing room at both top and bottom to prevent crowdness.
     * E.g. if the last event ends at 14:30, the grid extends to 15:30.
     */
    fun computeDynamicHourRange(selectedSections: List<TimetableSection>): TimetableHourRange {
        val allMeetings = selectedSections.flatMap { it.meetings }
        if (allMeetings.isEmpty()) {
            return TimetableHourRange(startHour = 8, endHour = 18) // default 08:30 to 18:30
        }

        val firstStartMin = allMeetings.minOf { it.startMinutes }
        val lastEndMin = allMeetings.maxOf { it.endMinutes }

        // 1. Top space: leave at least 1 hour of space before the earliest meeting (minimum 07:30, maximum 10:30)
        // E.g. earliest 08:30 (510 min) -> (510 - 80)/60 = 7 (07:30)
        // earliest 09:30 (570 min) -> (570 - 80)/60 = 8 (08:30)
        // earliest 10:30 (630 min) -> (630 - 80)/60 = 9 (09:30)
        val computedStartHour = ((firstStartMin - 80) / 60).coerceIn(7, 10)

        // 2. Bottom space: leave at least 1 hour of space after the latest meeting ends
        // E.g. last event ends at 14:30 (870 min) -> (870 + 84)/60 = 15 (15:30)
        // last event ends at 17:20 (1040 min) -> (1040 + 84)/60 = 18 (18:30)
        // last event ends at 20:50 (1250 min) -> (1250 + 84)/60 = 22 (22:30)
        val computedEndHour = ((lastEndMin + 84) / 60)

        // Ensure reasonable minimum span (at least 6 hours) so short schedules remain proportional
        val endHourWithMinSpan = maxOf(computedEndHour, computedStartHour + 6)
        val finalEndHour = endHourWithMinSpan.coerceIn(computedStartHour + 4, 23)

        return TimetableHourRange(startHour = computedStartHour, endHour = finalEndHour)
    }

    private const val GRID_TOP = HEADER_HEIGHT + DAY_HEADER_HEIGHT
    private const val GRID_HEIGHT = EXPORT_HEIGHT - GRID_TOP

    /**
     * Generates a high-definition landscape timetable bitmap (2400x1400)
     * with spacious day columns and comprehensive course event blocks.
     */
    fun generateLandscapeBitmap(
        term: TimetableTerm,
        selectedSections: List<TimetableSection>,
        conflicts: List<TimeConflict> = emptyList(),
        isDark: Boolean = false,
        creditsByCode: Map<String, Int> = emptyMap()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(EXPORT_WIDTH, EXPORT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette definitions
        val bgColor = if (isDark) 0xFF0F172A.toInt() else 0xFFFFFFFF.toInt()
        val headerBg = if (isDark) 0xFF1E293B.toInt() else 0xFFF8FAFC.toInt()
        val dayHeaderBg = if (isDark) 0xFF1E293B.toInt() else 0xFFF1F5F9.toInt()
        val gridBg = if (isDark) 0xFF0B1120.toInt() else 0xFFFAFAFA.toInt()
        val timeColBg = if (isDark) 0xFF1E293B.toInt() else 0xFFF8FAFC.toInt()
        val gridBorder = if (isDark) 0xFF334155.toInt() else 0xFFE2E8F0.toInt()
        val hourLineColor = if (isDark) 0xFF1E293B.toInt() else 0xFFF1F5F9.toInt()
        val textPrimary = if (isDark) 0xFFF8FAFC.toInt() else 0xFF0F172A.toInt()
        val textSecondary = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
        val lingnanRed = 0xFFDC2626.toInt()

        // Background fill
        val bgPaint = Paint().apply { color = bgColor; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, EXPORT_WIDTH.toFloat(), EXPORT_HEIGHT.toFloat(), bgPaint)

        // 1. Top Brand & Term Banner (0 to HEADER_HEIGHT)
        val bannerPaint = Paint().apply { color = headerBg; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, EXPORT_WIDTH.toFloat(), HEADER_HEIGHT, bannerPaint)

        val borderPaint = Paint().apply {
            color = gridBorder
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(0f, HEADER_HEIGHT, EXPORT_WIDTH.toFloat(), HEADER_HEIGHT, borderPaint)

        // LingUBible Brand Badge (Red Squircle)
        val brandBadgePaint = Paint().apply { color = lingnanRed; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawRoundRect(RectF(48f, 38f, 102f, 92f), 14f, 14f, brandBadgePaint)

        // Book mark inside badge
        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("📖", 75f, 75f, badgeTextPaint)

        // Brand Title & Subtitle
        val titlePaint = Paint().apply {
            color = textPrimary
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("LingUBible", 120f, 75f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = textSecondary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("每週課表 • Weekly Timetable", 325f, 75f, subtitlePaint)

        // Right side: Term & Course Summary Info
        val termPaint = Paint().apply {
            color = textPrimary
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(term.name, EXPORT_WIDTH - 48f, 58f, termPaint)

        val totalCredits = selectedSections.sumOf { s ->
            creditsByCode[s.courseCode.trim().uppercase(Locale.ROOT)] ?: 3
        }
        val summaryText = "${selectedSections.size} 科課程 • 共 $totalCredits 學分"
        val summaryPaint = Paint().apply {
            color = textSecondary
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(summaryText, EXPORT_WIDTH - 48f, 95f, summaryPaint)

        // 2. Day Headers Row (HEADER_HEIGHT to GRID_TOP)
        val dayHeaderPaint = Paint().apply { color = dayHeaderBg; style = Paint.Style.FILL }
        canvas.drawRect(0f, HEADER_HEIGHT, EXPORT_WIDTH.toFloat(), GRID_TOP, dayHeaderPaint)
        canvas.drawLine(0f, GRID_TOP, EXPORT_WIDTH.toFloat(), GRID_TOP, borderPaint)

        // Top-left "時間 Time" label
        val timeHeaderPaint = Paint().apply {
            color = textSecondary
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("時間 Time", TIME_COL_WIDTH / 2f, HEADER_HEIGHT + 44f, timeHeaderPaint)
        canvas.drawLine(TIME_COL_WIDTH, HEADER_HEIGHT, TIME_COL_WIDTH, EXPORT_HEIGHT.toFloat(), borderPaint)

        // 7 Day Headers
        val dayZhPaint = Paint().apply {
            color = textPrimary
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val dayEnPaint = Paint().apply {
            color = textSecondary
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in DAYS_SUN_TO_SAT.indices) {
            val day = DAYS_SUN_TO_SAT[i]
            val cx = TIME_COL_WIDTH + i * DAY_COL_WIDTH + (DAY_COL_WIDTH / 2f)
            canvas.drawText(day.zh, cx, HEADER_HEIGHT + 35f, dayZhPaint)
            canvas.drawText(day.en, cx, HEADER_HEIGHT + 60f, dayEnPaint)

            val dividerX = TIME_COL_WIDTH + (i + 1) * DAY_COL_WIDTH
            canvas.drawLine(dividerX, HEADER_HEIGHT, dividerX, EXPORT_HEIGHT.toFloat(), borderPaint)
        }

        // 3. Grid Body (GRID_TOP to EXPORT_HEIGHT)
        // Time column background
        val timeColBgPaint = Paint().apply { color = timeColBg; style = Paint.Style.FILL }
        canvas.drawRect(0f, GRID_TOP, TIME_COL_WIDTH, EXPORT_HEIGHT.toFloat(), timeColBgPaint)

        // Day columns background
        val gridBgPaint = Paint().apply { color = gridBg; style = Paint.Style.FILL }
        canvas.drawRect(TIME_COL_WIDTH, GRID_TOP, EXPORT_WIDTH.toFloat(), EXPORT_HEIGHT.toFloat(), gridBgPaint)

        val hourLinePaint = Paint().apply {
            color = hourLineColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val hourTextPaint = Paint().apply {
            color = textSecondary
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val hourRange = computeDynamicHourRange(selectedSections)
        val startHour = hourRange.startHour
        val endHour = hourRange.endHour
        val totalHours = hourRange.totalHours
        val startMinuteTotal = hourRange.startMinuteTotal
        val totalMinutes = hourRange.totalMinutes

        for (h in 0..totalHours) {
            val y = GRID_TOP + h * (GRID_HEIGHT / totalHours.toFloat())
            // Horizontal hour separator
            canvas.drawLine(0f, y, EXPORT_WIDTH.toFloat(), y, hourLinePaint)
            // Time mark (avoid cutting off bottom text label)
            val textY = if (h == totalHours) (y - 8f).coerceAtMost(EXPORT_HEIGHT - 6f) else y + 22f
            canvas.drawText(String.format("%02d:30", h + startHour), TIME_COL_WIDTH / 2f, textY, hourTextPaint)
        }

        // 4. Course Event Blocks
        val cardFillPaint = Paint().apply { style = Paint.Style.FILL; isAntiAlias = true }
        val cardStrokePaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1.5f; isAntiAlias = true }
        val clashStrokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            color = 0xFFEF4444.toInt()
            isAntiAlias = true
        }

        val codePaint = Paint().apply {
            color = Color.WHITE
            textSize = 25f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val titlePaintCard = Paint().apply {
            color = Color.argb(235, 255, 255, 255)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val typePaint = Paint().apply {
            color = Color.argb(230, 255, 255, 255)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val venuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val timePaint = Paint().apply {
            color = Color.argb(220, 255, 255, 255)
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val clashBadgeBg = Paint().apply { color = 0xFFDC2626.toInt(); style = Paint.Style.FILL; isAntiAlias = true }
        val clashBadgeText = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val typeBadgeBg = Paint().apply {
            color = Color.argb(70, 0, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val typeBadgeStroke = Paint().apply {
            color = Color.argb(60, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val typeBadgeText = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        for (section in selectedSections) {
            for (meeting in section.meetings) {
                val dayIndex = DAYS_SUN_TO_SAT.indexOfFirst { it.code.equals(meeting.day, ignoreCase = true) }
                if (dayIndex < 0) continue

                val startMin = (meeting.startMinutes - startMinuteTotal).coerceAtLeast(0)
                val durationMin = (meeting.endMinutes - meeting.startMinutes + 1).coerceAtLeast(30)

                val top = GRID_TOP + (startMin.toFloat() / totalMinutes.toFloat()) * GRID_HEIGHT
                val cardHeight = (durationMin.toFloat() / totalMinutes.toFloat()) * GRID_HEIGHT
                val bottom = top + cardHeight

                val colLeft = TIME_COL_WIDTH + dayIndex * DAY_COL_WIDTH
                val left = colLeft + 6f
                val right = colLeft + DAY_COL_WIDTH - 6f
                val cardRect = RectF(left, top, right, bottom)

                val isClashing = conflicts.any {
                    (it.sectionA.id == section.id || it.sectionB.id == section.id) && it.day.equals(meeting.day, ignoreCase = true)
                }

                val blockColor = try {
                    Color.parseColor(section.colorHex)
                } catch (_: Exception) {
                    lingnanRed
                }

                // Draw Card
                cardFillPaint.color = blockColor
                canvas.drawRoundRect(cardRect, 14f, 14f, cardFillPaint)

                if (isClashing) {
                    canvas.drawRoundRect(cardRect, 14f, 14f, clashStrokePaint)
                } else {
                    cardStrokePaint.color = Color.argb(60, 255, 255, 255)
                    canvas.drawRoundRect(cardRect, 14f, 14f, cardStrokePaint)
                }

                // Inner card content
                val contentLeft = left + 14f
                val contentRight = right - 14f
                val contentWidth = contentRight - contentLeft

                // Top-right corner badge: LEC / TUT
                val typeStr = meeting.type.ifBlank { "LEC" }.uppercase()
                val typeTextWidth = try { typeBadgeText.measureText(typeStr) } catch (_: Exception) { 36f }
                val typeBadgeWidth = (typeTextWidth + 18f).coerceAtLeast(46f)
                val badgeTop = top + 10f
                val badgeBottom = badgeTop + 24f
                val typeBadgeRect = RectF(contentRight - typeBadgeWidth, badgeTop, contentRight, badgeBottom)
                canvas.drawRoundRect(typeBadgeRect, 6f, 6f, typeBadgeBg)
                canvas.drawRoundRect(typeBadgeRect, 6f, 6f, typeBadgeStroke)
                canvas.drawText(typeStr, typeBadgeRect.centerX(), badgeTop + 17f, typeBadgeText)

                // Optional Clash badge to the left of LEC/TUT badge
                val codeRightLimit: Float
                if (isClashing) {
                    val clashBadgeWidth = 52f
                    val clashRight = typeBadgeRect.left - 6f
                    val clashLeft = clashRight - clashBadgeWidth
                    val clashRect = RectF(clashLeft, badgeTop, clashRight, badgeBottom)
                    canvas.drawRoundRect(clashRect, 6f, 6f, clashBadgeBg)
                    canvas.drawText("衝堂", clashRect.centerX(), badgeTop + 17f, clashBadgeText)
                    codeRightLimit = clashLeft - 6f
                } else {
                    codeRightLimit = typeBadgeRect.left - 6f
                }

                val courseCodeMaxWidth = (codeRightLimit - contentLeft).coerceAtLeast(60f)
                val courseTitle = section.courseTitle.ifBlank { section.courseTitleZh }
                val truncatedTitle = truncateText(courseTitle, titlePaintCard, contentWidth)

                val crnText = when {
                    section.section.isNotBlank() && section.crn.isNotBlank() -> "Sect ${section.section} • CRN ${section.crn}"
                    section.crn.isNotBlank() -> "CRN ${section.crn}"
                    section.section.isNotBlank() -> "Section ${section.section}"
                    else -> ""
                }
                val venueText = if (meeting.venue.isNotBlank()) "📍 ${meeting.venue}" else "📍 TBA"
                val timeSpanText = "🕒 ${meeting.start} - ${meeting.end}"

                if (cardHeight >= 145f) {
                    // Full multi-line layout for 1.5h - 3h meetings
                    // Line 1: Course code on top-left, LEC/TUT on top-right
                    canvas.drawText(truncateText(section.courseCode, codePaint, courseCodeMaxWidth), contentLeft, top + 29f, codePaint)

                    // Line 2: Truncated Course Name
                    canvas.drawText(truncatedTitle, contentLeft, top + 56f, titlePaintCard)

                    // Line 3: Section & CRN
                    canvas.drawText(crnText, contentLeft, top + 82f, typePaint)

                    // Line 4: Location
                    canvas.drawText(venueText, contentLeft, top + 107f, venuePaint)

                    // Line 5: Time
                    canvas.drawText(timeSpanText, contentLeft, top + 132f, timePaint)
                } else if (cardHeight >= 95f) {
                    // Medium compact layout for 1h - 1.5h meetings
                    // Row 1: Course code on top-left, LEC/TUT on top-right
                    canvas.drawText(truncateText(section.courseCode, codePaint, courseCodeMaxWidth), contentLeft, top + 27f, codePaint)

                    // Row 2: Truncated Course Name
                    canvas.drawText(truncatedTitle, contentLeft, top + 50f, titlePaintCard)

                    // Row 3: Section & CRN
                    canvas.drawText(crnText, contentLeft, top + 72f, typePaint)

                    // Row 4: Combined Venue & Time
                    val combinedVenueTime = "${if (meeting.venue.isNotBlank()) meeting.venue else "TBA"}  •  ${meeting.start}-${meeting.end}"
                    canvas.drawText(combinedVenueTime, contentLeft, top + 93f, venuePaint)
                } else {
                    // Short layout (< 1h meetings)
                    canvas.drawText(truncateText(section.courseCode, codePaint, courseCodeMaxWidth), contentLeft, top + 26f, codePaint)
                    canvas.drawText(truncatedTitle, contentLeft, top + 49f, titlePaintCard)
                    val compactInfo = "${if (section.crn.isNotBlank()) "CRN ${section.crn} • " else ""}${if (meeting.venue.isNotBlank()) meeting.venue else "TBA"} • ${meeting.start}-${meeting.end}"
                    canvas.drawText(compactInfo, contentLeft, top + 70f, venuePaint)
                }
            }
        }

        return bitmap
    }

    /**
     * Generates a high-definition portrait timetable bitmap (1440x2560)
     * matching standard modern smartphone screen ratio (9:16).
     */
    fun generatePortraitBitmap(
        term: TimetableTerm,
        selectedSections: List<TimetableSection>,
        conflicts: List<TimeConflict> = emptyList(),
        isDark: Boolean = false,
        creditsByCode: Map<String, Int> = emptyMap()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(PORTRAIT_WIDTH, PORTRAIT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette definitions
        val bgColor = if (isDark) 0xFF0F172A.toInt() else 0xFFFFFFFF.toInt()
        val headerBg = if (isDark) 0xFF1E293B.toInt() else 0xFFF8FAFC.toInt()
        val dayHeaderBg = if (isDark) 0xFF1E293B.toInt() else 0xFFF1F5F9.toInt()
        val gridBg = if (isDark) 0xFF0B1120.toInt() else 0xFFFAFAFA.toInt()
        val timeColBg = if (isDark) 0xFF1E293B.toInt() else 0xFFF8FAFC.toInt()
        val gridBorder = if (isDark) 0xFF334155.toInt() else 0xFFE2E8F0.toInt()
        val hourLineColor = if (isDark) 0xFF1E293B.toInt() else 0xFFF1F5F9.toInt()
        val textPrimary = if (isDark) 0xFFF8FAFC.toInt() else 0xFF0F172A.toInt()
        val textSecondary = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
        val lingnanRed = 0xFFDC2626.toInt()

        val portraitHeaderHeight = 160f
        val portraitDayHeaderHeight = 80f
        val portraitTimeColWidth = 130f
        val portraitDayColWidth = (PORTRAIT_WIDTH - portraitTimeColWidth) / 7f
        val portraitGridTop = portraitHeaderHeight + portraitDayHeaderHeight
        val portraitGridHeight = PORTRAIT_HEIGHT - portraitGridTop

        // Background fill
        val bgPaint = Paint().apply { color = bgColor; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, PORTRAIT_WIDTH.toFloat(), PORTRAIT_HEIGHT.toFloat(), bgPaint)

        // 1. Top Brand & Term Banner
        val bannerPaint = Paint().apply { color = headerBg; style = Paint.Style.FILL }
        canvas.drawRect(0f, 0f, PORTRAIT_WIDTH.toFloat(), portraitHeaderHeight, bannerPaint)

        val borderPaint = Paint().apply {
            color = gridBorder
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(0f, portraitHeaderHeight, PORTRAIT_WIDTH.toFloat(), portraitHeaderHeight, borderPaint)

        // Brand Badge (Red Squircle)
        val brandBadgePaint = Paint().apply { color = lingnanRed; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawRoundRect(RectF(36f, 36f, 96f, 96f), 14f, 14f, brandBadgePaint)

        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("📖", 66f, 76f, badgeTextPaint)

        // Brand Title & Subtitle
        val titlePaint = Paint().apply {
            color = textPrimary
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("LingUBible", 112f, 72f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = textSecondary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("每週課表 • Weekly Timetable", 112f, 115f, subtitlePaint)

        // Right side: Term & Course Summary Info
        val termPaint = Paint().apply {
            color = textPrimary
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(term.name, PORTRAIT_WIDTH - 36f, 70f, termPaint)

        val totalCredits = selectedSections.sumOf { s ->
            creditsByCode[s.courseCode.trim().uppercase(Locale.ROOT)] ?: 3
        }
        val summaryText = "${selectedSections.size} 科課程 • 共 $totalCredits 學分"
        val summaryPaint = Paint().apply {
            color = textSecondary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(summaryText, PORTRAIT_WIDTH - 36f, 115f, summaryPaint)

        // 2. Day Headers Row
        val dayHeaderPaint = Paint().apply { color = dayHeaderBg; style = Paint.Style.FILL }
        canvas.drawRect(0f, portraitHeaderHeight, PORTRAIT_WIDTH.toFloat(), portraitGridTop, dayHeaderPaint)
        canvas.drawLine(0f, portraitGridTop, PORTRAIT_WIDTH.toFloat(), portraitGridTop, borderPaint)

        // Top-left "時間 Time" label
        val timeHeaderPaint = Paint().apply {
            color = textSecondary
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("時間 Time", portraitTimeColWidth / 2f, portraitHeaderHeight + 48f, timeHeaderPaint)
        canvas.drawLine(portraitTimeColWidth, portraitHeaderHeight, portraitTimeColWidth, PORTRAIT_HEIGHT.toFloat(), borderPaint)

        // 7 Day Headers
        val dayZhPaint = Paint().apply {
            color = textPrimary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val dayEnPaint = Paint().apply {
            color = textSecondary
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in DAYS_SUN_TO_SAT.indices) {
            val day = DAYS_SUN_TO_SAT[i]
            val cx = portraitTimeColWidth + i * portraitDayColWidth + (portraitDayColWidth / 2f)
            canvas.drawText(day.zh, cx, portraitHeaderHeight + 36f, dayZhPaint)
            canvas.drawText(day.en, cx, portraitHeaderHeight + 62f, dayEnPaint)

            val dividerX = portraitTimeColWidth + (i + 1) * portraitDayColWidth
            canvas.drawLine(dividerX, portraitHeaderHeight, dividerX, PORTRAIT_HEIGHT.toFloat(), borderPaint)
        }

        // 3. Grid Body
        val timeColBgPaint = Paint().apply { color = timeColBg; style = Paint.Style.FILL }
        canvas.drawRect(0f, portraitGridTop, portraitTimeColWidth, PORTRAIT_HEIGHT.toFloat(), timeColBgPaint)

        val gridBgPaint = Paint().apply { color = gridBg; style = Paint.Style.FILL }
        canvas.drawRect(portraitTimeColWidth, portraitGridTop, PORTRAIT_WIDTH.toFloat(), PORTRAIT_HEIGHT.toFloat(), gridBgPaint)

        val hourLinePaint = Paint().apply {
            color = hourLineColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val hourTextPaint = Paint().apply {
            color = textSecondary
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val hourRange = computeDynamicHourRange(selectedSections)
        val startHour = hourRange.startHour
        val endHour = hourRange.endHour
        val totalHours = hourRange.totalHours
        val startMinuteTotal = hourRange.startMinuteTotal
        val totalMinutes = hourRange.totalMinutes

        for (h in 0..totalHours) {
            val y = portraitGridTop + h * (portraitGridHeight / totalHours.toFloat())
            canvas.drawLine(0f, y, PORTRAIT_WIDTH.toFloat(), y, hourLinePaint)
            val textY = if (h == totalHours) (y - 8f).coerceAtMost(PORTRAIT_HEIGHT - 6f) else y + 22f
            canvas.drawText(String.format("%02d:30", h + startHour), portraitTimeColWidth / 2f, textY, hourTextPaint)
        }

        // 4. Course Event Blocks
        val cardFillPaint = Paint().apply { style = Paint.Style.FILL; isAntiAlias = true }
        val cardStrokePaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1.5f; isAntiAlias = true }
        val clashStrokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = 0xFFEF4444.toInt()
            isAntiAlias = true
        }
        val codePaint = Paint().apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val titlePaintCard = Paint().apply {
            color = Color.argb(235, 255, 255, 255)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val typePaint = Paint().apply {
            color = Color.argb(230, 255, 255, 255)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val venuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val timePaint = Paint().apply {
            color = Color.argb(220, 255, 255, 255)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val clashBadgeBg = Paint().apply { color = 0xFFDC2626.toInt(); style = Paint.Style.FILL; isAntiAlias = true }
        val clashBadgeText = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val typeBadgeBg = Paint().apply {
            color = Color.argb(70, 0, 0, 0)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val typeBadgeStroke = Paint().apply {
            color = Color.argb(60, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val typeBadgeText = Paint().apply {
            color = Color.WHITE
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        for (section in selectedSections) {
            for (meeting in section.meetings) {
                val dayIndex = DAYS_SUN_TO_SAT.indexOfFirst { it.code.equals(meeting.day, ignoreCase = true) }
                if (dayIndex < 0) continue

                val startMin = (meeting.startMinutes - startMinuteTotal).coerceAtLeast(0)
                val durationMin = (meeting.endMinutes - meeting.startMinutes + 1).coerceAtLeast(30)

                val top = portraitGridTop + (startMin.toFloat() / totalMinutes.toFloat()) * portraitGridHeight
                val cardHeight = (durationMin.toFloat() / totalMinutes.toFloat()) * portraitGridHeight
                val bottom = top + cardHeight

                val colLeft = portraitTimeColWidth + dayIndex * portraitDayColWidth
                val left = colLeft + 4f
                val right = colLeft + portraitDayColWidth - 4f
                val cardRect = RectF(left, top, right, bottom)

                val isClashing = conflicts.any {
                    (it.sectionA.id == section.id || it.sectionB.id == section.id) && it.day.equals(meeting.day, ignoreCase = true)
                }

                val blockColor = try {
                    Color.parseColor(section.colorHex)
                } catch (_: Exception) {
                    lingnanRed
                }

                // Draw Card
                cardFillPaint.color = blockColor
                canvas.drawRoundRect(cardRect, 12f, 12f, cardFillPaint)

                if (isClashing) {
                    canvas.drawRoundRect(cardRect, 12f, 12f, clashStrokePaint)
                } else {
                    cardStrokePaint.color = Color.argb(60, 255, 255, 255)
                    canvas.drawRoundRect(cardRect, 12f, 12f, cardStrokePaint)
                }

                // Inner card content
                val contentLeft = left + 10f
                val contentRight = right - 10f
                val contentWidth = contentRight - contentLeft

                // Top-right corner badge: LEC / TUT
                val typeStr = meeting.type.ifBlank { "LEC" }.uppercase()
                val typeTextWidth = try { typeBadgeText.measureText(typeStr) } catch (_: Exception) { 30f }
                val typeBadgeWidth = (typeTextWidth + 14f).coerceAtLeast(38f)
                val badgeTop = top + 8f
                val badgeBottom = badgeTop + 22f
                val typeBadgeRect = RectF(contentRight - typeBadgeWidth, badgeTop, contentRight, badgeBottom)
                canvas.drawRoundRect(typeBadgeRect, 6f, 6f, typeBadgeBg)
                canvas.drawRoundRect(typeBadgeRect, 6f, 6f, typeBadgeStroke)
                canvas.drawText(typeStr, typeBadgeRect.centerX(), badgeTop + 16f, typeBadgeText)

                // Optional Clash badge to the left of LEC/TUT badge
                val codeRightLimit: Float
                if (isClashing) {
                    val clashBadgeWidth = 44f
                    val clashRight = typeBadgeRect.left - 4f
                    val clashLeft = clashRight - clashBadgeWidth
                    val clashRect = RectF(clashLeft, badgeTop, clashRight, badgeBottom)
                    canvas.drawRoundRect(clashRect, 6f, 6f, clashBadgeBg)
                    canvas.drawText("衝堂", clashRect.centerX(), badgeTop + 16f, clashBadgeText)
                    codeRightLimit = clashLeft - 4f
                } else {
                    codeRightLimit = typeBadgeRect.left - 4f
                }

                val courseCodeMaxWidth = (codeRightLimit - contentLeft).coerceAtLeast(40f)
                val courseTitle = section.courseTitle.ifBlank { section.courseTitleZh }
                val truncatedTitle = truncateText(courseTitle, titlePaintCard, contentWidth)

                val crnText = when {
                    section.section.isNotBlank() && section.crn.isNotBlank() -> "S${section.section} • CRN ${section.crn}"
                    section.crn.isNotBlank() -> "CRN ${section.crn}"
                    section.section.isNotBlank() -> "Section ${section.section}"
                    else -> ""
                }
                val venueText = if (meeting.venue.isNotBlank()) "📍 ${meeting.venue}" else "📍 TBA"
                val timeSpanText = "🕒 ${meeting.start} - ${meeting.end}"

                if (cardHeight >= 115f) {
                    // Full multi-line layout (most portrait classes are >= 170px)
                    canvas.drawText(truncateText(section.courseCode, codePaint, courseCodeMaxWidth), contentLeft, top + 25f, codePaint)
                    canvas.drawText(truncatedTitle, contentLeft, top + 49f, titlePaintCard)
                    canvas.drawText(crnText, contentLeft, top + 72f, typePaint)
                    canvas.drawText(venueText, contentLeft, top + 94f, venuePaint)
                    canvas.drawText(timeSpanText, contentLeft, top + 116f, timePaint)
                } else if (cardHeight >= 80f) {
                    canvas.drawText(truncateText(section.courseCode, codePaint, courseCodeMaxWidth), contentLeft, top + 24f, codePaint)
                    canvas.drawText(truncatedTitle, contentLeft, top + 46f, titlePaintCard)
                    canvas.drawText(crnText, contentLeft, top + 68f, typePaint)
                    val compactInfo = "${if (meeting.venue.isNotBlank()) meeting.venue else "TBA"} • ${meeting.start}-${meeting.end}"
                    canvas.drawText(compactInfo, contentLeft, top + 88f, venuePaint)
                } else {
                    canvas.drawText(truncateText(section.courseCode, codePaint, courseCodeMaxWidth), contentLeft, top + 23f, codePaint)
                    canvas.drawText(truncatedTitle, contentLeft, top + 45f, titlePaintCard)
                    val compactInfo = "${if (section.crn.isNotBlank()) "CRN ${section.crn} • " else ""}${if (meeting.venue.isNotBlank()) meeting.venue else "TBA"}"
                    canvas.drawText(compactInfo, contentLeft, top + 66f, venuePaint)
                }
            }
        }

        return bitmap
    }

    /**
     * Generates a timetable bitmap according to the requested orientation.
     */
    fun generateBitmap(
        orientation: TimetableExportOrientation,
        term: TimetableTerm,
        selectedSections: List<TimetableSection>,
        conflicts: List<TimeConflict> = emptyList(),
        isDark: Boolean = false,
        creditsByCode: Map<String, Int> = emptyMap()
    ): Bitmap {
        return when (orientation) {
            TimetableExportOrientation.LANDSCAPE -> generateLandscapeBitmap(term, selectedSections, conflicts, isDark, creditsByCode)
            TimetableExportOrientation.PORTRAIT -> generatePortraitBitmap(term, selectedSections, conflicts, isDark, creditsByCode)
        }
    }

    /**
     * Truncates text with an ellipsis (...) if its rendered width exceeds maxWidth.
     */
    fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (text.isBlank()) return text
        try {
            if (paint.measureText(text) <= maxWidth) return text
            var low = 0
            var high = text.length
            var best = ""
            while (low <= high) {
                val mid = (low + high) / 2
                val candidate = text.substring(0, mid).trimEnd() + "..."
                if (paint.measureText(candidate) <= maxWidth) {
                    best = candidate
                    low = mid + 1
                } else {
                    high = mid - 1
                }
            }
            return if (best.isNotBlank()) best else text.take(1) + "..."
        } catch (_: Exception) {
            // Fallback for JVM unit tests where paint.measureText may not be mocked
            return if (text.length > 22) text.take(20).trimEnd() + "..." else text
        }
    }

    suspend fun capture(
        context: Context,
        orientation: TimetableExportOrientation,
        term: TimetableTerm,
        selectedSections: List<TimetableSection>,
        conflicts: List<TimeConflict> = emptyList(),
        isDark: Boolean = false,
        creditsByCode: Map<String, Int> = emptyMap()
    ): File = withContext(Dispatchers.IO) {
        val bitmap = generateBitmap(orientation, term, selectedSections, conflicts, isDark, creditsByCode)
        try {
            val prefix = if (orientation == TimetableExportOrientation.LANDSCAPE) "timetable-landscape-" else "timetable-portrait-"
            val file = File.createTempFile(prefix, ".png", context.cacheDir)
            try {
                file.outputStream().use { out ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) { "Failed to compress timetable bitmap" }
                }
                file
            } catch (e: Exception) {
                file.delete()
                throw e
            }
        } finally {
            bitmap.recycle()
        }
    }

    suspend fun captureLandscape(
        context: Context,
        term: TimetableTerm,
        selectedSections: List<TimetableSection>,
        conflicts: List<TimeConflict> = emptyList(),
        isDark: Boolean = false,
        creditsByCode: Map<String, Int> = emptyMap()
    ): File = capture(context, TimetableExportOrientation.LANDSCAPE, term, selectedSections, conflicts, isDark, creditsByCode)

    suspend fun capturePortrait(
        context: Context,
        term: TimetableTerm,
        selectedSections: List<TimetableSection>,
        conflicts: List<TimeConflict> = emptyList(),
        isDark: Boolean = false,
        creditsByCode: Map<String, Int> = emptyMap()
    ): File = capture(context, TimetableExportOrientation.PORTRAIT, term, selectedSections, conflicts, isDark, creditsByCode)

    suspend fun capture(context: Context, headerLayer: GraphicsLayer, bodyLayer: GraphicsLayer, background: Int): File {
        val header = checkNotNull(headerLayer.toImageBitmap().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false))
        try {
            val body = checkNotNull(bodyLayer.toImageBitmap().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false))
            try {
                return withContext(Dispatchers.IO) {
                    require(header.width == body.width && body.height > 0) { "Timetable not ready" }
                    val image = Bitmap.createBitmap(header.width, header.height + body.height, Bitmap.Config.ARGB_8888)
                    try {
                        Canvas(image).apply {
                            drawColor(background)
                            drawBitmap(header, 0f, 0f, null)
                            drawBitmap(body, 0f, header.height.toFloat(), null)
                        }
                        val file = File.createTempFile("timetable-", ".png", context.cacheDir)
                        try {
                            file.outputStream().use { check(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                            file
                        } catch (error: Exception) {
                            file.delete()
                            throw error
                        }
                    } finally {
                        image.recycle()
                    }
                }
            } finally {
                body.recycle()
            }
        } finally {
            header.recycle()
        }
    }

    fun saveToDocument(context: Context, source: File, destination: Uri) {
        source.inputStream().use { input ->
            val output = context.contentResolver.openOutputStream(destination, "wt")
                ?: error("Cannot open image destination")
            output.use { input.copyTo(it) }
        }
    }
}
