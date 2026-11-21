package com.example.hiraganapractice.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.hiraganapractice.data.QuizStatsRepository
import com.example.hiraganapractice.model.BASIC_HIRAGANA_DATA_SET
import com.example.hiraganapractice.model.KATAKANA_DATA_SET
import com.example.hiraganapractice.model.QuizScope
import com.example.hiraganapractice.model.ScreenState
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class QuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var app: Application
    private lateinit var vm: QuizViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        app = ApplicationProvider.getApplicationContext()
        QuizStatsRepository(app).clearStats()
        vm = QuizViewModel(app)
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

        val vm2 = QuizViewModel(app)
        assertEquals(7, vm2.uiState.value.questionCount)
        assertEquals(QuizScope.KATAKANA, vm2.uiState.value.quizScope)
        assertNotEquals(QuizScope.HIRAGANA_ALL, vm2.uiState.value.quizScope)
    }
}
