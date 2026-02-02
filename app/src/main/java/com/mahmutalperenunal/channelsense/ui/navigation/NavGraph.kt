package com.mahmutalperenunal.channelsense.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mahmutalperenunal.channelsense.feature.analyzer.ui.AnalyzerScreen
import com.mahmutalperenunal.channelsense.feature.guide.ui.ChannelGuideScreen
import com.mahmutalperenunal.channelsense.feature.settings.ui.SettingsScreen

@Composable
fun ChannelSenseNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.ANALYZER
    ) {
        composable(route = Destinations.ANALYZER) {
            AnalyzerScreen(
                onChannelSelected = { channel ->
                    navController.navigate("guide/$channel")
                },
                onOpenSettings = {
                    navController.navigate(Destinations.SETTINGS)
                }
            )
        }

        composable(
            route = Destinations.GUIDE_WITH_ARGS,
            arguments = listOf(
                navArgument(Destinations.Args.CHANNEL) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val channel = backStackEntry.arguments?.getInt(Destinations.Args.CHANNEL) ?: 1
            ChannelGuideScreen(
                channel = channel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Destinations.SETTINGS) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}