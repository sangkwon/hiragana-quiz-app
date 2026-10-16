package com.example.hiraganapractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hiraganapractice.model.ScreenState
import com.example.hiraganapractice.ui.screens.QuizScreen
import com.example.hiraganapractice.ui.screens.ResultScreen
import com.example.hiraganapractice.ui.screens.StartScreen
import com.example.hiraganapractice.ui.screens.StatsScreen
import com.example.hiraganapractice.ui.theme.HiraganaPracticeTheme
import com.example.hiraganapractice.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiraganaPracticeTheme {
                HiraganaQuizApp()
            }
        }
    }
}

@Composable
fun HiraganaQuizApp(
    viewModel: QuizViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (uiState.screenState) {
            ScreenState.START -> {
                StartScreen(
                    onStartQuiz = { viewModel.startQuiz() },
                    onOpenStats = { viewModel.openStatsScreen() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenState.QUIZ -> {
                val currentQuestion = uiState.currentQuestion
                if (currentQuestion != null) {
                    QuizScreen(
                        question = currentQuestion,
                        currentQuestionIndex = uiState.currentQuestionIndex,
                        totalQuestions = uiState.totalQuestions,
                        score = uiState.score,
                        selectedOptionIndex = uiState.selectedOptionIndex,
                        isAnswerSubmitted = uiState.isAnswerSubmitted,
                        onOptionSelected = { index -> viewModel.selectOption(index) },
                        onNextQuestion = { viewModel.nextQuestion() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            ScreenState.RESULT -> {
                ResultScreen(
                    score = uiState.score,
                    totalQuestions = uiState.totalQuestions,
                    answerRecords = uiState.answerRecords,
                    onRestartQuiz = { viewModel.startQuiz() },
                    onOpenStats = { viewModel.openStatsScreen() },
                    onGoToStart = { viewModel.goToStart() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ScreenState.STATS -> {
                StatsScreen(
                    stats = uiState.cumulativeStats,
                    onBackToStart = { viewModel.goToStart() },
                    onClearStats = { viewModel.clearCumulativeStats() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
