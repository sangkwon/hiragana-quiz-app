package com.example.hiraganapractice.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiraganapractice.data.QuizStatsRepository
import com.example.hiraganapractice.model.ALL_HIRAGANA_DATA_SET
import com.example.hiraganapractice.model.BASIC_HIRAGANA_DATA_SET
import com.example.hiraganapractice.model.KATAKANA_DATA_SET
import com.example.hiraganapractice.model.QuizScope
import com.example.hiraganapractice.model.CumulativeStats
import com.example.hiraganapractice.model.HiraganaItem
import com.example.hiraganapractice.model.Question
import com.example.hiraganapractice.model.QuestionType
import com.example.hiraganapractice.model.ScreenState
import com.example.hiraganapractice.model.UserAnswerRecord
import com.example.hiraganapractice.model.getHiraganaText
import com.example.hiraganapractice.utils.TextToSpeechHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class QuizUiState(
    val screenState: ScreenState = ScreenState.START,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val score: Int = 0,
    val answerRecords: List<UserAnswerRecord> = emptyList(),
    val cumulativeStats: CumulativeStats = CumulativeStats(),
    val questionCount: Int = 20,
    val quizScope: QuizScope = QuizScope.HIRAGANA_ALL
) {
    val currentQuestion: Question?
        get() = questions.getOrNull(currentQuestionIndex)

    val totalQuestions: Int
        get() = questions.size
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuizStatsRepository(application.applicationContext)
    private val ttsHelper = TextToSpeechHelper(application.applicationContext)

    private val _uiState = MutableStateFlow(
        QuizUiState(
            cumulativeStats = repository.getStats(),
            questionCount = repository.getLastQuestionCount(),
            quizScope = repository.getLastQuizScope()
        )
    )
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var autoAdvanceJob: Job? = null

    fun startQuiz(
        questionCount: Int = _uiState.value.questionCount,
        quizScope: QuizScope = _uiState.value.quizScope
    ) {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = null

        repository.saveLastQuestionCount(questionCount)
        repository.saveLastQuizScope(quizScope)

        val generatedQuestions = generateQuestions(questionCount, quizScope)
        _uiState.update { state ->
            state.copy(
                screenState = ScreenState.QUIZ,
                questions = generatedQuestions,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                score = 0,
                answerRecords = emptyList(),
                cumulativeStats = repository.getStats(),
                questionCount = questionCount,
                quizScope = quizScope
            )
        }
    }

    fun selectOption(optionIndex: Int) {
        val currentState = _uiState.value
        if (currentState.isAnswerSubmitted) return

        autoAdvanceJob?.cancel()

        val currentQ = currentState.currentQuestion ?: return
        val isCorrect = (optionIndex == currentQ.correctAnswerIndex)
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        val selectedOptionText = currentQ.options.getOrNull(optionIndex) ?: ""
        val hiraganaToSpeak = getHiraganaText(selectedOptionText)

        // Play Japanese pronunciation for selected option
        ttsHelper.speak(hiraganaToSpeak)

        val newRecord = UserAnswerRecord(
            question = currentQ,
            selectedIndex = optionIndex,
            isCorrect = isCorrect
        )

        _uiState.update { state ->
            state.copy(
                selectedOptionIndex = optionIndex,
                isAnswerSubmitted = true,
                score = newScore,
                answerRecords = state.answerRecords + newRecord
            )
        }

        // Schedule automatic navigation after 1 second
        autoAdvanceJob = viewModelScope.launch {
            delay(AUTO_ADVANCE_DELAY_MS)
            nextQuestion()
        }
    }

    fun speakText(text: String) {
        val hiragana = getHiraganaText(text)
        ttsHelper.speak(hiragana)
    }

    fun nextQuestion() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = null

        val currentState = _uiState.value
        if (currentState.currentQuestionIndex + 1 < currentState.totalQuestions) {
            _uiState.update { state ->
                state.copy(
                    currentQuestionIndex = state.currentQuestionIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerSubmitted = false
                )
            }
        } else {
            // Save quiz result to repository
            repository.saveQuizResult(currentState.answerRecords)
            val updatedStats = repository.getStats()

            _uiState.update { state ->
                state.copy(
                    screenState = ScreenState.RESULT,
                    cumulativeStats = updatedStats
                )
            }
        }
    }

    fun openStatsScreen() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = null

        val latestStats = repository.getStats()
        _uiState.update { state ->
            state.copy(
                screenState = ScreenState.STATS,
                cumulativeStats = latestStats
            )
        }
    }

    fun clearCumulativeStats() {
        repository.clearStats()
        val emptyStats = repository.getStats()
        _uiState.update { state ->
            state.copy(cumulativeStats = emptyStats)
        }
    }

    fun goToStart() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = null

        val latestStats = repository.getStats()
        _uiState.update { state ->
            state.copy(
                screenState = ScreenState.START,
                cumulativeStats = latestStats
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }

    private fun generateQuestions(questionCount: Int, quizScope: QuizScope): List<Question> {
        val dataSet = when (quizScope) {
            QuizScope.HIRAGANA_ALL -> ALL_HIRAGANA_DATA_SET
            QuizScope.HIRAGANA_BASIC -> BASIC_HIRAGANA_DATA_SET
            QuizScope.KATAKANA -> KATAKANA_DATA_SET
        }
        val countToTake = minOf(questionCount, dataSet.size)
        val shuffledHiragana = dataSet.shuffled().take(countToTake)

        return shuffledHiragana.mapIndexed { index, targetItem ->
            val questionType = if (Random.nextBoolean()) {
                QuestionType.HIRAGANA_TO_ROMAJI
            } else {
                QuestionType.ROMAJI_TO_HIRAGANA
            }

            val distractors = generateDistractors(targetItem, questionType, quizScope)

            when (questionType) {
                QuestionType.HIRAGANA_TO_ROMAJI -> {
                    val prompt = targetItem.character
                    val correctAnswer = targetItem.romaji
                    val allOptions = (distractors + correctAnswer).shuffled()
                    val correctIndex = allOptions.indexOf(correctAnswer)

                    Question(
                        id = index + 1,
                        type = questionType,
                        target = targetItem,
                        prompt = prompt,
                        options = allOptions,
                        correctAnswerIndex = correctIndex,
                        correctAnswerText = correctAnswer
                    )
                }
                QuestionType.ROMAJI_TO_HIRAGANA -> {
                    val prompt = targetItem.romaji
                    val correctAnswer = targetItem.character
                    val allOptions = (distractors + correctAnswer).shuffled()
                    val correctIndex = allOptions.indexOf(correctAnswer)

                    Question(
                        id = index + 1,
                        type = questionType,
                        target = targetItem,
                        prompt = prompt,
                        options = allOptions,
                        correctAnswerIndex = correctIndex,
                        correctAnswerText = correctAnswer
                    )
                }
            }
        }
    }

    private fun generateDistractors(target: HiraganaItem, type: QuestionType, quizScope: QuizScope): List<String> {
        val dataSet = when (quizScope) {
            QuizScope.HIRAGANA_ALL -> ALL_HIRAGANA_DATA_SET
            QuizScope.HIRAGANA_BASIC -> BASIC_HIRAGANA_DATA_SET
            QuizScope.KATAKANA -> KATAKANA_DATA_SET
        }
        val candidates = dataSet.filter { it != target }
        val randomCandidates = candidates.shuffled().take(3)

        return when (type) {
            QuestionType.HIRAGANA_TO_ROMAJI -> randomCandidates.map { it.romaji }
            QuestionType.ROMAJI_TO_HIRAGANA -> randomCandidates.map { it.character }
        }
    }

    companion object {
        private const val AUTO_ADVANCE_DELAY_MS = 1000L
    }
}
