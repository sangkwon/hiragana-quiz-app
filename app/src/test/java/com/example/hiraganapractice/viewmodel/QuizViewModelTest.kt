package com.example.hiraganapractice.viewmodel

import com.example.hiraganapractice.fake.FakeSpeaker
import com.example.hiraganapractice.fake.FakeStatsRepository
import com.example.hiraganapractice.model.BASIC_HIRAGANA_DATA_SET
import com.example.hiraganapractice.model.KATAKANA_DATA_SET
import com.example.hiraganapractice.model.QuizScope
import com.example.hiraganapractice.model.ScreenState
import com.example.hiraganapractice.model.getHiraganaText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 순수 JVM 단위 테스트. Robolectric 없이, 가짜 저장소/Speaker를 생성자로 주입한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeStatsRepository
    private lateinit var speaker: FakeSpeaker
    private lateinit var vm: QuizViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeStatsRepository()
        speaker = FakeSpeaker()
        vm = QuizViewModel(repository, speaker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val state get() = vm.uiState.value

    private fun wrongIndex(): Int = (state.currentQuestion!!.correctAnswerIndex + 1) % 4

    @Test
    fun `initial state is start screen`() {
        assertEquals(ScreenState.START, state.screenState)
        assertTrue(state.questions.isEmpty())
    }

    @Test
    fun `startQuiz generates requested number of well-formed questions`() {
        vm.startQuiz(10, QuizScope.HIRAGANA_ALL)

        assertEquals(ScreenState.QUIZ, state.screenState)
        assertEquals(10, state.totalQuestions)
        assertEquals(0, state.currentQuestionIndex)
        assertEquals(0, state.score)
        state.questions.forEach { q ->
            assertEquals(4, q.options.size)
            assertEquals(4, q.options.toSet().size)
            assertEquals(q.correctAnswerText, q.options[q.correctAnswerIndex])
        }
    }

    @Test
    fun `question count is capped by data set size`() {
        vm.startQuiz(9999, QuizScope.HIRAGANA_BASIC)
        assertEquals(BASIC_HIRAGANA_DATA_SET.size, state.totalQuestions)
    }

    @Test
    fun `quiz scope limits which characters are asked`() {
        vm.startQuiz(20, QuizScope.KATAKANA)
        val allowed = KATAKANA_DATA_SET.toSet()
        assertTrue(state.questions.all { it.target in allowed })
    }

    @Test
    fun `correct answer increments score and locks the question`() {
        vm.startQuiz(5, QuizScope.HIRAGANA_BASIC)
        vm.selectOption(state.currentQuestion!!.correctAnswerIndex)

        assertEquals(1, state.score)
        assertTrue(state.isAnswerSubmitted)
        assertEquals(1, state.answerRecords.size)
        assertTrue(state.answerRecords.single().isCorrect)

        // a second tap on the same question is ignored
        vm.selectOption(wrongIndex())
        assertEquals(1, state.score)
        assertEquals(1, state.answerRecords.size)
    }

    @Test
    fun `wrong answer does not increment score`() {
        vm.startQuiz(5, QuizScope.HIRAGANA_BASIC)
        vm.selectOption(wrongIndex())

        assertEquals(0, state.score)
        assertFalse(state.answerRecords.single().isCorrect)
    }

    @Test
    fun `answer auto-advances to next question after one second`() {
        vm.startQuiz(5, QuizScope.HIRAGANA_BASIC)
        vm.selectOption(state.currentQuestion!!.correctAnswerIndex)

        dispatcher.scheduler.advanceTimeBy(900)
        dispatcher.scheduler.runCurrent()
        assertEquals(0, state.currentQuestionIndex)

        dispatcher.scheduler.advanceTimeBy(200)
        dispatcher.scheduler.runCurrent()
        assertEquals(1, state.currentQuestionIndex)
        assertFalse(state.isAnswerSubmitted)
        assertEquals(null, state.selectedOptionIndex)
    }

    @Test
    fun `finishing the last question shows result and saves stats`() {
        vm.startQuiz(3, QuizScope.HIRAGANA_BASIC)
        repeat(3) {
            vm.selectOption(state.currentQuestion!!.correctAnswerIndex)
            vm.nextQuestion()
        }

        assertEquals(ScreenState.RESULT, state.screenState)
        assertEquals(3, state.score)
        assertEquals(1, state.cumulativeStats.totalQuizzesCompleted)
        assertEquals(3, state.cumulativeStats.totalQuestionsAnswered)
        assertEquals(3, state.cumulativeStats.totalCorrectAnswers)
    }

    @Test
    fun `restarting resets quiz progress but keeps cumulative stats`() {
        vm.startQuiz(2, QuizScope.HIRAGANA_BASIC)
        repeat(2) {
            vm.selectOption(state.currentQuestion!!.correctAnswerIndex)
            vm.nextQuestion()
        }
        vm.startQuiz()

        assertEquals(ScreenState.QUIZ, state.screenState)
        assertEquals(0, state.score)
        assertEquals(0, state.currentQuestionIndex)
        assertTrue(state.answerRecords.isEmpty())
        assertEquals(1, state.cumulativeStats.totalQuizzesCompleted)
    }

    @Test
    fun `navigation between screens`() {
        vm.openStatsScreen()
        assertEquals(ScreenState.STATS, state.screenState)
        vm.goToStart()
        assertEquals(ScreenState.START, state.screenState)
    }

    @Test
    fun `clearCumulativeStats wipes saved results`() {
        vm.startQuiz(1, QuizScope.HIRAGANA_BASIC)
        vm.selectOption(state.currentQuestion!!.correctAnswerIndex)
        vm.nextQuestion()
        assertEquals(1, state.cumulativeStats.totalQuizzesCompleted)

        vm.clearCumulativeStats()
        assertEquals(0, state.cumulativeStats.totalQuizzesCompleted)
    }

    @Test
    fun `last settings are restored by a new view model`() {
        vm.startQuiz(7, QuizScope.KATAKANA)

        // 같은 저장소를 공유하는 새 ViewModel = 앱을 다시 켠 상황
        val vm2 = QuizViewModel(repository, FakeSpeaker())
        assertEquals(7, vm2.uiState.value.questionCount)
        assertEquals(QuizScope.KATAKANA, vm2.uiState.value.quizScope)
        assertNotEquals(QuizScope.HIRAGANA_ALL, vm2.uiState.value.quizScope)
    }

    // ---- 의존성 주입 덕분에 새로 가능해진 테스트 ----

    @Test
    fun `initial state reads saved settings and stats from repository`() {
        repository.saveLastQuestionCount(15)
        repository.saveLastQuizScope(QuizScope.HIRAGANA_BASIC)

        val fresh = QuizViewModel(repository, speaker).uiState.value
        assertEquals(15, fresh.questionCount)
        assertEquals(QuizScope.HIRAGANA_BASIC, fresh.quizScope)
    }

    @Test
    fun `selecting an option speaks its hiragana`() {
        vm.startQuiz(1, QuizScope.HIRAGANA_BASIC)
        val q = state.currentQuestion!!
        val chosen = q.options[q.correctAnswerIndex]

        vm.selectOption(q.correctAnswerIndex)

        assertEquals(listOf(getHiraganaText(chosen)), speaker.spokenTexts)
        // 이미 답한 문제를 다시 누르면 읽지 않는다
        vm.selectOption(wrongIndex())
        assertEquals(1, speaker.spokenTexts.size)
    }

    @Test
    fun `speakText converts romaji to hiragana before speaking`() {
        vm.speakText("ka")
        assertEquals(listOf("か"), speaker.spokenTexts)
    }

    @Test
    fun `quiz result is saved exactly once when the quiz ends`() {
        vm.startQuiz(2, QuizScope.HIRAGANA_BASIC)
        vm.selectOption(state.currentQuestion!!.correctAnswerIndex)
        vm.nextQuestion()
        assertEquals(0, repository.saveQuizResultCallCount)

        vm.selectOption(wrongIndex())
        vm.nextQuestion()
        assertEquals(1, repository.saveQuizResultCallCount)
        assertEquals(1, repository.getStats().totalCorrectAnswers)
    }
}
