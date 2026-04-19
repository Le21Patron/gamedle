package com.sefa.loldle_karakter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sefa.loldle_karakter.data.GameRepository
import com.sefa.loldle_karakter.data.GameViewModel
import com.sefa.loldle_karakter.data.GameViewModelFactory
import com.sefa.loldle_karakter.data.UserPreferencesRepository
import com.sefa.loldle_karakter.ui.GameScreen
import com.sefa.loldle_karakter.ui.GameSelectionScreen
import com.sefa.loldle_karakter.ui.LolLoreLibraryScreen
import com.sefa.loldle_karakter.ui.MinecraftGameScreen
import com.sefa.loldle_karakter.ui.theme.LoldleKarakterTheme

class MainActivity : ComponentActivity() {

    private lateinit var userPrefsRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        userPrefsRepository = UserPreferencesRepository(applicationContext)

        setContent {
            LoldleKarakterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AppNavigation(userPrefsRepository = userPrefsRepository)
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object GameSelection : Screen("game_selection")
    object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    object Minecraft : Screen("minecraft")
    object MinecraftDaily : Screen("minecraft-daily")
    object LolLore : Screen("lol-lore")
}

@Composable
fun AppNavigation(
    userPrefsRepository: UserPreferencesRepository
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val gameRepository = remember { GameRepository(context) }

    NavHost(navController = navController, startDestination = Screen.GameSelection.route) {

        composable(route = Screen.GameSelection.route) {
            GameSelectionScreen(
                userPrefsRepository = userPrefsRepository,
                onGameSelected = { gameId ->
                    when (gameId) {
                        "minecraft" -> navController.navigate(Screen.Minecraft.route)
                        "minecraft-daily" -> navController.navigate(Screen.MinecraftDaily.route)
                        "lol-lore" -> navController.navigate(Screen.LolLore.route)
                        else -> navController.navigate(Screen.Game.createRoute(gameId))
                    }
                }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")

            if (gameId != null) {
                val viewModel: GameViewModel = viewModel(
                    factory = GameViewModelFactory(
                        repository = gameRepository,
                        prefsRepository = userPrefsRepository,
                        gameId = gameId
                    )
                )

                GameScreen(
                    gameId = gameId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(route = Screen.Minecraft.route) {
            MinecraftGameScreen(
                onNavigateBack = { navController.popBackStack() },
                isDailyMode = false
            )
        }

        composable(route = Screen.MinecraftDaily.route) {
            MinecraftGameScreen(
                onNavigateBack = { navController.popBackStack() },
                isDailyMode = true,
                userPrefsRepository = userPrefsRepository
            )
        }

        composable(route = Screen.LolLore.route) {
            LolLoreLibraryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}