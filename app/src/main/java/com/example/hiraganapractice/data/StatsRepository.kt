package com.example.hiraganapractice.data

import com.example.hiraganapractice.model.CumulativeStats
import com.example.hiraganapractice.model.QuizScope
import com.example.hiraganapractice.model.UserAnswerRecord

/**
 * 퀴즈 통계/설정 저장소의 "약속(계약)".
 *
 * ViewModel은 이 인터페이스만 알고, 실제로 SharedPreferences에 저장하는지(QuizStatsRepository),
 * 메모리에만 들고 있는지(테스트용 FakeStatsRepository)는 모른다.
 * 그래서 테스트에서 Android 없이 가짜 구현을 끼워 넣을 수 있다.
 */
interface StatsRepository {
    fun getStats(): CumulativeStats
    fun saveQuizResult(answerRecords: List<UserAnswerRecord>)
    /** 누적 통계만 지운다. 마지막 문제 수·범위 설정은 유지한다. */
    fun clearStats()
    fun getLastQuestionCount(defaultCount: Int = 20): Int
    fun saveLastQuestionCount(count: Int)
    fun getLastQuizScope(defaultScope: QuizScope = QuizScope.HIRAGANA_ALL): QuizScope
    fun saveLastQuizScope(scope: QuizScope)
}
