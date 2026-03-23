package com.example.tuvi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tuvi.presentation.TuViUiState
import com.example.tuvi.presentation.TuViViewModel
import com.example.tuvi.presentation.screens.InputScreen
import com.example.tuvi.presentation.screens.TuViChartScreen
import com.example.tuvi.ui.theme.TuViTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TuViTheme {
                TuViApp()
            }
        }
    }
}

@Composable
fun TuViApp() {
    val navController = rememberNavController()
    val viewModel: TuViViewModel = viewModel(factory = TuViViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "input") {
        composable("input") {
            InputScreen(
                onViewChart = { name, day, month, year, viewYear, hour, minute, gender ->
                    viewModel.getTuVi(name, day, month, year, viewYear, hour, minute, gender)
                    navController.navigate("chart")
                }
            )
        }
        composable("chart") {
            when (val state = uiState) {
                is TuViUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TuViUiState.Success -> {
                    TuViChartScreen(
                        data = state.data,
                        onBack = {
                            viewModel.resetState()
                            navController.popBackStack()
                        }
                    )
                }
                is TuViUiState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }
                is TuViUiState.Idle -> {
                    // Should not happen if coming from input
                }
            }
        }
    }
}