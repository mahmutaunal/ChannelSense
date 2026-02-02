package com.mahmutalperenunal.channelsense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mahmutalperenunal.channelsense.ui.navigation.ChannelSenseNavGraph
import com.mahmutalperenunal.channelsense.ui.theme.ChannelSenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            ChannelSenseTheme {
                SetupSystemBars()
                ChannelSenseApp()
            }
        }
    }
}

@Composable
fun SetupSystemBars() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
}

@Composable
fun ChannelSenseApp() {
    ChannelSenseTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            ChannelSenseNavGraph(navController = navController)
        }
    }
}