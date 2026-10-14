package com.lingubible.app.domain.model

import androidx.compose.ui.graphics.Color

data class LingnanCat(
    val id: String,
    val nameZh: String,
    val nameEn: String,
    val emoji: String,
    val roleZh: String,
    val roleEn: String,
    val locationZh: String,
    val locationEn: String,
    val personalityZh: String,
    val personalityEn: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val quotes: List<String>,
    val tuenMunTrivia: String
)

object LingnanCatRepository {

    val CATS = listOf(
        LingnanCat(
            id = "gingey",
            nameZh = "永安黃（阿黃）",
            nameEn = "Wing On Gingey",
            emoji = "🐱",
            roleZh = "永安廣場守護神 · 曬太陽達人",
            roleEn = "Plaza Guardian · Master of Sunbathing",
            locationZh = "永安廣場草地 · Wing On Plaza Lawn",
            locationEn = "Wing On Plaza Lawn",
            personalityZh = "親人、貪食、極度鬆弛，見人就翻肚肚求摸摸",
            personalityEn = "Affectionate, relaxed, always asks for belly rubs",
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFFEF3C7),
            quotes = listOf(
                "今日陽光咁好，唔好成日摺埋啦，出嚟永安廣場伸個懶腰喵～",
                "搭輕鐵記得拍卡啊喵！屯門碼頭去嶺大吹吹風好舒服㗎～",
                "摸完我肚肚，今日 assignment 一定過三爆四喵！",
                "聽講屯門牛入市區要騎牛？我都係留喺永安廣場曬太陽算喵～",
                "讀書攰啦？坐喺草地睇我瞓覺，包你充返電喵～",
                "屯門天氣咁舒服，最啱就係午餐後瞓個養生晏覺喵！"
            ),
            tuenMunTrivia = "永安廣場是嶺大貓咪最愛的日光浴聖地，據說摸過阿黃頭頂的同學，當季 GPA 都不會差！"
        ),
        LingnanCat(
            id = "tuxedo",
            nameZh = "莊月明白手襪（黑白貓）",
            nameEn = "Chuang Tuxedo",
            emoji = "🧦",
            roleZh = "哲學系榮譽旁聽生 · 紙皮箱學者",
            roleEn = "Philosophy Auditor · Box Sitting Scholar",
            locationZh = "莊月明樓一帶 · Chuang Yen Ming Building",
            locationEn = "Chuang Yen Ming Building",
            personalityZh = "冷靜睿智、眼神高冷，只有罐罐才能打破沉默",
            personalityEn = "Calm, intellectual, gives fierce motivational glares",
            primaryColor = Color(0xFF334155),
            secondaryColor = Color(0xFFF1F5F9),
            quotes = listOf(
                "讀書最緊要專心，好似我伏喺紙皮箱咁穩陣，慌咩過唔到三三喵！",
                "今晚去唔去紅橋食宵夜啊？食飽雞翼先有力氣寫 essay 喵～",
                "用我蔑視眼神激勵你：咪再摸魚啦，快啲溫書喵！",
                "屯門公路又塞車？好彩我住莊月明樓唔洗出市區擠巴士喵～",
                "存在主義定貓罐頭？緊係罐罐重要過 present 啦喵！",
                "望咩望？你份 mid-term review 溫好未呀？快去睇 LingUBible 喵！"
            ),
            tuenMunTrivia = "白手襪常年蹲點莊月明樓外，以四隻標誌性白腳套和哲學家般的沉思神態聞名嶺大。"
        ),
        LingnanCat(
            id = "butter",
            nameZh = "Can 仔保安忌廉仔",
            nameEn = "Can Boy Butter",
            emoji = "🥐",
            roleZh = "學生餐廳榮譽保安 · 飯堂巡邏員",
            roleEn = "Canteen Chief Inspector · Food Security",
            locationZh = "學生餐廳門口 · Student Canteen",
            locationEn = "Student Canteen Gate",
            personalityZh = "為食好動、準時巡邏，監督每個嶺南人有無食飽",
            personalityEn = "Foodie, punctual, inspects everyone's lunch packs",
            primaryColor = Color(0xFFD97706),
            secondaryColor = Color(0xFFFDE68A),
            quotes = listOf(
                "今日 Can 仔有咩食？記得食飽先好去 present 喵！",
                "又去置樂食豆腐花？記得買個三文魚罐罐返嚟探我喵～",
                "無論讀書幾忙，早餐午餐都要準時食，貓咪最關心你肚皮喵！",
                "落雨記得帶遮啊，Can 仔外面條樓梯好滑㗎喵～",
                "好香啊～係咪買咗紅橋椒鹽燒賣？分半啖得唔得喵？",
                "食飽飽先有靈感寫 conclusion，快啲坐低開餐喵！"
            ),
            tuenMunTrivia = "忌廉仔每天午餐及晚餐時間必在 Can 仔門前就位，是嶺大同學課堂間最溫暖的治癒守護者。"
        )
    )

    fun getCatById(id: String): LingnanCat {
        return CATS.firstOrNull { it.id == id } ?: CATS[0]
    }
}
