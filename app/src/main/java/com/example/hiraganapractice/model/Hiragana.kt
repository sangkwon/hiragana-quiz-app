package com.example.hiraganapractice.model

data class HiraganaItem(
    val character: String,
    val romaji: String,
    val rowName: String // 행 이름 (예: あ행, か행)
)

val HIRAGANA_DATA_SET = listOf(
    // あ행
    HiraganaItem("あ", "a", "あ행"),
    HiraganaItem("い", "i", "あ행"),
    HiraganaItem("う", "u", "あ행"),
    HiraganaItem("え", "e", "あ행"),
    HiraganaItem("お", "o", "あ행"),

    // か행
    HiraganaItem("か", "ka", "か행"),
    HiraganaItem("き", "ki", "か행"),
    HiraganaItem("く", "ku", "か행"),
    HiraganaItem("け", "ke", "か행"),
    HiraganaItem("こ", "ko", "か행"),

    // さ행
    HiraganaItem("さ", "sa", "さ행"),
    HiraganaItem("し", "shi", "さ행"),
    HiraganaItem("す", "su", "さ행"),
    HiraganaItem("せ", "se", "さ행"),
    HiraganaItem("そ", "so", "さ행"),

    // た행
    HiraganaItem("た", "ta", "た행"),
    HiraganaItem("ち", "chi", "た행"),
    HiraganaItem("つ", "tsu", "た행"),
    HiraganaItem("て", "te", "た행"),
    HiraganaItem("と", "to", "た행"),

    // な행
    HiraganaItem("な", "na", "な행"),
    HiraganaItem("に", "ni", "な행"),
    HiraganaItem("ぬ", "nu", "な행"),
    HiraganaItem("ね", "ne", "な행"),
    HiraganaItem("の", "no", "な행"),

    // は행
    HiraganaItem("は", "ha", "は행"),
    HiraganaItem("ひ", "hi", "は행"),
    HiraganaItem("ふ", "fu", "は행"),
    HiraganaItem("へ", "he", "は행"),
    HiraganaItem("ほ", "ho", "は행"),

    // ま행
    HiraganaItem("ま", "ma", "ま행"),
    HiraganaItem("み", "mi", "ま행"),
    HiraganaItem("む", "mu", "ま행"),
    HiraganaItem("め", "me", "ま행"),
    HiraganaItem("も", "mo", "ま행"),

    // や행
    HiraganaItem("や", "ya", "や행"),
    HiraganaItem("ゆ", "yu", "や행"),
    HiraganaItem("よ", "yo", "や행"),

    // ら행
    HiraganaItem("ら", "ra", "ら행"),
    HiraganaItem("り", "ri", "ら행"),
    HiraganaItem("る", "ru", "ら행"),
    HiraganaItem("れ", "re", "ら행"),
    HiraganaItem("ろ", "ro", "ら행"),

    // わ행 & ん
    HiraganaItem("わ", "wa", "わ행"),
    HiraganaItem("を", "wo", "わ행"),
    HiraganaItem("ん", "n", "ん")
)

val ROMAJI_TO_HIRAGANA_MAP: Map<String, String> = HIRAGANA_DATA_SET.associateBy({ it.romaji }, { it.character })

fun getHiraganaText(text: String): String {
    return ROMAJI_TO_HIRAGANA_MAP[text] ?: text
}
