package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viewmodels.ReadingViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Reading : Screen("reading/{textId}") {
        fun createRoute(textId: String) = "reading/$textId"
    }
    object Quiz : Screen("quiz/{textId}/{duration}/{mistakes}") {
        fun createRoute(textId: String, duration: Int, mistakes: Int) = "quiz/$textId/$duration/$mistakes"
    }
    object Dashboard : Screen("dashboard")
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    viewModel: ReadingViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToReading = { textId ->
                    navController.navigate(Screen.Reading.createRoute(textId))
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route)
                }
            )
        }
        composable(
            route = Screen.Reading.route,
            arguments = listOf(navArgument("textId") { type = NavType.StringType })
        ) { backStackEntry ->
            val textId = backStackEntry.arguments?.getString("textId") ?: ""
            ReadingScreen(
                textId = textId,
                viewModel = viewModel,
                onReadingFinished = { durationSecs, mistakes ->
                    navController.navigate(Screen.Quiz.createRoute(textId, durationSecs, mistakes)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("textId") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType },
                navArgument("mistakes") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val textId = backStackEntry.arguments?.getString("textId") ?: ""
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val mistakes = backStackEntry.arguments?.getInt("mistakes") ?: 0
            
            QuizScreen(
                textId = textId,
                durationSecs = duration,
                mistakes = mistakes,
                viewModel = viewModel,
                onQuizComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            ParentDashboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
