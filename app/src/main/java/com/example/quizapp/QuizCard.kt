package com.example.quizapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuizCard(
    quiz: Quiz,
    onPlayQuiz: (Quiz) -> Unit,
    onDeleteQuiz: (Quiz) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Add vertical padding for better spacing
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Slightly more elevation for better effect
        shape = RoundedCornerShape(12.dp) // Rounded corners for a softer look
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Keep the padding uniform
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with weight to prevent squishing icons
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium, // Applying a more prominent text style
                modifier = Modifier.weight(1f) // Make title take up remaining space
            )

            Spacer(modifier = Modifier.width(8.dp)) // Space between title and icons

            // Action Icons Row with spacing
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Add space between the icons
            ) {
                IconButton(onClick = { onPlayQuiz(quiz) }) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play Quiz",
                        tint = MaterialTheme.colorScheme.primary // Make icon match theme color
                    )
                }
                IconButton(onClick = { onDeleteQuiz(quiz) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Quiz",
                        tint = MaterialTheme.colorScheme.error // Make delete icon red for emphasis
                    )
                }
            }
        }
    }
}
