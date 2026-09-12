package com.lingubible.app.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CalendarCategory(
    val labelZh: String,
    val labelEn: String,
    val colorHex: String
) {
    @SerialName("term")
    TERM("學期開始／結束", "Term Begin / End", "#DC2626"),

    @SerialName("exam")
    EXAM("考試", "Examinations", "#CA8A04"),

    @SerialName("holiday")
    HOLIDAY("假期／停課", "Holiday / Class Suspended", "#8B5CF6"),

    @SerialName("addDrop")
    ADD_DROP("加退選期", "Add / Drop Period", "#84CC16"),

    @SerialName("registration")
    REGISTRATION("註冊", "Registration", "#0EA5E9"),

    @SerialName("deadline")
    DEADLINE("截止日期", "Deadline", "#F97316"),

    @SerialName("assessment")
    ASSESSMENT("成績／覆核", "Grades / Appeals", "#22C55E"),

    @SerialName("graduation")
    GRADUATION("畢業", "Graduation", "#6366F1"),

    @SerialName("event")
    EVENT("活動", "Event", "#64748B"),

    @SerialName("hostel")
    HOSTEL("宿舍", "Hostel", "#14B8A6");

    val composeColor: Color
        get() = Color(android.graphics.Color.parseColor(colorHex))
}

@Serializable
data class AcademicEvent(
    val id: String,
    val start: String, // "YYYY-MM-DD"
    val end: String? = null, // "YYYY-MM-DD" or null for single day
    val category: CalendarCategory,
    val category2: CalendarCategory? = null,
    val title: String,
    @SerialName("title_tc")
    val titleTc: String = "",
    @SerialName("title_sc")
    val titleSc: String = ""
) {
    val isMultiDay: Boolean
        get() = end != null && end != start

    val displayTitle: String
        get() = if (titleTc.isNotBlank()) titleTc else title

    val secondaryTitle: String
        get() = if (titleTc.isNotBlank()) title else ""

    val dateRangeDisplay: String
        get() {
            if (!isMultiDay) return start
            return "$start ~ $end"
        }

    fun formatWebDateRange(): String {
        return try {
            val sParts = start.split("-")
            val sMonth = sParts[1].toInt()
            val sDay = sParts[2].toInt()
            if (!isMultiDay || end == null) {
                "${sMonth}月${sDay}日"
            } else {
                val eParts = end.split("-")
                val eMonth = eParts[1].toInt()
                val eDay = eParts[2].toInt()
                "${sMonth}月${sDay}日 – ${eMonth}月${eDay}日"
            }
        } catch (e: Exception) {
            dateRangeDisplay
        }
    }
}
