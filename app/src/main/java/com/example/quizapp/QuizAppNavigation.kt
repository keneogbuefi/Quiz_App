package com.example.quizapp

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun QuizScreen(quizQuestions: List<QuizQuestion>, onQuizComplete: () -> Unit) {
    // ... your QuizScreen implementation ...

    // When the quiz is completed, call onQuizComplete
    // e.g., Button(onClick = onQuizComplete) { Text("Finish Quiz") }
}

@Composable
fun QuizAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = HomeScreenViewModel(navController, context = LocalContext.current),
                    onPlayQuiz = { quiz ->
                    // Pass the User object as a Parcelable
                        Log.d("QuizAppNavigation", "Quiz to play: ${quiz.title}")
                    navController.currentBackStackEntry?.savedStateHandle?.set("quiz",  quiz)
                    navController.navigate("quiz_display")
                }
                )
        }
        composable("quiz_prompt") {
            QuizPromptScreen(
                viewModel = viewModel(),
                navigateBack = { navController.popBackStack() },
                navigateToQuiz = { quiz ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("quiz", quiz)
                    navController.navigate("quiz_screen")
                }
            )
        }
        composable("quiz_display") {
            val quiz = navController.previousBackStackEntry?.savedStateHandle?.get<Quiz>("quiz")
            if (quiz != null) {
                QuizDisplayScreen(quiz,navigateBack = {Log.d("QuizAppNavigation", "Back button clicked")
                    navController.navigate("home") },)
            }
        }


        // Add other composable routes as needed, like "results", "history"
    }
}