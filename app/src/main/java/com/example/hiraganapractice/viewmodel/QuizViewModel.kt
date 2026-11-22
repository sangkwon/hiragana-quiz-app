package com.example.hiraganapractice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hiraganapractice.data.QuizStatsRepository
import com.example.hiraganapractice.data.StatsRepository
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
import com.example.hiraganapractice.utils.Speaker
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

/**
 * 의존성(저장소, 음성)을 직접 만들지 않고 생성자로 "주입"받는다.
 * - 앱에서는 [Factory]가 실제 구현(SharedPreferences, TextToSpeech)을 넣어 준다.
 * - 테스트에서는 가짜 구현을 넣어, Android/Robolectric 없이 순수 JVM에서 돌린다.
 */
class QuizViewModel(
    private val repository: StatsRepository,
    private val ttsHelper: Speaker
) : ViewModel() {

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
        val dataSet = dataSetFor(quizScope)
        val countToTake = minOf(questionCount, dataSet.size)
        val shuffledHiragana = dataSet.shuffled().take(countToTake)

        return shuffledHiragana.mapIndexed { index, targetItem ->
            val questionType = if (Random.nextBoolean()) {
                QuestionType.HIRAGANA_TO_ROMAJI
            } else {
                QuestionType.ROMAJI_TO_HIRAGANA
            }

            val distractors = generateDistractors(targetItem, questionType, dataSet)

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

    companion object {
        private const val AUTO_ADVANCE_DELAY_MS = 1000L

        /**
         * ViewModel에 생성자 인자가 생기면 시스템이 스스로 만들 수 없으므로,
         * "어떻게 만드는지"를 알려 주는 Factory가 필요하다.
         * Application은 ViewModel을 만드는 순간 CreationExtras에서 꺼내 쓴다.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                QuizViewModel(
                    repository = QuizStatsRepository(app),
                    ttsHelper = TextToSpeechHelper(app)
                )
            }
        }
    }
}

internal fun dataSetFor(quizScope: QuizScope): List<HiraganaItem> = when (quizScope) {
    QuizScope.HIRAGANA_ALL -> ALL_HIRAGANA_DATA_SET
    QuizScope.HIRAGANA_BASIC -> BASIC_HIRAGANA_DATA_SET
    QuizScope.KATAKANA -> KATAKANA_DATA_SET
}

/**
 * 정답(target)을 제외한 오답 보기 3개를 만든다.
 * random을 인자로 받아, 테스트에서 시드를 고정해 결과를 재현할 수 있게 한다.
 */
internal fun generateDistractors(
    target: HiraganaItem,
    type: QuestionType,
    dataSet: List<HiraganaItem>,
    random: Random = Random.Default
): List<String> {
    // じ/ぢ("ji"), ず/づ("zu")처럼 정답과 발음이 같은 글자는 오답이 될 수 없으므로 후보에서 뺀다.
    val candidates = dataSet.filter { it.romaji != target.romaji }

    // 오답끼리도 보기 문자열이 겹치지 않도록 중복을 없앤 뒤 3개를 고른다.
    val options = when (type) {
        QuestionType.HIRAGANA_TO_ROMAJI -> candidates.map { it.romaji }
        QuestionType.ROMAJI_TO_HIRAGANA -> candidates.map { it.character }
    }
    return options.distinct().shuffled(random).take(3)
}
