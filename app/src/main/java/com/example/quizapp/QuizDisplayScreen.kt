package com.example.quizapp

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDisplayScreen(quiz: Quiz, navigateBack: () -> Unit) {
    val selectedAnswers = remember { mutableStateListOf<Int?>(*arrayOfNulls<Int?>(quiz.questions.size)) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var showResults by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (!showResults) {
                // Display current question in a Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        val currentQuestion = quiz.questions[currentQuestionIndex]
                        QuestionDisplay(
                            question = currentQuestion,
                            selectedAnswer = selectedAnswers[currentQuestionIndex]
                        ) { answerIndex ->
                            selectedAnswers[currentQuestionIndex] = answerIndex
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentQuestionIndex > 0) {
                        Button(onClick = { currentQuestionIndex-- }) {
                            Text("Previous")
                        }
                    }
                    if (currentQuestionIndex < quiz.questions.size - 1) {
                        Button(onClick = { currentQuestionIndex++ }) {
                            Text("Next")
                        }
                    } else {
                        // Submit button on the last question
                        Button(onClick = { showResults = true }) {
                            Text("Submit")
                        }
                    }
                }
            } else {
                // Results Section with background
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp)) // Apply rounded corners
                        .background(Color(186, 220, 255, 123)) // Semi-transparent background
                        .padding(8.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Results",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Display total correct answers
                        Text(
                            text = "Total Correct: ${selectedAnswers.countCorrectAnswers(quiz)} out of ${quiz.questions.size}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable list of questions results
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        itemsIndexed(quiz.questions) { index, question ->
                            val selectedAnswer = selectedAnswers[index]
                            val isCorrect = selectedAnswer == question.correctAnswer

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Question ${index + 1}: ${if (isCorrect) "Correct" else "Incorrect"}",
                                        color = if (isCorrect) Color.Green else Color.Red,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Your Answer: ${question.options.getOrNull(selectedAnswer ?: -1) ?: "Not answered"}")
                                    Text("Correct Answer: ${question.options[question.correctAnswer]}")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Retry and Back buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { showResults = false }) {
                        Text("Try Again")
                    }
                    Button(onClick = { navigateBack() }) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionDisplay(question: QuizQuestion, selectedAnswer: Int?, onAnswerSelected: (Int) -> Unit) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = question.question,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        question.options.forEachIndexed { index, option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                RadioButton(
                    selected = selectedAnswer == index,
                    onClick = { onAnswerSelected(index) }
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private fun List<Int?>.countCorrectAnswers(quiz: Quiz): Int {
    return this.zip(quiz.questions.map { it.correctAnswer }).count { (selected, correct) ->
        selected == correct
    }
}
