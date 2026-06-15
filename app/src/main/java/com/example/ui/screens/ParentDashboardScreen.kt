package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodels.ReadingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    viewModel: ReadingViewModel,
    onBack: () -> Unit
) {
    val stats by viewModel.userStats.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "ru") "Аналитика (Родители)" else "Аналитика (Ата-аналар)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (language == "ru") "Общая статистика:" else "Жалпы статистика:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Всего текстов прочитано: ${stats.readTextsCount}")
                    Text("Заработано звезд: ${stats.stars}")
                    Text("Время чтения (сек): ${stats.totalReadingTimeSeconds}")
                }
            }

            Text(if (language == "ru") "История сессий:" else "Сессиялар тарихы:", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn {
                items(sessions) { session ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Текст ID: ${session.textId}", fontWeight = FontWeight.Bold)
                            Text("Дата: ${dateFormat.format(Date(session.dateMillis))}")
                            Text("Длительность: ${session.durationSeconds} сек.")
                            Text("Трудные слова (нажатия): ${session.mistakes}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
