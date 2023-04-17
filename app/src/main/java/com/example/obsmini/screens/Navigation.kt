package com.example.obsmini.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


// TODO passing viewModel down as parameter is considered bad practice, find better alternative?

@Composable
fun MyNavigation() {

    val viewModel: MyViewModel = hiltViewModel()
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) { MainScreen(navController, viewModel) }
        composable(Screen.Settings.route) { SettingsScreen(navController, viewModel) }
        composable(Screen.SavedTracks.route) { SavedTrackScreen(navController, viewModel) }
    }
}


sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object SavedTracks : Screen("savedTracks")
}