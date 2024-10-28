package com.example.quizapp

//Programmer: Kene Ogbuefi
//Date: 9/27/2024; version 1
//Android Studio Koala Feature Drop| 2024.1.2
//Windows 11 Home 23H2
//Description: This app allows you to generate and play multiple choice quizzes using Google's Gemini Pro or Flash AI.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.quizapp.ui.theme.QuizAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuizAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuizAppNavigation() // Call the navigation composable
                }
            }
        }
    }
}