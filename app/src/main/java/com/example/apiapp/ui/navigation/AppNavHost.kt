package com.example.apiapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apiapp.shortcuts.AppShortcuts
import com.example.apiapp.ui.CharacterDetailScreen
import com.example.apiapp.ui.CharacterScreen
import com.example.apiapp.ui.CharacterViewModel
import com.example.apiapp.ui.StatsScreen

@Composable
fun AppNavHost(viewModel: CharacterViewModel, initialDestination: String? = null) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Screen.CharacterList.route || currentRoute == Screen.Stats.route

    LaunchedEffect(initialDestination) {
        when (initialDestination) {
            AppShortcuts.DESTINATION_STATS -> navController.navigate(Screen.Stats.route)
            AppShortcuts.DESTINATION_FAVORITES -> viewModel.toggleFavoritesOnly()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.CharacterList.route,
                        onClick = {
                            navController.navigate(Screen.CharacterList.route) {
                                popUpTo(Screen.CharacterList.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Explorar") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Stats.route,
                        onClick = {
                            navController.navigate(Screen.Stats.route) {
                                popUpTo(Screen.CharacterList.route)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("Estadísticas") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.CharacterList.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.CharacterList.route) {
                CharacterScreen(
                    viewModel = viewModel,
                    onCharacterClick = { characterId ->
                        navController.navigate(Screen.CharacterDetail.createRoute(characterId))
                    },
                    onCompareRequested = { firstId, secondId ->
                        navController.navigate(Screen.Compare.createRoute(firstId, secondId))
                    }
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.CharacterDetail.route,
                arguments = listOf(navArgument(Screen.CharacterDetail.ARG_CHARACTER_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val characterId = backStackEntry.arguments?.getInt(Screen.CharacterDetail.ARG_CHARACTER_ID)
                val allCharacters by viewModel.allCharacters.collectAsState()
                val character = allCharacters.find { it.id == characterId }
                val episodeState by viewModel.episodeState.collectAsState()

                LaunchedEffect(characterId) {
                    character?.let { viewModel.loadEpisodes(it) }
                }

                if (character != null) {
                    CharacterDetailScreen(
                        character = character,
                        episodeState = episodeState,
                        onBack = { navController.popBackStack() },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
            }

            composable(
                route = Screen.Compare.route,
                arguments = listOf(
                    navArgument(Screen.Compare.ARG_FIRST_ID) { type = NavType.IntType },
                    navArgument(Screen.Compare.ARG_SECOND_ID) { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val firstId = backStackEntry.arguments?.getInt(Screen.Compare.ARG_FIRST_ID)
                val secondId = backStackEntry.arguments?.getInt(Screen.Compare.ARG_SECOND_ID)
                val allCharacters by viewModel.allCharacters.collectAsState()
                val first = allCharacters.find { it.id == firstId }
                val second = allCharacters.find { it.id == secondId }

                if (first != null && second != null) {
                    com.example.apiapp.ui.CompareScreen(
                        first = first,
                        second = second,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
