package com.lingubible.app.domain.calculator

data class GpaFortune(
    val id: Int,
    val grade: String,
    val title: String,
    val poem: List<String>,
    val interpretation: String,
    val oracleAppropriate: String,
    val oracleInappropriate: String,
    val isGodTier: Boolean = false,
    val gradeEn: String = "",
    val titleEn: String = "",
    val poemEn: List<String> = emptyList(),
    val interpretationEn: String = "",
    val oracleAppropriateEn: String = "",
    val oracleInappropriateEn: String = ""
) {
    fun grade(isZh: Boolean): String = if (isZh) grade else gradeEn.ifBlank { grade }
    fun title(isZh: Boolean): String = if (isZh) title else titleEn.ifBlank { title }
    fun poem(isZh: Boolean): List<String> = if (isZh) poem else poemEn.ifEmpty { poem }
    fun interpretation(isZh: Boolean): String = if (isZh) interpretation else interpretationEn.ifBlank { interpretation }
    fun oracleAppropriate(isZh: Boolean): String = if (isZh) oracleAppropriate else oracleAppropriateEn.ifBlank { oracleAppropriate }
    fun oracleInappropriate(isZh: Boolean): String = if (isZh) oracleInappropriate else oracleInappropriateEn.ifBlank { oracleInappropriate }
}

object GpaFortuneRepository {
    val FORTUNES = listOf(
        GpaFortune(
            id = 1,
            grade = "【特特上籤 · 天選神籤】",
            title = "嶺南武林至尊 · 4.0 登頂",
            poem = listOf(
                "天賦異稟讀神科，科科拎 A 唔使拖。",
                "四點零滿分在手，Dean's List 永遠刻你名。"
            ),
            interpretation = "組員爭住叫你做大佬，Prof 睇到你份 Assignment 都起立鼓掌！成個學期你係無敵嘅，快啲去飲多杯凍檸茶慶祝啦！",
            oracleAppropriate = "接受眾人膜拜、請朋友食茶記",
            oracleInappropriate = "太謙虛講自己「完全冇溫書」",
            isGodTier = true,
            gradeEn = "[Supreme Tier · Chosen Oracle]",
            titleEn = "Lingnan Grandmaster · 4.0 Summit",
            poemEn = listOf(
                "Gifted in every course, straight A's without a hitch.",
                "With a 4.0 in hand, your name is etched on the Dean's List."
            ),
            interpretationEn = "Groupmates crown you the carry, and even your Prof gives a standing ovation for your assignment! You're unstoppable this term—go grab an iced lemon tea to celebrate!",
            oracleAppropriateEn = "Accepting praise gracefully, treating friends to cha chaan teng",
            oracleInappropriateEn = "Humblebragging that you 'didn't study at all'"
        ),
        GpaFortune(
            id = 2,
            grade = "【第貳籤 · 過三上上吉】",
            title = "一級榮譽在望 · 天地線助攻",
            poem = listOf(
                "過三望四好意頭，靚 Grade 滾滾唔使愁。",
                "星期五放假天地線，瞓到自然醒至走。"
            ),
            interpretation = "求學業大吉！Reg 科秒速中第一志願，成個學期冇早八（8:30am 堂）。Final 出題全部撞正你睇過嘅 Slide，First Hon 穩如泰山！",
            oracleAppropriate = "早啲瞓覺、養足精神",
            oracleInappropriate = "期末考前一晚先嚟通頂狂煲",
            isGodTier = false,
            gradeEn = "[Lot 2 · Supreme 3.0+ Blessing]",
            titleEn = "First Class in Sight · Day-Off Alignment",
            poemEn = listOf(
                "Passing 3.0 and eyeing 4.0, sweet grades roll in effortlessly.",
                "Fridays off with a clean timetable—sleep in till noon."
            ),
            interpretationEn = "Great academic fortune! You land your first-choice courses instantly with zero 8:30 AM classes. Every final exam question comes straight from the slides you reviewed—First Hon is rock solid!",
            oracleAppropriateEn = "Getting early sleep, staying well-rested",
            oracleInappropriateEn = "Pulling an all-nighter the night before finals"
        ),
        GpaFortune(
            id = 3,
            grade = "【第叁籤 · 貴人相助籤】",
            title = "隊友個個神助攻 · Free-rider 自動退賽",
            poem = listOf(
                "分組唔怕遇爛泥，隊友 Carry 唔使提。",
                "每人交足十頁紙，Present 順過飲咖啡。"
            ),
            interpretation = "求 Group Project 絕無潛水怪及 Free-rider！分工清晰，Meeting 一次就傾完，唔使你一個人托全家。Prof 讚你哋係近年見過最齊心嘅 Group！",
            oracleAppropriate = "分工合作、多啲讚賞隊友",
            oracleInappropriate = "已讀不回 WhatsApp group 訊息",
            isGodTier = false,
            gradeEn = "[Lot 3 · Dream Teammates]",
            titleEn = "All-Star Groupmates · Zero Free-Riders",
            poemEn = listOf(
                "No deadweight in your group—teammates carry their share.",
                "Ten solid pages from everyone, presentations smoother than coffee."
            ),
            interpretationEn = "Zero ghosting and zero free-riders in your group project! Clear roles, one-shot meetings, and no solo-carrying needed. Your Prof calls your team the most cohesive group in years!",
            oracleAppropriateEn = "Sharing tasks fairly, hyping up your teammates",
            oracleInappropriateEn = "Leaving WhatsApp group messages on read"
        ),
        GpaFortune(
            id = 4,
            grade = "【第肆籤 · 靚 Curve 護體籤】",
            title = "Mean 隨人願 · 爛 Grade 退散",
            poem = listOf(
                "Midterm 唔識咪驚慌，全場都係咁樣撞。",
                "好彩阿 Sir 拉靚 Curve，即刻保住 B+ 磅。"
            ),
            interpretation = "求考試逢凶化吉！雖然 Midterm 覺得自己炒炒地，但原來全班 Mean 仲低過你。阿 Sir 一拉 Curve 即刻鹹魚翻身，保住漂亮 GPA！",
            oracleAppropriate = "多謝默默拉低個 Mean 嘅手足",
            oracleInappropriate = "考完試即刻衝去同人對答案",
            isGodTier = false,
            gradeEn = "[Lot 4 · Blessed by the Curve]",
            titleEn = "Favourable Mean · Bad Grades Begone",
            poemEn = listOf(
                "Don't panic if the midterm felt tough—everyone guessed too.",
                "A generous curve from the Prof locks in a solid B+."
            ),
            interpretationEn = "Turning exam peril into triumph! Even though you felt shaky after the midterm, the class mean is even lower. Once the curve kicks in, your GPA bounces right back!",
            oracleAppropriateEn = "Thanking classmates who quietly lowered the mean",
            oracleInappropriateEn = "Comparing answers right outside the exam hall"
        ),
        GpaFortune(
            id = 5,
            grade = "【第伍籤 · 辯才無礙籤】",
            title = "Q&A 完美拆彈 · 全場掌聲雷動",
            poem = listOf(
                "上台自信講英文，台下點頭無疑問。",
                "Prof 挑剔都挑唔到，滿分入袋笑吟吟。"
            ),
            interpretation = "求 Presentation 一帆風順！Slide 靚圖清，唔疾機、唔死火。即使 Prof 突擊問刁鑽問題，你都能言善辯，完美答中 Marking Scheme！",
            oracleAppropriate = "眼神接觸、自信微笑",
            oracleInappropriate = "成場對住個投影幕照稿讀",
            isGodTier = false,
            gradeEn = "[Lot 5 · Eloquence Unbound]",
            titleEn = "Flawless Q&A · Standing Ovation",
            poemEn = listOf(
                "Speak with confidence on stage while the audience nods along.",
                "Not even the Prof can nitpick—full marks in the bag."
            ),
            interpretationEn = "Smooth sailing for your presentation! Crisp slides, zero tech glitches. Even when the Prof throws a curveball question, you nail every point on the marking scheme!",
            oracleAppropriateEn = "Making eye contact, smiling with confidence",
            oracleInappropriateEn = "Reading word-for-word off the projector screen"
        ),
        GpaFortune(
            id = 6,
            grade = "【第陸籤 · 秒速神手籤】",
            title = "Banner 系統無窒機 · 爛堂 Waitlist 即刻清",
            poem = listOf(
                "開網 Reg 科快如電，想入邊班就邊班。",
                "killer 走佬唔教你，天使 Prof 帶你飛仙。"
            ),
            interpretation = "求排程選課心想事成！一撳即入，唔使排 Waitlist。揀中嘅全部都係靚 Grade 天使 Professor，Assignment 簡單又有 Heart！",
            oracleAppropriate = "準時開電腦、校好鬧鐘準備",
            oracleInappropriate = "最後一秒先發覺唔記得密碼",
            isGodTier = false,
            gradeEn = "[Lot 6 · Lightning Add/Drop]",
            titleEn = "Lag-Free Banner · Instant Waitlist Clear",
            poemEn = listOf(
                "Lightning-fast course registration—every section you want.",
                "Tough graders swapped out; angel Profs lead the way."
            ),
            interpretationEn = "Course registration wishes granted! Instant enrolment with zero waitlists. Every class you pick has a kind, generous Prof and straightforward assignments!",
            oracleAppropriateEn = "Logging in early, setting multiple alarms",
            oracleInappropriateEn = "Forgetting your password one minute before add/drop"
        ),
        GpaFortune(
            id = 7,
            grade = "【第柒籤 · 錦鯉化吉籤】",
            title = "大步檻過 · 直達三字頭",
            poem = listOf(
                "考試撞彩題題中，Assignment 趕切收工。",
                "低空飛過直達三，屋企人笑開眉峰。"
            ),
            interpretation = "求保 Grade 大順！雖然平時冇乜點上堂，但考試前突然福至心靈睇啱重點。剛好壓線過三，無驚無險又過一個 Term！",
            oracleAppropriate = "誠心拜謝 GPA God 保佑",
            oracleInappropriate = "下次再拖到 Deadline 前十分鐘交",
            isGodTier = false,
            gradeEn = "[Lot 7 · Lucky Koi Clutch]",
            titleEn = "Clutch Victory · Crossing 3.0",
            poemEn = listOf(
                "Lucky guesses hit the mark; assignments submitted just in time.",
                "Skimming past the 3.0 line—peace of mind all around."
            ),
            interpretationEn = "Clutch grade protection! Even after a laid-back semester, you miraculously revise the exact topics tested. You clear 3.0 right on the buzzer for a stress-free term!",
            oracleAppropriateEn = "Thanking the GPA God sincerely",
            oracleInappropriateEn = "Waiting until 10 minutes before the deadline again"
        ),
        GpaFortune(
            id = 8,
            grade = "【第捌籤 · 萬事亨通籤】",
            title = "文運亨通 · 嶺南錦鯉附體",
            poem = listOf(
                "虎地靈氣聚心頭，求 Grade 得 Grade 樂悠悠。",
                "春風得意馬蹄疾，畢業戴帽笑不休。"
            ),
            interpretation = "全方位開運大吉！不論主修副修、通識定語言，樣樣游刃有餘。圖書館實有位、印紙唔窒機、莊期考試雙豐收！",
            oracleAppropriate = "保持好心情、同朋友去莊宿玩下",
            oracleInappropriate = "自我懷疑、過度焦慮",
            isGodTier = false,
            gradeEn = "[Lot 8 · All-Round Prosperity]",
            titleEn = "Academic Bliss · Lingnan Lucky Koi",
            poemEn = listOf(
                "Fu Tei spirit fills your stride; every grade you wish arrives.",
                "Breezing through your studies, beaming at graduation."
            ),
            interpretationEn = "All-around good fortune! Major, minor, common core, or languages—you handle it all with ease. Library seats are always open, printers never jam, and campus life thrives!",
            oracleAppropriateEn = "Staying positive, hanging out with friends",
            oracleInappropriateEn = "Self-doubt and unnecessary stress"
        )
    )

    fun getRandomFortune(isFourFlat: Boolean = false, excludeId: Int? = null): GpaFortune {
        if (isFourFlat) {
            return FORTUNES.first { it.isGodTier }
        }
        val pool = if (excludeId != null && FORTUNES.size > 1) {
            FORTUNES.filter { it.id != excludeId }
        } else {
            FORTUNES
        }
        return pool.random()
    }
}
