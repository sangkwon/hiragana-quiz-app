package com.example.hiraganapractice.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiraganapractice.model.Question
import com.example.hiraganapractice.model.QuestionType
import com.example.hiraganapractice.ui.theme.CorrectGreen
import com.example.hiraganapractice.ui.theme.CorrectGreenContainer
import com.example.hiraganapractice.ui.theme.IncorrectRed
import com.example.hiraganapractice.ui.theme.IncorrectRedContainer

@Composable
fun QuizScreen(
    question: Question,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    score: Int,
    selectedOptionIndex: Int?,
    isAnswerSubmitted: Boolean,
    onOptionSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (currentQuestionIndex + 1).toFloat() / totalQuestions

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Progress & Counter
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "문제 ${currentQuestionIndex + 1} / $totalQuestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Score indicator badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "정답: $score",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Main Content Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Question Type Badge
                val typeText = when (question.type) {
                    QuestionType.HIRAGANA_TO_ROMAJI -> "유형 1: 히라가나 ➔ 발음(로마자) 맞추기"
                    QuestionType.ROMAJI_TO_HIRAGANA -> "유형 2: 발음(로마자) ➔ 히라가나 맞추기"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Question Prompt Display Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = question.prompt,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (question.type == QuestionType.HIRAGANA_TO_ROMAJI) "이 글자의 발음은?" else "이 발음의 글자는?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options List (4 choices)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    question.options.forEachIndexed { index, option ->
                        OptionCard(
                            optionText = option,
                            isSelected = selectedOptionIndex == index,
                            isCorrectOption = index == question.correctAnswerIndex,
                            isAnswerSubmitted = isAnswerSubmitted,
                            onClick = { onOptionSelected(index) }
                        )
                    }
                }
            }

            // Bottom Section: Reserved Fixed Height for Feedback & Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (isAnswerSubmitted) {
                    val isUserCorrect = selectedOptionIndex == question.correctAnswerIndex

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Feedback Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isUserCorrect) CorrectGreenContainer else IncorrectRedContainer
                                )
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isUserCorrect) {
                                    "정답입니다! 🎉 (${question.target.character} = ${question.target.romaji})"
                                } else {
                                    "오답입니다! 정답: '${question.correctAnswerText}' (${question.target.character} = ${question.target.romaji})"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUserCorrect) CorrectGreen else IncorrectRed,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onNextQuestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUserCorrect) CorrectGreen else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (currentQuestionIndex + 1 == totalQuestions) "결과 보기 (1초 후 자동 이동)" else "다음 문제 (1초 후 자동 이동)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "다음",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(116.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "보기를 선택하여 정답을 맞춰보세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    optionText: String,
    isSelected: Boolean,
    isCorrectOption: Boolean,
    isAnswerSubmitted: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isAnswerSubmitted && isCorrectOption -> CorrectGreenContainer
            isAnswerSubmitted && isSelected -> IncorrectRedContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "optionContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isAnswerSubmitted && isCorrectOption -> CorrectGreen
            isAnswerSubmitted && isSelected -> IncorrectRed
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "optionContentColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isAnswerSubmitted && isCorrectOption -> CorrectGreen
            isAnswerSubmitted && isSelected -> IncorrectRed
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        },
        label = "optionBorderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isAnswerSubmitted, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!isAnswerSubmitted) 1.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = optionText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            if (isAnswerSubmitted) {
                when {
                    isCorrectOption -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "정답",
                            tint = CorrectGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    isSelected -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "오답",
                            tint = IncorrectRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
