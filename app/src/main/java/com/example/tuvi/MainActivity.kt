package com.example.tuvi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anhnn.language.LanguageDataSource
import com.anhnn.language.LanguageManager
import com.example.tuvi.ui.screens.LanguagePickerScreen
import com.example.tuvi.ui.screens.SplashScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.anhnn.ads.Ads
import com.anhnn.analytics.Analytics
import com.anhnn.analytics.TrackScreenViews
import com.example.tuvi.ads.AdNames
import com.example.tuvi.ads.RemoteConfigManager
import com.example.tuvi.analytics.Events
import com.example.tuvi.presentation.SavedChartsViewModel
import com.example.tuvi.presentation.SettingsUiState
import com.example.tuvi.presentation.SettingsViewModel
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
import com.example.tuvi.ui.screens.ExitAppHandler
import com.example.tuvi.ui.screens.HomeScreen
import com.example.tuvi.ui.screens.IntroScreen
import com.example.tuvi.ui.screens.LichScreen
import com.example.tuvi.ui.screens.SavedChartsScreen
import com.anhnn.feedback.FeedbackScreen
import com.anhnn.rate.RateDialog
import com.anhnn.rate.requestInAppReview
import com.example.tuvi.ui.screens.PrivacyPolicyScreen
import com.example.tuvi.ui.screens.SettingsScreen
import com.example.tuvi.ui.theme.TuViTheme
import android.net.Uri

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val code = runBlocking { LanguageDataSource(newBase).languageCode.first() }
        super.attachBaseContext(LanguageManager.setLanguage(newBase, code))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TuViApplication

        // Consent (UMP) + init Mobile Ads SDK (gói trong module ads); xong thì preload.
        Ads.start(this) {
            Ads.preload(this, AdNames.SPLASH_OPEN)
        }
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
                TuViApp(isDark = settingsState.themeDark, onboardingDone = app.initialOnboardingDone)
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun TuViApp(isDark: Boolean = true, onboardingDone: Boolean = true) {
    val navController = rememberNavController()
    val viewModel: TuViViewModel = viewModel(factory = TuViViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lastInput by viewModel.lastInput.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showRateDialog by rememberSaveable { mutableStateOf(false) }
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
        // Tự bắn screen_view mỗi khi đổi màn.
        TrackScreenViews(navController)
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = "splash"
        ) {
        composable("splash") {
            val activity = context as Activity
            SplashScreen(
                onFinish = {
                    // Lần đầu: vào intro trước Home (không chèn quảng cáo trước intro).
                    if (!onboardingDone) {
                        navController.navigate("intro") {
                            popUpTo("splash") { inclusive = true }
                        }
                        return@SplashScreen
                    }
                    Ads.showInterstitial(activity, AdNames.SPLASH_OPEN) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                },
                isAdReady = {
                    !RemoteConfigManager.adsEnabled() ||
                        Ads.isInterstitialReady(AdNames.SPLASH_OPEN)
                },
            )
        }
        composable("intro") {
            val scope = rememberCoroutineScope()
            val prefs = (context.applicationContext as TuViApplication).userPreferencesRepository
            IntroScreen(
                onFinish = {
                    scope.launch {
                        prefs.setOnboardingDone()
                        navController.navigate("home") {
                            popUpTo("intro") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("home") {
            val activity = context as Activity
            // Nạp trước ad cho dialog thoát + các interstitial từ Home → hiện tức thì khi cần.
            LaunchedEffect(Unit) {
                Ads.preload(
                    context,
                    AdNames.EXIT_NATIVE, AdNames.EXIT_BANNER,
                    AdNames.HOME_TUVI, AdNames.HOME_BROWSER, AdNames.HOME_CALENDAR,
                )
            }
            ExitAppHandler(onExit = { activity.finish() })
            HomeScreen(
                onOpenTuVi = {
                    Analytics.logEvent(Events.HOME_TILE_CLICK, mapOf(Events.P_TILE to "tuvi"))
                    Ads.showInterstitial(activity,AdNames.HOME_TUVI) {
                        navController.navigate("input")
                    }
                },
                onOpenBrowser = {
                    Analytics.logEvent(Events.HOME_TILE_CLICK, mapOf(Events.P_TILE to "browser"))
                    Ads.showInterstitial(activity,AdNames.HOME_BROWSER) {
                        val url = Uri.encode("https://www.google.com")
                        navController.navigate("browser?url=$url&title=Trình+Duyệt")
                    }
                },
                onOpenCalendar = {
                    Analytics.logEvent(Events.HOME_TILE_CLICK, mapOf(Events.P_TILE to "calendar"))
                    Ads.showInterstitial(activity,AdNames.HOME_CALENDAR) {
                        navController.navigate("lich")
                    }
                },
                onOpenSettings = { navController.navigate("settings") },
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
            // Native cho màn chọn ngôn ngữ (mở từ Settings) — nạp sẵn.
            LaunchedEffect(Unit) { Ads.preload(context, AdNames.LANGUAGE_NATIVE) }
            SettingsScreen(
                onBack = { navController.popBackStack() },
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
            LanguagePickerScreen(
                onBack = { navController.popBackStack() },
                onLanguageSaved = {
                    // Restart app từ splash để toàn bộ UI + ads load lại theo locale mới
                    val intent = Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            )
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
        composable("input") {
            val activity = context as Activity
            LaunchedEffect(Unit) { Ads.preload(context, AdNames.CHART_CREATE) }
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
                    Ads.showInterstitial(activity,AdNames.CHART_CREATE) {
                        navController.navigate("chart")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("chart") {
            val activity = context as Activity
            LaunchedEffect(Unit) { Ads.preload(context, AdNames.AI_OPEN, AdNames.CHART_DOWNLOAD) }
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
                        onOpenAiReading = {
                            Ads.showInterstitial(activity,AdNames.AI_OPEN) {
                                navController.navigate("ai_reading")
                            }
                        },
                        savedChartId = savedChartIdVm,
                        onBack = {
                            viewModel.resetState()
                            navController.popBackStack()
                        },
                        onSave = { nhom, onResult ->
                            viewModel.saveChart(nhom) { result ->
                                result
                                    .onSuccess { _ ->
                                        Analytics.logEvent(Events.CHART_SAVE)
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
                                        Analytics.logEvent(Events.CHART_UNSAVE)
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
            val activity = context as Activity
            LaunchedEffect(Unit) { Ads.preload(context, AdNames.AI_REQUEST) }
            val aiInterpretLoading by viewModel.aiInterpretLoading.collectAsStateWithLifecycle()
            val selectedCung by viewModel.selectedCung.collectAsStateWithLifecycle()
            val aiUsed by viewModel.aiUsed.collectAsStateWithLifecycle()
            val successState = uiState as? TuViUiState.Success
            val aiReadings = successState?.aiReadings ?: emptyMap()
            val vanHanReading = successState?.vanHanReading
            AiReadingScreen(
                selectedCung = selectedCung,
                aiReadings = aiReadings,
                loading = aiInterpretLoading,
                aiUsed = aiUsed,
                vanHanReading = vanHanReading,
                onSelectCung = { viewModel.selectCung(it) },
                onRequest = {
                    Ads.showInterstitial(activity,AdNames.AI_REQUEST) {
                        viewModel.fetchAiInterpretation(
                            cung = selectedCung,
                            onError = { err ->
                                Toast.makeText(context, err.resolve(context), Toast.LENGTH_LONG).show()
                            },
                        )
                    }
                },
                onRequestVanHan = {
                    Ads.showInterstitial(activity,AdNames.AI_REQUEST) {
                        viewModel.fetchVanHanInterpretation(
                            onError = { err ->
                                Toast.makeText(context, err.resolve(context), Toast.LENGTH_LONG).show()
                            },
                        )
                    }
                },
                onBack = { navController.popBackStack() },
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
