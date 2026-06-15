package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.TextItem
import com.example.viewmodels.ReadingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReadingViewModel,
    onNavigateToReading: (String) -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val availableTexts by viewModel.availableTexts.collectAsState()
    val userStats by viewModel.userStats.collectAsState()

    val filteredTexts = availableTexts.filter { it.language == language }

    var showPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (language == "ru") "🐰 Легкое Чтение 🌸" else "🐰 Оңай Оқу 🌸", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Stars", tint = Color(0xFFFFC107), modifier = Modifier.size(32.dp))
                        Text(
                            text = "${userStats.stars}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(start = 4.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { viewModel.setLanguage(if (language == "ru") "kk" else "ru") }) {
                            Icon(Icons.Default.Translate, contentDescription = "Change Language", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showPinDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Parent Dashboard", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Little background decorators
            Text("🦋", fontSize = 40.sp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 100.dp, end = 20.dp))
            Text("⭐", fontSize = 30.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp))
            Text("🍀", fontSize = 50.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 50.dp, end = 30.dp))
            Text("🐱", fontSize = 40.sp, modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 80.dp, start = 20.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (language == "ru") "Выбери текст для чтения:" else "Оқуға арналған мәтінді таңдаңыз:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredTexts) { textItem ->
                        TextCard(
                            textItem = textItem,
                            onClick = { onNavigateToReading(textItem.id) }
                        )
                    }
                }
            }
        }

        if (showPinDialog) {
            PinDialog(
                language = language,
                onDismiss = { showPinDialog = false },
                onSuccess = {
                    showPinDialog = false
                    onNavigateToDashboard()
                }
            )
        }
    }
}

@Composable
fun TextCard(textItem: TextItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("text_card_${textItem.id}"),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📚", fontSize = 28.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = textItem.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (textItem.language == "ru") "Сложность: " else "Күрделілігі: ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    repeat(textItem.difficulty) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFFFC107))
                    }
                }
            }
        }
    }
}

@Composable
fun PinDialog(language: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    // Fake PIN auth for MVP
    var pin by remember { mutableStateOf("") }
    val correctPin = "1111"
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == "ru") "Вход для родителей" else "Ата-аналарға кіру") },
        text = {
            Column {
                Text(if (language == "ru") "Введите PIN-код (по умолчанию 1111):" else "PIN-кодты енгізіңіз (әдепкі бойынша 1111):")
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it; error = false },
                    isError = error
                )
                if (error) {
                    Text("Неверный код", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (pin == correctPin) onSuccess() else error = true
            }) {
                Text(if (language == "ru") "Войти" else "Кіру")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == "ru") "Отмена" else "Болдырмау")
            }
        }
    )
}
