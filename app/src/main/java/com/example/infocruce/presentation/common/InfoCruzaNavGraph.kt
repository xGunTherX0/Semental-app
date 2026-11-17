package com.example.infocruce.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.infocruce.InfoCruzaApplication
import com.example.infocruce.presentation.catalog.CatalogScreen
import com.example.infocruce.presentation.simulator.CrossSimulatorScreen

object Routes {
    const val HOME = "home"
    const val SIMULATOR = "simulator"
    const val CATALOG = "catalog"

    /** Argumento de navegación con el caballo elegido en el catálogo. */
    const val ARG_HORSE_ID = "horseId"

    /** Ruta del simulador con el caballo preseleccionado. */
    const val SIMULATOR_ROUTE = "$SIMULATOR?$ARG_HORSE_ID={$ARG_HORSE_ID}"

    /**
     * Construye la ruta del simulador. [horseId] = -1 indica "sin preselección".
     * Se navega siempre con el argumento explícito para no depender de la
     * opcionalidad de los query params de Navigation.
     */
    fun simulator(horseId: Long = -1L): String = "$SIMULATOR?$ARG_HORSE_ID=$horseId"
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
                onNavigateToSimulator = { navController.navigate(Routes.simulator()) },
                onNavigateToCatalog = { navController.navigate(Routes.CATALOG) }
            )
        }
        composable(
            route = Routes.SIMULATOR_ROUTE,
            arguments = listOf(
                navArgument(Routes.ARG_HORSE_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val horseId = entry.arguments?.getLong(Routes.ARG_HORSE_ID) ?: -1L
            CrossSimulatorScreen(
                onBackClick = { navController.popBackStack() },
                repository = repository,
                session = session,
                preselectedHorseId = horseId
            )
        }
        composable(Routes.CATALOG) {
            CatalogScreen(
                onBackClick = { navController.popBackStack() },
                onEvaluateClick = { horseId -> navController.navigate(Routes.simulator(horseId)) },
                repository = repository,
                session = session
            )
        }
    }
}
