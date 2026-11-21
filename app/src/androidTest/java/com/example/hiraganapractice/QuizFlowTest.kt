package com.example.hiraganapractice

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasText
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.hiraganapractice.model.BASIC_HIRAGANA_DATA_SET
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * 시작 → 퀴즈 → 결과 → 통계 화면 흐름을 실제 MainActivity로 검증한다.
 * 정답을 알 수 없으므로 데이터셋(BASIC_HIRAGANA_DATA_SET)에서 정답 보기를 계산해 누른다.
 */
class QuizFlowTest {

    @get:Rule
    val rule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        // 매 테스트마다 저장된 통계/설정을 초기화해 독립성을 보장한다.
        ctx.getSharedPreferences("hiragana_quiz_stats_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario?.close()
    }

    // ---------- helpers ----------

    private fun textOf(tag: String): String =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config
            .getOrNull(SemanticsProperties.Text)!!.joinToString("") { it.text }

    private fun startBasicQuiz(count: Int) {
        rule.onNodeWithText("히라가나 50음도").performClick()
        rule.onNodeWithText("${count}문항").performClick()
        rule.onNodeWithText("퀴즈 시작하기 (${count}문항)").performClick()
        rule.waitUntil(5_000) { hasNode(hasText("문제 1 / $count")) }
    }

    private fun hasNode(matcher: androidx.compose.ui.test.SemanticsMatcher): Boolean =
        rule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    /** 현재 문제에서 정답(correct=true) 또는 오답(false) 보기를 눌러 다음 문제 이동(또는 결과 화면)까지 기다린다. */
    private fun answerCurrent(correct: Boolean, index: Int, total: Int) {
        val prompt = textOf("question_prompt")
        val options = (0..3).map { textOf("option_$it") }

        val correctIdx = options.indexOfFirst { opt ->
            BASIC_HIRAGANA_DATA_SET.any {
                (it.character == prompt && it.romaji == opt) ||
                    (it.romaji == prompt && it.character == opt)
            }
        }
        check(correctIdx >= 0) { "정답 보기를 찾지 못함: prompt=$prompt options=$options" }

        val pick = if (correct) correctIdx else (correctIdx + 1) % 4
        rule.onNodeWithTag("option_$pick").performClick()

        // 1초 후 자동 진행: 다음 문제 번호 또는 결과 화면이 나올 때까지 대기
        if (index + 1 < total) {
            rule.waitUntil(5_000) { hasNode(hasText("문제 ${index + 2} / $total")) }
        } else {
            rule.waitUntil(5_000) { hasNode(hasText("최종 점수")) }
        }
    }

    private fun playQuiz(total: Int, correct: Boolean) {
        for (i in 0 until total) answerCurrent(correct, i, total)
    }

    // ---------- tests ----------

    @Test
    fun startScreen_showsMainControls() {
        rule.onNodeWithText("히라가나 퀴즈 연습").assertIsDisplayed()
        rule.onNodeWithText("연습 범위 선택").assertIsDisplayed()
        rule.onNodeWithText("퀴즈당 문항 수 선택").assertIsDisplayed()
        rule.onNodeWithText("누적 학습 기록 및 통계 보기").assertIsDisplayed()
    }

    @Test
    fun startButton_reflectsSelectedQuestionCount() {
        rule.onNodeWithText("10문항").performClick()
        rule.onNodeWithText("퀴즈 시작하기 (10문항)").assertIsDisplayed()
    }

    @Test
    fun quiz_allCorrect_showsPerfectResult() {
        startBasicQuiz(5)
        playQuiz(5, correct = true)

        rule.onNodeWithText("5 / 5").assertIsDisplayed()
        rule.onNodeWithText("100%").assertIsDisplayed()
    }

    @Test
    fun quiz_allWrong_showsZeroResult() {
        startBasicQuiz(5)
        playQuiz(5, correct = false)

        rule.onNodeWithText("0 / 5").assertIsDisplayed()
        rule.onNodeWithText("0%").assertIsDisplayed()
    }

    @Test
    fun quiz_scoreBadge_incrementsOnCorrectAnswer() {
        startBasicQuiz(5)
        rule.onNodeWithText("정답: 0").assertIsDisplayed()
        answerCurrent(correct = true, index = 0, total = 5)
        rule.onNodeWithText("정답: 1").assertIsDisplayed()
    }

    @Test
    fun result_restart_startsFreshQuiz() {
        startBasicQuiz(5)
        playQuiz(5, correct = true)

        rule.onNodeWithText("다시 도전하기").performClick()
        rule.waitUntil(5_000) { hasNode(hasText("문제 1 / 5")) }
        rule.onNodeWithText("정답: 0").assertIsDisplayed()
    }

    @Test
    fun result_goToStart_returnsToStartScreen() {
        startBasicQuiz(5)
        playQuiz(5, correct = true)

        rule.onNodeWithText("첫 화면").performClick()
        rule.onNodeWithText("히라가나 퀴즈 연습").assertIsDisplayed()
    }

    @Test
    fun result_openStats_andBack() {
        startBasicQuiz(5)
        playQuiz(5, correct = true)

        rule.onNodeWithText("누적 성적").performClick()
        rule.onNodeWithText("누적 학습 통계").assertIsDisplayed()
        rule.onNodeWithText("전체 학습 리포트").assertIsDisplayed()

        rule.onNodeWithText("첫 화면으로 돌아가기").performClick()
        rule.onNodeWithText("히라가나 퀴즈 연습").assertIsDisplayed()
    }

    @Test
    fun stats_clearIcon_hiddenWhenNoRecords() {
        rule.onNodeWithText("누적 학습 기록 및 통계 보기").performClick()
        rule.onNodeWithText("누적 학습 통계").assertIsDisplayed()
        rule.onNodeWithContentDescription("기록 초기화").assertDoesNotExist()
    }

    @Test
    fun stats_clearDialog_cancelKeepsRecords_confirmRemovesThem() {
        startBasicQuiz(5)
        playQuiz(5, correct = true)
        rule.onNodeWithText("누적 성적").performClick()
        rule.onNodeWithText("누적 학습 통계").assertIsDisplayed()

        // 취소: 기록과 아이콘이 그대로 남는다
        rule.onNodeWithContentDescription("기록 초기화").performClick()
        rule.onNodeWithText("누적 기록 초기화").assertIsDisplayed()
        rule.onNodeWithText("취소").performClick()
        rule.onNodeWithText("누적 기록 초기화").assertDoesNotExist()
        rule.onNodeWithContentDescription("기록 초기화").assertIsDisplayed()

        // 확인: 기록이 지워져 아이콘이 사라진다
        rule.onNodeWithContentDescription("기록 초기화").performClick()
        rule.onNodeWithText("초기화").performClick()
        rule.onNodeWithText("누적 기록 초기화").assertDoesNotExist()
        rule.onNodeWithContentDescription("기록 초기화").assertDoesNotExist()
    }

    @Test
    fun lastSelectedSettings_arePersistedAcrossRelaunch() {
        startBasicQuiz(10)
        scenario?.close()
        scenario = ActivityScenario.launch(MainActivity::class.java)

        rule.onNodeWithText("퀴즈 시작하기 (10문항)").assertIsDisplayed()
    }
}
