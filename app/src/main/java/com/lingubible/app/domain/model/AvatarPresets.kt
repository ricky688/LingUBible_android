package com.lingubible.app.domain.model

import androidx.compose.ui.graphics.Color

data class AvatarBackground(
    val key: String,
    val nameZh: String,
    val nameEn: String,
    val colors: List<Color>
)

object AvatarPresets {
    // 60 cute animal avatars matching web version
    val CUTE_AVATARS = listOf(
        // Mammals (哺乳動物)
        "🐱", "🐶", "🐰", "🐻", "🐼", "🐨", "🐯", "🦁", "🐵", "🐺",
        "🦊", "🐹", "🐷", "🐮", "🐸", "🐧", "🦝", "🐗", "🐴", "🦄",
        "🐙", "🐢", "🐳", "🐬", "🦭", "🦦", "🦘", "🐘", "🦏", "🦛",
        // Birds (鳥類)
        "🐦", "🦅", "🦆", "🦢", "🦉", "🦚", "🐓", "🦃", "🕊️", "🦜",
        // Marine (海洋生物)
        "🐠", "🐟", "🦈", "🐡", "🦀", "🦞", "🐚", "🪼", "🐋", "🦑",
        // Insects & others (昆蟲與其他)
        "🐝", "🦋", "🐞", "🐛", "🦗", "🕷️", "🐜", "🐌", "🐿️", "🦔"
    )

    // Animal names in Traditional Chinese & English
    private val ANIMAL_NAMES = mapOf(
        "🐱" to Pair("貓咪", "Cat"),
        "🐶" to Pair("小狗", "Dog"),
        "🐰" to Pair("兔子", "Rabbit"),
        "🐻" to Pair("熊熊", "Bear"),
        "🐼" to Pair("熊貓", "Panda"),
        "🐨" to Pair("無尾熊", "Koala"),
        "🐯" to Pair("老虎", "Tiger"),
        "🦁" to Pair("獅子", "Lion"),
        "🐵" to Pair("猴子", "Monkey"),
        "🐺" to Pair("狼", "Wolf"),
        "🦊" to Pair("狐狸", "Fox"),
        "🐹" to Pair("倉鼠", "Hamster"),
        "🐷" to Pair("小豬", "Pig"),
        "🐮" to Pair("牛牛", "Cow"),
        "🐸" to Pair("青蛙", "Frog"),
        "🐧" to Pair("企鵝", "Penguin"),
        "🦝" to Pair("浣熊", "Raccoon"),
        "🐗" to Pair("野豬", "Boar"),
        "🐴" to Pair("馬兒", "Horse"),
        "🦄" to Pair("獨角獸", "Unicorn"),
        "🐙" to Pair("章魚", "Octopus"),
        "🐢" to Pair("烏龜", "Turtle"),
        "🐳" to Pair("鯨魚", "Whale"),
        "🐬" to Pair("海豚", "Dolphin"),
        "🦭" to Pair("海豹", "Seal"),
        "🦦" to Pair("水獺", "Otter"),
        "🦘" to Pair("袋鼠", "Kangaroo"),
        "🐘" to Pair("大象", "Elephant"),
        "🦏" to Pair("犀牛", "Rhino"),
        "🦛" to Pair("河馬", "Hippo"),
        "🐦" to Pair("小鳥", "Bird"),
        "🦅" to Pair("老鷹", "Eagle"),
        "🦆" to Pair("鴨子", "Duck"),
        "🦢" to Pair("天鵝", "Swan"),
        "🦉" to Pair("貓頭鷹", "Owl"),
        "🦚" to Pair("孔雀", "Peacock"),
        "🐓" to Pair("公雞", "Rooster"),
        "🦃" to Pair("火雞", "Turkey"),
        "🕊️" to Pair("鴿子", "Dove"),
        "🦜" to Pair("鸚鵡", "Parrot"),
        "🐠" to Pair("熱帶魚", "Tropical Fish"),
        "🐟" to Pair("魚兒", "Fish"),
        "🦈" to Pair("鯊魚", "Shark"),
        "🐡" to Pair("河豚", "Blowfish"),
        "🦀" to Pair("螃蟹", "Crab"),
        "🦞" to Pair("龍蝦", "Lobster"),
        "🐚" to Pair("貝殼", "Shell"),
        "🪼" to Pair("水母", "Jellyfish"),
        "🐋" to Pair("藍鯨", "Blue Whale"),
        "🦑" to Pair("魷魚", "Squid"),
        "🐝" to Pair("蜜蜂", "Bee"),
        "🦋" to Pair("蝴蝶", "Butterfly"),
        "🐞" to Pair("瓢蟲", "Ladybug"),
        "🐛" to Pair("毛毛蟲", "Caterpillar"),
        "🦗" to Pair("蟋蟀", "Cricket"),
        "🕷️" to Pair("蜘蛛", "Spider"),
        "🐜" to Pair("螞蟻", "Ant"),
        "🐌" to Pair("蝸牛", "Snail"),
        "🐿️" to Pair("松鼠", "Chipmunk"),
        "🦔" to Pair("刺蝟", "Hedgehog")
    )

    // 60 background color gradients matching web BACKGROUND_COLORS
    val BACKGROUND_COLORS = listOf(
        // Light warm palettes (0 - 9)
        AvatarBackground("sunset", "夕陽橙", "Sunset", listOf(Color(0xFFFFEDD5), Color(0xFFFCE7F3))),
        AvatarBackground("peach", "蜜桃粉", "Peach", listOf(Color(0xFFFED7AA), Color(0xFFFFEDD5))),
        AvatarBackground("coral", "珊瑚橙", "Coral", listOf(Color(0xFFFEE2E2), Color(0xFFFCE7F3))),
        AvatarBackground("rose", "玫瑰粉", "Rose", listOf(Color(0xFFFCE7F3), Color(0xFFFFE4E6))),
        AvatarBackground("apricot", "杏桃橙", "Apricot", listOf(Color(0xFFFFF7ED), Color(0xFFFEF3C7))),
        AvatarBackground("cream", "奶油黃", "Cream", listOf(Color(0xFFFEFCE8), Color(0xFFFFF7ED))),
        AvatarBackground("vanilla", "香草白", "Vanilla", listOf(Color(0xFFFFFBEB), Color(0xFFFEF9C3))),
        AvatarBackground("blush", "胭脂粉", "Blush", listOf(Color(0xFFFFF1F2), Color(0xFFFCE7F3))),
        AvatarBackground("cherry", "櫻桃紅", "Cherry", listOf(Color(0xFFFEE2E2), Color(0xFFFFE4E6))),
        AvatarBackground("salmon", "鮭魚粉", "Salmon", listOf(Color(0xFFFCE7F3), Color(0xFFFFEDD5))),

        // Light cool palettes (10 - 19)
        AvatarBackground("ocean", "海洋藍", "Ocean", listOf(Color(0xFFDBEAFE), Color(0xFFCFFAFE))),
        AvatarBackground("sky", "天空藍", "Sky", listOf(Color(0xFFE0F2FE), Color(0xFFDBEAFE))),
        AvatarBackground("mint", "薄荷綠", "Mint", listOf(Color(0xFFDCFCE7), Color(0xFFD1FAE5))),
        AvatarBackground("forest", "森林綠", "Forest", listOf(Color(0xFFD1FAE5), Color(0xFFCCFBF1))),
        AvatarBackground("powder", "粉餅藍", "Powder", listOf(Color(0xFFEFF6FF), Color(0xFFE0F2FE))),
        AvatarBackground("seafoam", "海泡綠", "Seafoam", listOf(Color(0xFFF0FDFA), Color(0xFFDCFCE7))),
        AvatarBackground("aqua", "水晶藍", "Aqua", listOf(Color(0xFFECFEFF), Color(0xFFDBEAFE))),
        AvatarBackground("jade", "翡翠綠", "Jade", listOf(Color(0xFFF0FDF4), Color(0xFFD1FAE5))),
        AvatarBackground("turquoise", "土耳藍", "Turquoise", listOf(Color(0xFFCCFBF1), Color(0xFFCFFAFE))),
        AvatarBackground("sage", "鼠尾綠", "Sage", listOf(Color(0xFFECFDF5), Color(0xFFCCFBF1))),

        // Light purple palettes (20 - 24)
        AvatarBackground("lavender", "薰衣紫", "Lavender", listOf(Color(0xFFF3E8FF), Color(0xFFFCE7F3))),
        AvatarBackground("grape", "葡萄紫", "Grape", listOf(Color(0xFFEDE9FE), Color(0xFFF3E8FF))),
        AvatarBackground("lilac", "丁香紫", "Lilac", listOf(Color(0xFFFAF5FF), Color(0xFFEDE9FE))),
        AvatarBackground("orchid", "蘭花紫", "Orchid", listOf(Color(0xFFFCE7F3), Color(0xFFF3E8FF))),
        AvatarBackground("mauve", "木槿紫", "Mauve", listOf(Color(0xFFF5F3FF), Color(0xFFF3E8FF))),

        // Light neutral palettes (25 - 29)
        AvatarBackground("pearl", "珍珠白", "Pearl", listOf(Color(0xFFF9FAFB), Color(0xFFF1F5F9))),
        AvatarBackground("ivory", "象牙白", "Ivory", listOf(Color(0xFFFAFAF9), Color(0xFFF5F5F5))),
        AvatarBackground("sand", "沙灘金", "Sand", listOf(Color(0xFFFFFBEB), Color(0xFFF5F5F4))),
        AvatarBackground("linen", "亞麻灰", "Linen", listOf(Color(0xFFFAFAFA), Color(0xFFF5F5F4))),
        AvatarBackground("opal", "蛋白彩", "Opal", listOf(Color(0xFFF8FAFC), Color(0xFFF3F4F6))),

        // Deep warm palettes (30 - 35)
        AvatarBackground("burgundy", "酒窩紅", "Burgundy", listOf(Color(0xFF991B1B), Color(0xFF9F1239))),
        AvatarBackground("maroon", "栗子紅", "Maroon", listOf(Color(0xFFB91C1C), Color(0xFF991B1B))),
        AvatarBackground("crimson", "緋子紅", "Crimson", listOf(Color(0xFFBE123C), Color(0xFF9D174D))),
        AvatarBackground("rust", "鐵鏽紅", "Rust", listOf(Color(0xFFC2410C), Color(0xFFB91C1C))),
        AvatarBackground("copper", "古銅橙", "Copper", listOf(Color(0xFFB45309), Color(0xFF9A3412))),
        AvatarBackground("bronze", "青銅黃", "Bronze", listOf(Color(0xFFA16207), Color(0xFF92400E))),

        // Deep cool palettes (36 - 45)
        AvatarBackground("navy", "海軍藍", "Navy", listOf(Color(0xFF1E40AF), Color(0xFF3730A3))),
        AvatarBackground("midnight", "午夜藍", "Midnight", listOf(Color(0xFF1E293B), Color(0xFF1E3A8A))),
        AvatarBackground("steel", "鋼鐵灰", "Steel", listOf(Color(0xFF334155), Color(0xFF1F2937))),
        AvatarBackground("emerald", "祖母綠", "Emerald", listOf(Color(0xFF15803D), Color(0xFF065F46))),
        AvatarBackground("pine", "松樹綠", "Pine", listOf(Color(0xFF166534), Color(0xFF115E59))),
        AvatarBackground("teal", "青石藍", "Teal", listOf(Color(0xFF0F766E), Color(0xFF155E75))),
        AvatarBackground("cobalt", "鈷石藍", "Cobalt", listOf(Color(0xFF1D4ED8), Color(0xFF075985))),
        AvatarBackground("sapphire", "寶石藍", "Sapphire", listOf(Color(0xFF0369A1), Color(0xFF1E40AF))),
        AvatarBackground("arctic", "北極藍", "Arctic", listOf(Color(0xFF0E7490), Color(0xFF1E40AF))),
        AvatarBackground("deep_sea", "深海藍", "Deep Sea", listOf(Color(0xFF115E59), Color(0xFF1E3A8A))),

        // Deep purple palettes (46 - 50)
        AvatarBackground("plum", "梅子紫", "Plum", listOf(Color(0xFF3730A3), Color(0xFF6B21A8))),
        AvatarBackground("eggplant", "茄子紫", "Eggplant", listOf(Color(0xFF6B21A8), Color(0xFF5B21B6))),
        AvatarBackground("amethyst", "水晶紫", "Amethyst", listOf(Color(0xFF6D28D9), Color(0xFF6B21A8))),
        AvatarBackground("indigo", "靛藍紫", "Indigo", listOf(Color(0xFF4338CA), Color(0xFF5B21B6))),
        AvatarBackground("royal", "皇家紫", "Royal", listOf(Color(0xFF1E40AF), Color(0xFF6B21A8))),

        // Deep neutral palettes (51 - 54)
        AvatarBackground("charcoal", "木炭黑", "Charcoal", listOf(Color(0xFF1F2937), Color(0xFF1E293B))),
        AvatarBackground("graphite", "石墨灰", "Graphite", listOf(Color(0xFF292524), Color(0xFF1F2937))),
        AvatarBackground("obsidian", "曜石黑", "Obsidian", listOf(Color(0xFF1E293B), Color(0xFF1C1917))),
        AvatarBackground("onyx", "瑪瑙黑", "Onyx", listOf(Color(0xFF262626), Color(0xFF292524))),

        // Special gradients (55 - 59)
        AvatarBackground("rainbow", "彩虹漸", "Rainbow", listOf(Color(0xFFB91C1C), Color(0xFFCA8A04), Color(0xFF1E40AF))),
        AvatarBackground("aurora", "極光漸", "Aurora", listOf(Color(0xFF15803D), Color(0xFF1E40AF), Color(0xFF6B21A8))),
        AvatarBackground("cosmic", "宇宙漸", "Cosmic", listOf(Color(0xFF3730A3), Color(0xFF6B21A8), Color(0xFFBE185D))),
        AvatarBackground("tropical", "熱帶漸", "Tropical", listOf(Color(0xFF0E7490), Color(0xFF115E59), Color(0xFF166534))),
        AvatarBackground("fire", "火焰漸", "Fire", listOf(Color(0xFFB91C1C), Color(0xFFC2410C), Color(0xFFCA8A04)))
    )

    /**
     * Consistent 32-bit hash matching web getConsistentRandomIndex
     */
    fun getConsistentRandomIndex(seed: String, arrayLength: Int): Int {
        if (arrayLength <= 0) return 0
        var hash = 0
        for (ch in seed) {
            val code = ch.code
            hash = ((hash shl 5) - hash) + code
        }
        val safeHash = kotlin.math.abs(hash.toLong())
        return (safeHash % arrayLength).toInt()
    }

    /**
     * Compute deterministic default avatar for a user based on their ID
     */
    fun getDefaultAvatar(userId: String): CustomAvatar {
        if (userId.isBlank()) {
            return CustomAvatar(animal = "🐢", backgroundIndex = 57) // Default cute turtle with cosmic gradient
        }
        val animalIndex = getConsistentRandomIndex(userId, CUTE_AVATARS.size)
        val bgIndex = getConsistentRandomIndex("${userId}_bg", BACKGROUND_COLORS.size)
        return CustomAvatar(
            animal = CUTE_AVATARS[animalIndex],
            backgroundIndex = bgIndex
        )
    }

    fun getAnimalName(animal: String, isZh: Boolean): String {
        val pair = ANIMAL_NAMES[animal] ?: return animal
        return if (isZh) pair.first else pair.second
    }

    fun getBackground(index: Int): AvatarBackground {
        return if (index in BACKGROUND_COLORS.indices) BACKGROUND_COLORS[index] else BACKGROUND_COLORS[0]
    }

    fun getBackgroundName(index: Int, isZh: Boolean): String {
        val bg = getBackground(index)
        return if (isZh) bg.nameZh else bg.nameEn
    }
}
