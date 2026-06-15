package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodels.ReadingViewModel
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    textId: String,
    durationSecs: Int,
    mistakes: Int,
    viewModel: ReadingViewModel,
    onQuizComplete: () -> Unit
) {
    val texts by viewModel.availableTexts.collectAsState()
    val textItem = texts.find { it.id == textId }
    val language by viewModel.currentLanguage.collectAsState()

    if (textItem == null) {
        onQuizComplete()
        return
    }

    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showReward by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showReward) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 100.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(120.dp), tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (language == "ru") "Отлично! +1 Звезда" else "Керемет! +1 Жұлдыз",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LaunchedEffect(Unit) {
                    delay(2000)
                    viewModel.finishReading(textItem, durationSecs, mistakes, starsEarned = 1)
                    onQuizComplete()
                }
            }
            return@Surface
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Text("❓", fontSize = 60.sp, modifier = Modifier.align(Alignment.TopEnd).padding(32.dp))
            Text("🤔", fontSize = 60.sp, modifier = Modifier.align(Alignment.BottomStart).padding(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = textItem.quizQuestion,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 32.dp),
                    textAlign = TextAlign.Center
                )

                textItem.quizAnswers.forEachIndexed { index, answer ->
                    val isError = selectedAnswer == index && index != textItem.correctAnswerIndex
                    
                    Button(
                        onClick = {
                            selectedAnswer = index
                            if (index == textItem.correctAnswerIndex) {
                                showReward = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(72.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(text = answer, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
