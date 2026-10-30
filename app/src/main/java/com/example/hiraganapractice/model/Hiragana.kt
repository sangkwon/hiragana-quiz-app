package com.example.hiraganapractice.model

data class HiraganaItem(
    val character: String,
    val romaji: String,
    val rowName: String // 행 이름 (예: あ행, か행, が행, 요음, 촉음)
)

val BASIC_HIRAGANA_DATA_SET = listOf(
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

val DAKUTEN_HIRAGANA_DATA_SET = listOf(
    // が행 (탁음)
    HiraganaItem("が", "ga", "が행"),
    HiraganaItem("ぎ", "gi", "が행"),
    HiraganaItem("ぐ", "gu", "が행"),
    HiraganaItem("げ", "ge", "が행"),
    HiraganaItem("ご", "go", "が행"),

    // ざ행 (탁음)
    HiraganaItem("ざ", "za", "ざ행"),
    HiraganaItem("じ", "ji", "ざ행"),
    HiraganaItem("ず", "zu", "ざ행"),
    HiraganaItem("ぜ", "ze", "ざ행"),
    HiraganaItem("ぞ", "zo", "ざ행"),

    // だ행 (탁음)
    HiraganaItem("だ", "da", "だ행"),
    HiraganaItem("ぢ", "ji", "だ행"),
    HiraganaItem("づ", "zu", "だ행"),
    HiraganaItem("で", "de", "だ행"),
    HiraganaItem("ど", "do", "だ행"),

    // ば행 (탁음)
    HiraganaItem("ば", "ba", "ば행"),
    HiraganaItem("び", "bi", "ば행"),
    HiraganaItem("ぶ", "bu", "ば행"),
    HiraganaItem("べ", "be", "ば행"),
    HiraganaItem("ぼ", "bo", "ば행"),

    // ぱ행 (반탁음)
    HiraganaItem("ぱ", "pa", "ぱ행"),
    HiraganaItem("ぴ", "pi", "ぱ행"),
    HiraganaItem("ぷ", "pu", "ぱ행"),
    HiraganaItem("ぺ", "pe", "ぱ행"),
    HiraganaItem("ぽ", "po", "ぱ행")
)

val YOON_AND_SOKUON_DATA_SET = listOf(
    // 촉음
    HiraganaItem("っ", "tsu", "촉음"),

    // 요음 (Yōon)
    HiraganaItem("きゃ", "kya", "요음"),
    HiraganaItem("きゅ", "kyu", "요음"),
    HiraganaItem("きょ", "kyo", "요음"),

    HiraganaItem("しゃ", "sha", "요음"),
    HiraganaItem("しゅ", "shu", "요음"),
    HiraganaItem("しょ", "sho", "요음"),

    HiraganaItem("ちゃ", "cha", "요음"),
    HiraganaItem("ちゅ", "chu", "요음"),
    HiraganaItem("ちょ", "cho", "요음"),

    HiraganaItem("にゃ", "nya", "요음"),
    HiraganaItem("にゅ", "nyu", "요음"),
    HiraganaItem("にょ", "nyo", "요음"),

    HiraganaItem("ひゃ", "hya", "요음"),
    HiraganaItem("ひゅ", "hyu", "요음"),
    HiraganaItem("ひょ", "hyo", "요음"),

    HiraganaItem("みゃ", "mya", "요음"),
    HiraganaItem("みゅ", "myu", "요음"),
    HiraganaItem("みょ", "myo", "요음"),

    HiraganaItem("りゃ", "rya", "요음"),
    HiraganaItem("りゅ", "ryu", "요음"),
    HiraganaItem("りょ", "ryo", "요음"),

    HiraganaItem("ぎゃ", "gya", "요음"),
    HiraganaItem("ぎゅ", "gyu", "요음"),
    HiraganaItem("ぎょ", "gyo", "요음"),

    HiraganaItem("じゃ", "ja", "요음"),
    HiraganaItem("じゅ", "ju", "요음"),
    HiraganaItem("じょ", "jo", "요음"),

    HiraganaItem("びゃ", "bya", "요음"),
    HiraganaItem("びゅ", "byu", "요음"),
    HiraganaItem("びょ", "byo", "요음"),

    HiraganaItem("ぴゃ", "pya", "요음"),
    HiraganaItem("ぴゅ", "pyu", "요음"),
    HiraganaItem("ぴょ", "pyo", "요음")
)

val ALL_HIRAGANA_DATA_SET = BASIC_HIRAGANA_DATA_SET + DAKUTEN_HIRAGANA_DATA_SET + YOON_AND_SOKUON_DATA_SET

val HIRAGANA_DATA_SET = ALL_HIRAGANA_DATA_SET

fun getHiraganaText(text: String): String {
    return ALL_HIRAGANA_DATA_SET.find { it.romaji.lowercase() == text.lowercase() }?.character
        ?: ALL_HIRAGANA_DATA_SET.find { it.character == text }?.character
        ?: text
}
