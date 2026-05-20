package com.example.tuvi

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anhnn.language.LanguageDataSource
import com.anhnn.language.LanguageManager
import com.anhnn.language.LanguageScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tuvi.presentation.AuthUiState
import com.example.tuvi.presentation.AuthViewModel
import com.example.tuvi.presentation.SavedChartsViewModel
import com.example.tuvi.presentation.SettingsUiState
import com.example.tuvi.presentation.SettingsViewModel
import com.example.tuvi.presentation.TuViError
import com.example.tuvi.presentation.TuViUiState
import com.example.tuvi.presentation.TuViViewModel
import com.example.tuvi.presentation.resolve
import com.example.tuvi.presentation.screens.InputScreen
import com.example.tuvi.presentation.screens.TuViChartScreen
import com.example.tuvi.ui.browser.BookmarkScreen
import androidx.compose.runtime.LaunchedEffect
import com.example.tuvi.ui.browser.BrowserConfig
import com.example.tuvi.ui.browser.BrowserScreen
import com.example.tuvi.presentation.BrowserViewModel
import com.example.tuvi.ui.browser.HistoryScreen
import com.example.tuvi.ui.screens.AiReadingScreen
import com.example.tuvi.ui.screens.CalendarChooserScreen
import com.example.tuvi.ui.screens.HomeScreen
import com.example.tuvi.ui.screens.LichScreen
import com.example.tuvi.ui.screens.LoginScreen
import com.example.tuvi.ui.screens.SavedChartsScreen
import com.anhnn.feedback.FeedbackScreen
import com.anhnn.rate.RateDialog
import com.anhnn.rate.requestInAppReview
import com.example.tuvi.ui.screens.PrivacyPolicyScreen
import com.example.tuvi.ui.screens.SettingsScreen
import com.example.tuvi.ui.theme.TuViTheme
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val code = runBlocking { LanguageDataSource(newBase).languageCode.first() }
        super.attachBaseContext(LanguageManager.setLanguage(newBase, code))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TuViApplication
        setContent {
            val settingsVm: SettingsViewModel = viewModel(
                factory = AndroidViewModelFactory.getInstance(application)
            )
            val settingsState by settingsVm.uiState.collectAsStateWithLifecycle(
                initialValue = SettingsUiState(
                    themeDark = app.initialDark,
                    localeTag = com.example.tuvi.data.preferences.UserPreferencesRepository.LOCALE_VI
                )
            )
            TuViTheme(darkTheme = settingsState.themeDark) {
                TuViApp(isDark = settingsState.themeDark)
            }
        }
    }
}

@Composable
fun TuViApp(isDark: Boolean = true) {
    val navController = rememberNavController()
    val viewModel: TuViViewModel = viewModel(factory = TuViViewModel.Factory)
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lastInput by viewModel.lastInput.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val startDestination = remember { if (authViewModel.isSignedIn) "home" else "login" }
    var showRateDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = startDestination
        ) {
        composable("login") {
            val loading = authState is AuthUiState.Loading
            LaunchedEffect(authState) {
                when (val s = authState) {
                    is AuthUiState.SignedIn -> {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                    is AuthUiState.Error -> {
                        val msg = s.message.takeIf { it.isNotBlank() }
                            ?.let { context.getString(R.string.login_error_generic, it) }
                            ?: context.getString(R.string.login_error_unknown)
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        authViewModel.consumeError()
                    }
                    else -> Unit
                }
            }
            LoginScreen(
                loading = loading,
                onSignInWithGoogle = { authViewModel.signInWithGoogle(context) },
            )
        }
        composable("home") {
            LaunchedEffect(Unit) { authViewModel.refreshProfile() }
            HomeScreen(
                onOpenTuVi = { navController.navigate("input") },
                onOpenBrowser = {
                    val url = Uri.encode("https://www.google.com")
                    navController.navigate("browser?url=$url&title=Trình+Duyệt")
                },
                onOpenCalendar = { navController.navigate("lich") },
                onOpenSettings = { navController.navigate("settings") },
                authUser = (authState as? AuthUiState.SignedIn)?.user,
            )
        }
        composable("calendar_chooser") {
            CalendarChooserScreen(
                onBack = { navController.popBackStack() },
                onOpenCalendar = { navController.navigate("lich") },
            )
        }
        composable("lich") {
            LichScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            val authUser = (authState as? AuthUiState.SignedIn)?.user
            LaunchedEffect(Unit) { authViewModel.refreshProfile() }
            SettingsScreen(
                onBack = { navController.popBackStack() },
                authUser = authUser,
                onSignOut = {
                    authViewModel.signOut(context)
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenSaved = { navController.navigate("saved_charts") },
                onOpenLanguage = { navController.navigate("language") },
                onOpenPrivacy = { navController.navigate("privacy_policy") },
                onOpenFeedback = { navController.navigate("feedback") },
                onRateApp = {
                    requestInAppReview(
                        activity = context as Activity,
                        onFallback = { showRateDialog = true }
                    )
                }
            )
        }
        composable("feedback") {
            FeedbackScreen(
                email = "nguyenanhcry@gmail.com",
                subject = context.getString(R.string.feedback_subject),
                title = context.getString(R.string.feedback_screen_title),
                onBack = { navController.popBackStack() }
            )
        }
        composable("language") {
            val context = LocalContext.current
            LanguageScreen(
                onBack = { navController.popBackStack() },
                onLanguageSaved = { (context as? Activity)?.recreate() }
            )
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
        composable("input") {
            InputScreen(
                onViewChart = { name, day, month, year, viewYear, hour, minute, gender, duongLich ->
                    viewModel.getTuVi(
                        name,
                        day,
                        month,
                        year,
                        viewYear,
                        hour,
                        minute,
                        gender,
                        duongLich,
                    )
                    navController.navigate("chart")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("chart") {
            val savedChartIdVm by viewModel.savedChartId.collectAsStateWithLifecycle()
            when (val state = uiState) {
                is TuViUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is TuViUiState.Success -> {
                    TuViChartScreen(
                        data = state.data,
                        onOpenAiReading = { navController.navigate("ai_reading") },
                        savedChartId = savedChartIdVm,
                        onBack = {
                            viewModel.resetState()
                            navController.popBackStack()
                        },
                        onSave = { nhom, onResult ->
                            viewModel.saveChart(nhom) { result ->
                                result
                                    .onSuccess { _ ->
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_chart_saved, lastInput?.ten.orEmpty()),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onResult(true)
                                    }
                                    .onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_chart_save_failed, it.message.orEmpty()),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onResult(false)
                                    }
                            }
                        },
                        onRemoveSave = { id, onResult ->
                            viewModel.deleteChart(id) { result ->
                                result
                                    .onSuccess {
                                        Toast.makeText(context, context.getString(R.string.toast_chart_unsaved), Toast.LENGTH_SHORT).show()
                                        onResult(true)
                                    }
                                    .onFailure {
                                        Toast.makeText(context, context.getString(R.string.toast_chart_unsave_failed, it.message.orEmpty()), Toast.LENGTH_SHORT).show()
                                        onResult(false)
                                    }
                            }
                        }
                    )
                }

                is TuViUiState.Error -> {
                    Toast.makeText(context, state.error.resolve(context), Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                }

                is TuViUiState.Idle -> {}
            }
        }
        composable("ai_reading") {
            val aiInterpretLoading by viewModel.aiInterpretLoading.collectAsStateWithLifecycle()
            val selectedCung by viewModel.selectedCung.collectAsStateWithLifecycle()
            val aiReadings = (uiState as? TuViUiState.Success)?.aiReadings ?: emptyMap()
            val authUser = (authState as? AuthUiState.SignedIn)?.user
            var showInsufficient by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) { authViewModel.refreshProfile() }
            AiReadingScreen(
                selectedCung = selectedCung,
                aiReadings = aiReadings,
                loading = aiInterpretLoading,
                onSelectCung = { viewModel.selectCung(it) },
                onRequest = {
                    viewModel.fetchAiInterpretation(
                        cung = selectedCung,
                        onError = { err ->
                            if (err == TuViError.AiInsufficientTokens) {
                                showInsufficient = true
                            } else {
                                Toast.makeText(context, err.resolve(context), Toast.LENGTH_LONG).show()
                            }
                        },
                        onBalanceUpdated = { tokens, free ->
                            authViewModel.updateBalance(tokens, free)
                        },
                    )
                },
                onBack = { navController.popBackStack() },
                tokens = authUser?.tokens,
                freeQuestions = authUser?.freeQuestions,
                aiQuestionCost = authUser?.aiQuestionCost,
                showInsufficientDialog = showInsufficient,
                onDismissInsufficientDialog = { showInsufficient = false },
                onTopUp = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.ai_topup_coming_soon),
                        Toast.LENGTH_LONG,
                    ).show()
                },
            )
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
                .collectAsStateWithLifecycle()
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
                    viewModel.openSavedChart(saved) { ok ->
                        if (ok) navController.navigate("chart")
                        else Toast.makeText(context, context.getString(R.string.toast_chart_open_failed), Toast.LENGTH_SHORT).show()
                    }
                },
                viewModel = savedVm
            )
        }
        }

        if (showRateDialog) {
            RateDialog(
                packageName = context.packageName,
                title = stringResource(R.string.rate_dialog_title),
                message = stringResource(R.string.rate_dialog_message),
                confirmText = stringResource(R.string.rate_dialog_confirm),
                dismissText = stringResource(R.string.rate_dialog_dismiss),
                onDismiss = { showRateDialog = false }
            )
        }
    }
}
