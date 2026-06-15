package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.utils.SpeechRecognizerManager
import com.example.utils.TTSManager
import com.example.utils.TextSplitter
import com.example.viewmodels.ReadingViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    textId: String,
    viewModel: ReadingViewModel,
    onReadingFinished: (duration: Int, mistakes: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val texts by viewModel.availableTexts.collectAsState()
    val textItem = texts.find { it.id == textId }
    val language by viewModel.currentLanguage.collectAsState()

    if (textItem == null) {
        onBack()
        return
    }

    var isSyllableMode by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var ttsManager: TTSManager? by remember { mutableStateOf(null) }
    var mistakesCount by remember { mutableStateOf(0) }
    
    // STT State
    var recognizedText by remember { mutableStateOf("") }
    var sttError by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    var speechRecognizerManager: SpeechRecognizerManager? by remember { mutableStateOf(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            speechRecognizerManager?.startListening(textItem.language)
        } else {
            sttError = if (language == "ru") "Нет доступа к микрофону" else "Микрофонға рұқсат жоқ"
        }
    }

    LaunchedEffect(Unit) {
        startTime = System.currentTimeMillis()
    }

    DisposableEffect(Unit) {
        ttsManager = TTSManager(context) { ready ->
            if (ready) {
                ttsManager?.setLanguage(textItem.language)
            }
        }
        speechRecognizerManager = SpeechRecognizerManager(
            context = context,
            onResult = { result -> recognizedText = result },
            onError = { error -> sttError = error },
            onStateChanged = { listening -> isListening = listening }
        )
        onDispose {
            ttsManager?.shutdown()
            speechRecognizerManager?.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(textItem.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val textToSpeak = if (isSyllableMode) {
                            textItem.content.split(" ", "\n").joinToString(" ") { word ->
                                TextSplitter.splitIntoSyllables(word.trim()).replace("-", ", ")
                            }
                        } else {
                            textItem.content
                        }
                        ttsManager?.speak(textToSpeak)
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Озвучить текст")
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(if (language == "ru") "Читать по слогам" else "Буынмен оқу", modifier = Modifier.weight(1f), fontSize = 18.sp)
                Switch(checked = isSyllableMode, onCheckedChange = { isSyllableMode = !isSyllableMode })
            }

            // Text block spacing
            val words = textItem.content.split(Regex("\\s+"))
            val difficultWordIndices = remember { mutableStateListOf<Int>() }
            
            FlowRow(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                words.forEachIndexed { index, word ->
                    val cleanWord = word.trim()
                    if (cleanWord.isNotEmpty()) {
                        val displayWord = if (isSyllableMode) {
                            TextSplitter.splitIntoSyllables(cleanWord)
                        } else {
                            cleanWord
                        }

                        val isDifficult = difficultWordIndices.contains(index)

                        Text(
                            text = displayWord,
                            fontSize = 32.sp, // Large font for dyslexia
                            fontWeight = FontWeight.Medium,
                            lineHeight = 44.sp,
                            color = if (isDifficult) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clickable {
                                    if (!difficultWordIndices.contains(index)) {
                                        difficultWordIndices.add(index)
                                        mistakesCount++
                                    }
                                    val textToSpeak = if (isSyllableMode) {
                                        TextSplitter.splitIntoSyllables(cleanWord).replace("-", ", ")
                                    } else {
                                        cleanWord
                                    }
                                    ttsManager?.speak(textToSpeak)
                                }
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            
            if (recognizedText.isNotEmpty() || sttError != null) {
                 Surface(
                     modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                     color = MaterialTheme.colorScheme.surfaceVariant,
                     shape = MaterialTheme.shapes.medium
                 ) {
                     Column(modifier = Modifier.padding(16.dp)) {
                         Text(
                             text = if (language == "ru") "Вы сказали:" else "Сіз айттыңыз:",
                             style = MaterialTheme.typography.labelMedium,
                             color = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                         Text(
                             text = sttError ?: recognizedText,
                             style = MaterialTheme.typography.bodyLarge,
                             color = if (sttError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                         )
                     }
                 }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FloatingActionButton(
                    onClick = {
                        if (isListening) {
                            speechRecognizerManager?.stopListening()
                        } else {
                            sttError = null
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                speechRecognizerManager?.startListening(textItem.language)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Speech to Text", tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Button(
                    onClick = {
                        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                        onReadingFinished(duration, mistakesCount)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(if (language == "ru") "Я прочитал!" else "Мен оқыдым!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
