package com.example.quizapp

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


class QuizPromptViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()




    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _quizGenerated = MutableStateFlow<List<QuizQuestion>?>(null)
    val quizGenerated: StateFlow<List<QuizQuestion>?> = _quizGenerated.asStateFlow()


    fun generateQuiz(subject: String, questionCount: Float, difficulty: String, title: String, isRandomTitle: Boolean, selectedModel: String) {
        val generativeModel = GenerativeModel(
            modelName = selectedModel,
            apiKey = BuildConfig.apiKey
        )
        _uiState.value = UiState.Loading
        val cat = """
            Generate a quiz about $subject with question count of $questionCount and difficulty of $difficulty, in JSON format with the following schema and do not add any extra text, begin and end with the first and last curly braces:
        {
            "title": "${if (isRandomTitle) "Random Quiz Title" else title}", 
            "questions": [
            {
                "question": "string",
                "options": ["string", "string", "string", "string"],
                "correctAnswer": "number" // Index of the correct answer in the options array
            },
            // ...
            ]
        }"""

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        text(cat)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun saveQuizToFile(context: Context, quiz: Quiz, filename: String) {
        val json = Json.encodeToString(quiz)
        val file = File(context.filesDir, filename)
        file.writeText(json)
    }

    fun parseQuizFromJson(jsonString: String, context: Context) {
        val gson = Gson()
        val quiz = gson.fromJson(jsonString, Quiz::class.java)
        if (quiz != null) {
            saveQuizToFile(context, quiz, "${quiz.title}.json")

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPromptScreen(
    viewModel: QuizPromptViewModel,
    navigateBack: () -> Unit,
    navigateToQuiz: (Quiz) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val placeholderPrompt = ""
    val placeholderResult = ""
    var subject by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var questionCount by remember { mutableStateOf(5f) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var difficulty by remember { mutableStateOf("Medium") }
    val difficulties = listOf("Easy", "Medium", "Hard", "Very Hard")
    var quizTitle by rememberSaveable { mutableStateOf("") }
    var isRandomTitle by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    var selectedModel by remember { mutableStateOf("gemini-1.5-pro") }
    var isProSelected by remember { mutableStateOf(true) }
    var isFlashSelected by remember { mutableStateOf(false) }

    SideEffect {
        if (uiState is UiState.Success) {
            val quizJson = (uiState as UiState.Success).outputText
            Log.d("QuizPromptScreen", "Quiz JSON: $quizJson")
            viewModel.parseQuizFromJson(quizJson, context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(

                title = { Text(style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    text = "Create Your Quiz") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Enter Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = error != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = quizTitle,
                        onValueChange = { quizTitle = it },
                        label = { Text("Enter Quiz Title") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = error != null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use Random Title")
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = isRandomTitle, onCheckedChange = { isRandomTitle = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Model and Difficulty Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Model & Difficulty",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Model Selection
                    Text("Select Model", style = MaterialTheme.typography.bodyLarge)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isProSelected,
                            onCheckedChange = {
                                isProSelected = it
                                if (it) isFlashSelected = false
                            }
                        )
                        Text("Gemini Pro")
                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(
                            checked = isFlashSelected,
                            onCheckedChange = {
                                isFlashSelected = it
                                if (it) isProSelected = false
                            }
                        )
                        Text("Flash")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Difficulty Selection
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = difficulty,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Select Difficulty") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            difficulties.forEach { selectionOption ->
                                DropdownMenuItem(
                                    onClick = {
                                        difficulty = selectionOption
                                        expanded = false
                                    },
                                    text = { Text(selectionOption) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Question Count
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Question Count: ${questionCount.toInt()}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = questionCount,
                        onValueChange = { questionCount = it },
                        valueRange = 1f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Handling and Generate Button
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.generateQuiz(
                        subject = subject,
                        questionCount = questionCount,
                        difficulty = difficulty,
                        title = quizTitle,
                        isRandomTitle = isRandomTitle,
                        selectedModel = selectedModel
                    )
                },
                enabled = !isLoading && subject.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Generate Quiz")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Section
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                val textColor = if (uiState is UiState.Error) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = result,
                    color = textColor,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
