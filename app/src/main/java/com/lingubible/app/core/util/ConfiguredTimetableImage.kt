package com.lingubible.app.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.lingubible.app.domain.model.*

data class TimetableExportColors(val background: Int, val surface: Int, val text: Int, val outline: Int)

internal fun configuredTimetableImage(orientation: TimetableExportOrientation, term: TimetableTerm, sections: List<TimetableSection>,
    conflicts: List<TimeConflict>, credits: Map<String, Int>, gridLines: Boolean, options: TimetableAppearance, colors: TimetableExportColors): Bitmap {
    require(timetableAppearanceError(options, term) == null)
    val baseWidth = if (orientation == TimetableExportOrientation.LANDSCAPE) 2400 else 1440
    val baseHeight = if (orientation == TimetableExportOrientation.LANDSCAPE) 1400 else 2560
    val (width, height) = timetableExportSize(baseWidth, baseHeight, options.resolution)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    try {
        val canvas = Canvas(bitmap)
        canvas.scale(width.toFloat() / baseWidth, height.toFloat() / baseHeight)
        canvas.drawColor(colors.background)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val text = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.text; textSize = 28f }
        val header = 150f
        val dayHeader = 75f
        val timeWidth = 110f
        val top = header + dayHeader
        val gridHeight = baseHeight - top - 24f
        val days = options.orderedDays
        val colWidth = (baseWidth - timeWidth - 24f) / days.size
        val (start, end) = timetableMinuteRange(sections, options)
        fun y(minutes: Int) = top + (minutes - start).toFloat() / (end - start) * gridHeight
        fun paragraph(value: String, x: Float, y: Float, w: Float, h: Float, size: Float, color: Int) {
            if (w <= 0 || h < size) return
            text.textSize = size; text.color = color
            val layout = StaticLayout.Builder.obtain(value, 0, value.length, text, w.toInt().coerceAtLeast(1))
                .setMaxLines((h / (size * 1.2f)).toInt().coerceAtLeast(1)).setEllipsize(TextUtils.TruncateAt.END).setIncludePad(false).build()
            canvas.save(); canvas.clipRect(x, y, x + w, y + h); canvas.translate(x, y); layout.draw(canvas); canvas.restore()
        }
        paragraph(options.title.ifBlank { term.name }, 24f, 24f, baseWidth - 48f, 60f, 40f, colors.text)
        paragraph("LingUBible · " + TimetableImageExport.computeSummaryText(sections, credits), 24f, 90f, baseWidth - 48f, 40f, 26f, colors.text)
        days.forEachIndexed { index, day ->
            val x = timeWidth + index * colWidth
            paint.color = colors.surface; paint.style = Paint.Style.FILL
            canvas.drawRect(x, header, x + colWidth, baseHeight - 24f, paint)
            val label = com.lingubible.app.ui.components.DAYS_SUN_TO_SAT.first { it.code == day }.let { "${it.zh} ${it.en}" }
            paragraph(label, x + 10f, header + 20f, colWidth - 20f, 40f, 26f, colors.text)
        }
        paint.color = colors.outline; paint.style = Paint.Style.STROKE; paint.strokeWidth = 1f
        for (index in 0..days.size) canvas.drawLine(timeWidth + index * colWidth, header, timeWidth + index * colWidth, baseHeight - 24f, paint)
        for (minute in start until end step 60) {
            val lineY = y(minute)
            if (gridLines) canvas.drawLine(timeWidth, lineY, baseWidth - 24f, lineY, paint)
            paragraph(timetableClock(minute), 4f, lineY + 6f, timeWidth - 8f, 40f, 23f, colors.text)
        }
        sections.forEach { section -> section.meetings.forEach meetingLoop@ { meeting ->
            val column = days.indexOf(meeting.day)
            val from = maxOf(start, meeting.startMinutes)
            val until = minOf(end, meeting.endMinutes + 1)
            if (column < 0 || until <= from) return@meetingLoop
            val left = timeWidth + column * colWidth + 4f
            val rect = RectF(left, y(from) + 2f, left + colWidth - 8f, y(until) - 2f)
            val hex = options.colors[section.courseCode] ?: section.colorHex
            paint.color = Color.parseColor(hex); paint.style = Paint.Style.FILL
            canvas.drawRoundRect(rect, 14f, 14f, paint)
            if (conflicts.any { (it.sectionA.id == section.id || it.sectionB.id == section.id) && it.day == meeting.day }) {
                paint.color = Color.RED; paint.style = Paint.Style.STROKE; paint.strokeWidth = 4f
                canvas.drawRoundRect(rect, 14f, 14f, paint)
            }
            val lines = timetableBlockLines(section, meeting, options, credits[section.courseCode.uppercase()])
            paragraph(lines.joinToString("\n"), rect.left + 10f, rect.top + 8f, rect.width() - 20f, rect.height() - 16f,
                if (orientation == TimetableExportOrientation.LANDSCAPE) 22f else 20f,
                if (timetableUsesLightText(hex)) Color.WHITE else Color.BLACK)
        } }
        return bitmap
    } catch (error: Throwable) { bitmap.recycle(); throw error }
}
