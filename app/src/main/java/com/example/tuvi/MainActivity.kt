package com.example.tuvi

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.model.SavedChart
import com.example.tuvi.presentation.SavedChartsViewModel
import com.example.tuvi.presentation.TuViUiState
import com.example.tuvi.presentation.TuViViewModel
import com.example.tuvi.presentation.screens.InputScreen
import com.example.tuvi.presentation.screens.TuViChartScreen
import com.example.tuvi.ui.browser.BookmarkScreen
import androidx.compose.runtime.LaunchedEffect
import com.example.tuvi.ui.browser.BrowserConfig
import com.example.tuvi.ui.browser.BrowserScreen
import com.example.tuvi.ui.browser.BrowserViewModel
import com.example.tuvi.ui.browser.HistoryScreen
import com.example.tuvi.ui.screens.HomeScreen
import com.example.tuvi.ui.screens.SavedChartsScreen
import com.example.tuvi.ui.theme.TuViTheme
import android.net.Uri
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

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
    val lastInput by viewModel.lastInput.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Edge-to-edge: icon đồng hồ / pin… dùng màu sáng trên nền app tối (đọc được)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = "home"
        ) {
        composable("home") {
            HomeScreen(
                onOpenTuVi = { navController.navigate("input") },
                onOpenSaved = { navController.navigate("saved_charts") },
                onOpenBrowser = {
                    val url = Uri.encode("https://www.google.com")
                    navController.navigate("browser?url=$url&title=Trình+Duyệt")
                }
            )
        }
        composable("input") {
            InputScreen(
                onViewChart = { name, day, month, year, viewYear, hour, minute, gender, duongLich ->
                    viewModel.getTuVi(name, day, month, year, viewYear, hour, minute, gender, duongLich)
                    navController.navigate("chart")
                },
                onBack = { navController.popBackStack() }
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
                        },
                        onSave = { nhom ->
                            lastInput?.let { input ->
                                scope.launch {
                                    val chart = SavedChart(
                                        ten = input.ten,
                                        ngaySinh = "${input.ngay}/${input.thang}/${input.nam}",
                                        gioiTinh = if (input.gioiTinh == 1) "Nam" else "Nữ",
                                        nhom = nhom,
                                        ngayLuu = System.currentTimeMillis(),
                                        inputJson = AppContainer.appJson.encodeToString(input),
                                        chartJson = AppContainer.appJson.encodeToString(state.data)
                                    )
                                    AppContainer.saveChartUseCase(chart)
                                        .onSuccess {
                                            Toast.makeText(
                                                context,
                                                "Đã lưu lá số của ${input.ten}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .onFailure {
                                            Toast.makeText(
                                                context,
                                                "Lỗi khi lưu: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        }
                    )
                }

                is TuViUiState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }

                is TuViUiState.Idle -> {}
            }
        }
        composable(
            route = "browser?url={url}&title={title}",
            arguments = listOf(
                androidx.navigation.navArgument("url") { defaultValue = "https://www.google.com" },
                androidx.navigation.navArgument("title") { defaultValue = "Trình duyệt" }
            )
        ) { backStackEntry ->
            val url =
                Uri.decode(backStackEntry.arguments?.getString("url") ?: "https://www.google.com")
            val title = backStackEntry.arguments?.getString("title") ?: "Trình duyệt"
            // ViewModel gắn với backStackEntry để sống cùng lifecycle của destination này
            val browserVm: BrowserViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = BrowserViewModel.Factory
            )
            // Nhận URL từ HistoryScreen / BookmarkScreen qua savedStateHandle
            val pendingUrl by backStackEntry.savedStateHandle
                .getStateFlow("pendingUrl", "")
                .collectAsState()
            LaunchedEffect(pendingUrl) {
                if (pendingUrl.isNotEmpty()) {
                    browserVm.navigateTo(pendingUrl)
                    backStackEntry.savedStateHandle["pendingUrl"] = ""
                }
            }
            BrowserScreen(
                config = BrowserConfig(initialUrl = url, title = title),
                onBack = { navController.popBackStack() },
                viewModel = browserVm
            )
        }
        composable("browser_history") {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenUrl = { url ->
                    // Truyền URL về browser cũ qua savedStateHandle, giữ nguyên tabs
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("pendingUrl", url)
                    navController.popBackStack()
                }
            )
        }
        composable("browser_bookmarks") {
            BookmarkScreen(
                onBack = { navController.popBackStack() },
                onOpenUrl = { url ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("pendingUrl", url)
                    navController.popBackStack()
                }
            )
        }
        composable("saved_charts") {
            val savedVm: SavedChartsViewModel = viewModel(factory = SavedChartsViewModel.Factory)
            SavedChartsScreen(
                onBack = { navController.popBackStack() },
                onOpenChart = { saved ->
                    scope.launch {
                        runCatching {
                            val chart =
                                AppContainer.appJson.decodeFromString<com.example.tuvi.domain.model.TuViChart>(
                                    saved.chartJson
                                )
                            val input =
                                AppContainer.appJson.decodeFromString<com.example.tuvi.domain.model.TuViChartInput>(
                                    saved.inputJson
                                )
                            viewModel.loadSavedChart(input, chart)
                            navController.navigate("chart")
                        }.onFailure {
                            Toast.makeText(
                                context,
                                "Không thể mở lá số: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                viewModel = savedVm
            )
        }
        }
    }
}
