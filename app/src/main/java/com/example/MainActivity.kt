package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ReadingRepository
import com.example.data.local.AppDatabase
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodels.ReadingViewModel
import com.example.viewmodels.ReadingViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ReadingRepository(database.readingDao())
    
    setContent {
      MyApplicationTheme {
        val viewModel: ReadingViewModel = viewModel(factory = ReadingViewModelFactory(repository))
        AppNavigation(viewModel = viewModel)
      }
    }
  }
}
