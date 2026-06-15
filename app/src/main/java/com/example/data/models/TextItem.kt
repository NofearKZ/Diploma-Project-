package com.example.data.models

data class TextItem(
    val id: String,
    val title: String,
    val content: String,
    val language: String, // "ru" or "kk"
    val difficulty: Int, // 1, 2, 3
    val quizQuestion: String,
    val quizAnswers: List<String>,
    val correctAnswerIndex: Int
)

val sampleTexts = listOf(
    TextItem(
        id = "ru_1",
        title = "Кот и мышь",
        content = "Жил-был кот. Он любил спать на окне. Однажды мимо пробежала маленькая мышка. Кот открыл один глаз, посмотрел на нее и снова уснул. Мышка обрадовалась и убежала в свою норку.",
        language = "ru",
        difficulty = 1,
        quizQuestion = "Где любил спать кот?",
        quizAnswers = listOf("На столе", "На окне", "На полу"),
        correctAnswerIndex = 1
    ),
    TextItem(
        id = "ru_2",
        title = "Друзья",
        content = "В лесу жили заяц и ежик. Заяц быстро бегал, а ежик собирал грибы. Они часто играли вместе на поляне и помогали друг другу.",
        language = "ru",
        difficulty = 2,
        quizQuestion = "Что собирал ежик?",
        quizAnswers = listOf("Ягоды", "Грибы", "Шишки"),
        correctAnswerIndex = 1
    ),
    TextItem(
        id = "kk_1",
        title = "Алтын күз",
        content = "Күз келді. Ағаштардың жапырақтары сарғайды. Құстар жылы жаққа ұшып кетті. Балалар мектепке барды.",
        language = "kk",
        difficulty = 1,
        quizQuestion = "Құстар қайда ұшып кетті?",
        quizAnswers = listOf("Орманға", "Жылы жаққа", "Ауылға"),
        correctAnswerIndex = 1
    ),
    TextItem(
        id = "kk_2",
        title = "Түлкі мен ешкі",
        content = "Бір күні түлкі құдыққа түсіп кетеді. Оның қасына бір ешкі келеді. Түлкі ешкіні алдап құдыққа түсіреді де, өзі ешкінің мүйізіне басып құдықтан шығып кетеді.",
        language = "kk",
        difficulty = 2,
        quizQuestion = "Түлкі кімді алдады?",
        quizAnswers = listOf("Қасқырды", "Қоянды", "Ешкіні"),
        correctAnswerIndex = 2
    )
)
