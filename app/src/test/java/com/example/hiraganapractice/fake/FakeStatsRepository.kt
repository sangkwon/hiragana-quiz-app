package com.example.hiraganapractice.fake

import com.example.hiraganapractice.data.StatsRepository
import com.example.hiraganapractice.model.CharacterStat
import com.example.hiraganapractice.model.CumulativeStats
import com.example.hiraganapractice.model.QuizScope
import com.example.hiraganapractice.model.UserAnswerRecord

/**
 * 메모리에만 저장하는 가짜 저장소 (Fake).
 * SharedPreferences가 필요 없어서 Android 없이 순수 JVM에서 동작한다.
 * 실제 저장 동작 자체는 QuizStatsRepositoryTest(Robolectric)가 따로 검증한다.
 */
class FakeStatsRepository : StatsRepository {

    private var quizzesCompleted = 0
    private val records = mutableListOf<UserAnswerRecord>()
    private var lastQuestionCount: Int? = null
    private var lastQuizScope: QuizScope? = null

    /** 테스트에서 "몇 번 저장됐나"를 확인할 때 쓴다. */
    var saveQuizResultCallCount = 0
        private set

    override fun getStats(): CumulativeStats {
        val charStats = records
            .groupBy { it.question.target }
            .map { (item, recs) ->
                CharacterStat(
                    character = item.character,
                    romaji = item.romaji,
                    correctCount = recs.count { it.isCorrect },
                    totalCount = recs.size
                )
            }
        return CumulativeStats(
            totalQuizzesCompleted = quizzesCompleted,
            totalQuestionsAnswered = records.size,
            totalCorrectAnswers = records.count { it.isCorrect },
            characterStatsList = charStats
        )
    }

    override fun saveQuizResult(answerRecords: List<UserAnswerRecord>) {
        saveQuizResultCallCount++
        if (answerRecords.isEmpty()) return
        quizzesCompleted++
        records += answerRecords
    }

    override fun clearStats() {
        quizzesCompleted = 0
        records.clear()
        // 실제 구현과 같게, 마지막 문제 수·범위 설정은 유지한다.
    }

    override fun getLastQuestionCount(defaultCount: Int): Int = lastQuestionCount ?: defaultCount

    override fun saveLastQuestionCount(count: Int) {
        lastQuestionCount = count
    }

    override fun getLastQuizScope(defaultScope: QuizScope): QuizScope = lastQuizScope ?: defaultScope

    override fun saveLastQuizScope(scope: QuizScope) {
        lastQuizScope = scope
    }
}
