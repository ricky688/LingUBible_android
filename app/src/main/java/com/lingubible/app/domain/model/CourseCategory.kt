package com.lingubible.app.domain.model

data class CourseCategory(
    val code: String,
    val nameZh: String,
    val nameEn: String
)

val COURSE_CATEGORIES = listOf(
    CourseCategory("ALL", "全部", "All Departments"),
    CourseCategory("CCC", "核心", "Common Core"),
    CourseCategory("ACT", "會計", "Accountancy"),
    CourseCategory("BUS", "商學", "Business Admin"),
    CourseCategory("CDS", "數據科學", "Data Science"),
    CourseCategory("CHI", "中文", "Chinese"),
    CourseCategory("ECO", "經濟", "Economics"),
    CourseCategory("ENG", "英文", "English"),
    CourseCategory("FIN", "金融", "Finance"),
    CourseCategory("GOV", "政府政治", "Gov & Int Affairs"),
    CourseCategory("HST", "歷史", "History"),
    CourseCategory("MGT", "管理", "Management"),
    CourseCategory("MKT", "市場", "Marketing"),
    CourseCategory("PHI", "哲學", "Philosophy"),
    CourseCategory("POL", "政治", "Political Sci"),
    CourseCategory("PSY", "心理", "Psychology"),
    CourseCategory("SCI", "科學", "Science"),
    CourseCategory("SOC", "社會", "Sociology"),
    CourseCategory("TRA", "翻譯", "Translation")
)
