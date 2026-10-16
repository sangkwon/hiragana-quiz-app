package com.example.hiraganapractice.model

enum class QuestionType {
    HIRAGANA_TO_ROMAJI, // 유형 1: 히라가나 제시 -> 로마자 발음 선택
    ROMAJI_TO_HIRAGANA  // 유형 2: 로마자 발음 제시 -> 히라가나 글자 선택
}

data class Question(
    val id: Int,
    val type: QuestionType,
    val target: HiraganaItem,
    val prompt: String,            // 문제 제시어 (히라가나 또는 로마자)
    val options: List<String>,     // 4개의 보기
    val correctAnswerIndex: Int,   // 정답 보기 인덱스 (0~3)
    val correctAnswerText: String  // 정답 문자열
)

enum class ScreenState {
    START,
    QUIZ,
    RESULT,
    STATS
}

data class UserAnswerRecord(
    val question: Question,
    val selectedIndex: Int,
    val isCorrect: Boolean
)

data class CharacterStat(
    val character: String,
    val romaji: String,
    val correctCount: Int,
    val totalCount: Int
) {
    val accuracy: Float
        get() = if (totalCount > 0) (correctCount.toFloat() / totalCount) * 100f else 0f
}

data class CumulativeStats(
    val totalQuizzesCompleted: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val characterStatsList: List<CharacterStat> = emptyList()
) {
    val overallAccuracy: Float
        get() = if (totalQuestionsAnswered > 0) (totalCorrectAnswers.toFloat() / totalQuestionsAnswered) * 100f else 0f
}
