package com.example.quizapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class HomeScreenViewModel(
    private val navController: NavController,
    private val context : Context
) : ViewModel() {

    private val _quizzes = mutableStateListOf<Quiz>()
    val quizzes: List<Quiz> = _quizzes

    init {
        viewModelScope.launch {
            _quizzes.addAll(loadQuizzesFromDataSource(context)) // Pass context here
        }
    }

    private suspend fun loadQuizzesFromDataSource(context: Context): List<Quiz> {
        val quizzes = mutableListOf<Quiz>()
        val filesDir = context.filesDir
        val quizFiles = filesDir.listFiles { file -> file.name.endsWith(".json") }

        quizFiles?.forEach { file ->
            try {
                val json = file.readText()
                Log.d("HomeScreenViewModel", "Loaded JSON: $json")
                val quiz = Json.decodeFromString<Quiz>(json)
                quizzes.add(quiz)
            } catch (e: Exception) {
                // Handle errors, e.g., log the exception
                Log.e("HomeScreenViewModel", "Error loading quiz from file: ${file.name}", e)
            }
        }

        return quizzes
    }

    fun onDeleteQuiz(quiz: Quiz) {
        _quizzes.remove(quiz)
        val file = File(context.filesDir, "${quiz.title}.json")
        file.delete()
    }

    fun onCreateNewQuiz() {
        navController.navigate("quiz_prompt")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeScreenViewModel, onPlayQuiz: (quiz: Quiz) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                ),

                title = { Text("Quiz App")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onCreateNewQuiz) {
                Icon(Icons.Filled.Add, contentDescription = "Create New Quiz")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.quizzes) { quiz ->
                QuizCard(quiz, onPlayQuiz, viewModel::onDeleteQuiz)
            }
        }
    }
}