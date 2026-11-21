package com.example.hiraganapractice.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.hiraganapractice.model.BASIC_HIRAGANA_DATA_SET
import com.example.hiraganapractice.model.Question
import com.example.hiraganapractice.model.QuestionType
import com.example.hiraganapractice.model.QuizScope
import com.example.hiraganapractice.model.UserAnswerRecord
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QuizStatsRepositoryTest {

    private lateinit var repo: QuizStatsRepository

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        repo = QuizStatsRepository(ctx)
        repo.clearStats()
    }

    private fun record(index: Int, correct: Boolean): UserAnswerRecord {
        val item = BASIC_HIRAGANA_DATA_SET[index]
        val q = Question(
            id = 1, type = QuestionType.HIRAGANA_TO_ROMAJI, target = item,
            prompt = item.character, options = listOf(item.romaji, "x", "y", "z"),
            correctAnswerIndex = 0, correctAnswerText = item.romaji
        )
        return UserAnswerRecord(q, if (correct) 0 else 1, correct)
    }

    @Test
    fun `initial stats are empty`() {
        val s = repo.getStats()
        assertEquals(0, s.totalQuizzesCompleted)
        assertEquals(0, s.totalQuestionsAnswered)
        assertEquals(0, s.totalCorrectAnswers)
        assertEquals(0f, s.overallAccuracy, 0f)
    }

    @Test
    fun `saveQuizResult accumulates totals`() {
        repo.saveQuizResult(listOf(record(0, true), record(1, false)))
        repo.saveQuizResult(listOf(record(0, true)))

        val s = repo.getStats()
        assertEquals(2, s.totalQuizzesCompleted)
        assertEquals(3, s.totalQuestionsAnswered)
        assertEquals(2, s.totalCorrectAnswers)
    }

    @Test
    fun `saveQuizResult tracks per-character counts`() {
        val a = BASIC_HIRAGANA_DATA_SET[0].character
        val i = BASIC_HIRAGANA_DATA_SET[1].character
        repo.saveQuizResult(listOf(record(0, true), record(1, false)))
        repo.saveQuizResult(listOf(record(0, false)))

        val stats = repo.getStats().characterStatsList.associateBy { it.character }
        assertEquals(2, stats.getValue(a).totalCount)
        assertEquals(1, stats.getValue(a).correctCount)
        assertEquals(50f, stats.getValue(a).accuracy, 0.01f)
        assertEquals(1, stats.getValue(i).totalCount)
        assertEquals(0, stats.getValue(i).correctCount)
    }

    @Test
    fun `saving empty result does not count as a quiz`() {
        repo.saveQuizResult(emptyList())
        assertEquals(0, repo.getStats().totalQuizzesCompleted)
    }

    @Test
    fun `clearStats resets everything`() {
        repo.saveQuizResult(listOf(record(0, true)))
        repo.clearStats()
        assertEquals(0, repo.getStats().totalQuizzesCompleted)
        assertEquals(0, repo.getStats().characterStatsList.sumOf { it.totalCount })
    }

    @Test
    fun `last question count and scope persist with defaults`() {
        assertEquals(20, repo.getLastQuestionCount())
        assertEquals(QuizScope.HIRAGANA_ALL, repo.getLastQuizScope())

        repo.saveLastQuestionCount(10)
        repo.saveLastQuizScope(QuizScope.KATAKANA)

        assertEquals(10, repo.getLastQuestionCount())
        assertEquals(QuizScope.KATAKANA, repo.getLastQuizScope())
    }
}
