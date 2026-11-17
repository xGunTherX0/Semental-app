package com.example.infocruce.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.infocruce.InfoCruzaApplication
import com.example.infocruce.presentation.catalog.CatalogScreen
import com.example.infocruce.presentation.simulator.CrossSimulatorScreen

object Routes {
    const val HOME = "home"
    const val SIMULATOR = "simulator"
    const val CATALOG = "catalog"
}

@Composable
fun InfoCruzaNavGraph(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as InfoCruzaApplication
    val repository = app.repository
    val session = app.session
    val currentUser by session.currentUser.collectAsState()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                currentUser = currentUser,
                onRoleSelected = session::selectRole,
                onNavigateToSimulator = { navController.navigate(Routes.SIMULATOR) },
                onNavigateToCatalog = { navController.navigate(Routes.CATALOG) }
            )
        }
        composable(Routes.SIMULATOR) {
            CrossSimulatorScreen(
                onBackClick = { navController.popBackStack() },
                repository = repository,
                session = session
            )
        }
        composable(Routes.CATALOG) {
            CatalogScreen(
                onBackClick = { navController.popBackStack() },
                onEvaluateClick = { navController.navigate(Routes.SIMULATOR) },
                repository = repository,
                session = session
            )
        }
    }
}
