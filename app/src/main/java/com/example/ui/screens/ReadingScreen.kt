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
import androidx.compose.ui.graphics.Color
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
    var accumulatedText by remember { mutableStateOf("") }
    var currentSessionText by remember { mutableStateOf("") }
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
            onResult = { text, isFinal -> 
                if (isFinal) {
                    accumulatedText += " $text"
                    currentSessionText = ""
                } else {
                    currentSessionText = text
                }
            },
            onError = { error -> sttError = error },
            onStateChanged = { listening -> isListening = listening }
        )
        onDispose {
            ttsManager?.shutdown()
            speechRecognizerManager?.stopListening()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(textItem.title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.primary)
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
                        Icon(Icons.Default.PlayArrow, contentDescription = "Озвучить текст", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Text("🎈", fontSize = 50.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = Color.White,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(if (language == "ru") "Читать по слогам" else "Буынмен оқу", modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Switch(
                            checked = isSyllableMode, 
                            onCheckedChange = { isSyllableMode = !isSyllableMode },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

            // Text block spacing
            val words = textItem.content.split(Regex("\\s+"))
            val difficultWordIndices = remember { mutableStateListOf<Int>() }
            val correctWordIndices = remember { mutableStateListOf<Int>() }
            
            val fullSpokenText = "$accumulatedText $currentSessionText"
            LaunchedEffect(fullSpokenText) {
                correctWordIndices.clear()
                val spokenWords = fullSpokenText.lowercase().replace(Regex("[^\\p{L}\\s]"), "").split(Regex("\\s+"))
                val targetWords = words.map { it.lowercase().replace(Regex("[^\\p{L}\\s]"), "") }
                
                var tIdx = 0
                for (sWord in spokenWords) {
                    if (sWord.isBlank()) continue
                    for (i in tIdx until minOf(tIdx + 4, targetWords.size)) {
                        if (targetWords[i] == sWord || (sWord.length >= 4 && targetWords[i].startsWith(sWord.take(4)))) {
                            correctWordIndices.add(i)
                            tIdx = i + 1
                            break
                        }
                    }
                }
            }
            
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
                        val isCorrect = correctWordIndices.contains(index)

                        val wordColor = when {
                            isCorrect -> Color(0xFF4CAF50)
                            isDifficult -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Text(
                            text = displayWord,
                            fontSize = 32.sp, // Large font for dyslexia
                            fontWeight = FontWeight.Medium,
                            lineHeight = 44.sp,
                            color = wordColor,
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
            
            if (sttError != null) {
                Text(
                    text = sttError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                    modifier = Modifier.padding(end = 16.dp).size(72.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.Mic, 
                        contentDescription = "Speech to Text", 
                        tint = if (isListening) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Button(
                    onClick = {
                        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                        onReadingFinished(duration, mistakesCount)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == "ru") "Я прочитал!" else "Мен оқыдым!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        }
    }
}
