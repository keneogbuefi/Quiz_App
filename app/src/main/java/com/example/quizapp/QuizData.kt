package com.example.quizapp
import android.os.Parcelable
import java.io.Serializable
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.parcelize.Parcelize

@Parcelize
@KSerializable
data class Quiz(
    val title: String,
    val questions: List<QuizQuestion>
    // Add other properties as needed
): Parcelable


@Parcelize
@KSerializable
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int // Index of the correct answer in the options list
): Parcelable