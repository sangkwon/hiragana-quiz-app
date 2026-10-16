package com.example.hiraganapractice.data

import android.content.Context
import android.content.SharedPreferences
import com.example.hiraganapractice.model.HIRAGANA_DATA_SET
import com.example.hiraganapractice.model.CharacterStat
import com.example.hiraganapractice.model.CumulativeStats
import com.example.hiraganapractice.model.UserAnswerRecord

class QuizStatsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getStats(): CumulativeStats {
        val quizzesCompleted = prefs.getInt(KEY_TOTAL_QUIZZES, 0)
        val questionsAnswered = prefs.getInt(KEY_TOTAL_QUESTIONS, 0)
        val correctAnswers = prefs.getInt(KEY_TOTAL_CORRECT, 0)

        val charStats = HIRAGANA_DATA_SET.map { item ->
            val total = prefs.getInt(KEY_CHAR_TOTAL_PREFIX + item.character, 0)
            val correct = prefs.getInt(KEY_CHAR_CORRECT_PREFIX + item.character, 0)
            CharacterStat(
                character = item.character,
                romaji = item.romaji,
                correctCount = correct,
                totalCount = total
            )
        }

        return CumulativeStats(
            totalQuizzesCompleted = quizzesCompleted,
            totalQuestionsAnswered = questionsAnswered,
            totalCorrectAnswers = correctAnswers,
            characterStatsList = charStats
        )
    }

    fun saveQuizResult(answerRecords: List<UserAnswerRecord>) {
        if (answerRecords.isEmpty()) return

        val currentQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0)
        val currentQuestions = prefs.getInt(KEY_TOTAL_QUESTIONS, 0)
        val currentCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0)

        val addedQuestions = answerRecords.size
        val addedCorrect = answerRecords.count { it.isCorrect }

        val editor = prefs.edit()
        editor.putInt(KEY_TOTAL_QUIZZES, currentQuizzes + 1)
        editor.putInt(KEY_TOTAL_QUESTIONS, currentQuestions + addedQuestions)
        editor.putInt(KEY_TOTAL_CORRECT, currentCorrect + addedCorrect)

        for (record in answerRecords) {
            val charKey = record.question.target.character
            val prevTotal = prefs.getInt(KEY_CHAR_TOTAL_PREFIX + charKey, 0)
            val prevCorrect = prefs.getInt(KEY_CHAR_CORRECT_PREFIX + charKey, 0)

            editor.putInt(KEY_CHAR_TOTAL_PREFIX + charKey, prevTotal + 1)
            if (record.isCorrect) {
                editor.putInt(KEY_CHAR_CORRECT_PREFIX + charKey, prevCorrect + 1)
            }
        }

        editor.apply()
    }

    fun clearStats() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "hiragana_quiz_stats_prefs"
        private const val KEY_TOTAL_QUIZZES = "key_total_quizzes"
        private const val KEY_TOTAL_QUESTIONS = "key_total_questions"
        private const val KEY_TOTAL_CORRECT = "key_total_correct"
        private const val KEY_CHAR_TOTAL_PREFIX = "key_char_total_"
        private const val KEY_CHAR_CORRECT_PREFIX = "key_char_correct_"
    }
}
