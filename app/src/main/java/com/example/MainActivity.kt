package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.AssistantRepository
import com.example.ui.AssistantScreen
import com.example.ui.AssistantViewModel
import com.example.ui.AssistantViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize SQLite Room Database
    val database = Room.databaseBuilder(
      applicationContext,
      AppDatabase::class.java,
      "aura_assistant_db"
    ).build()

    val repository = AssistantRepository(database)
    val viewModelFactory = AssistantViewModelFactory(repository)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        // Instantiate ViewModel with the custom repository factory
        val viewModel: AssistantViewModel = viewModel(factory = viewModelFactory)
        AssistantScreen(viewModel = viewModel)
      }
    }
  }
}

