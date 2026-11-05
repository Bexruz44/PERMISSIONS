package uz.ttpu.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import uz.ttpu.permissions.screens.CameraDetailScreen
import uz.ttpu.permissions.screens.LocationDetailScreen
import uz.ttpu.permissions.screens.MediaDetailScreen
import uz.ttpu.permissions.screens.SettingsScreen
import uz.ttpu.permissions.ui.theme.PERMISSIONSTheme

sealed class Screen {
    object Settings : Screen()
    object Camera : Screen()
    object Location : Screen()
    object Media : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PERMISSIONSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Settings) }

                    when (currentScreen) {
                        is Screen.Settings -> {
                            SettingsScreen(
                                onNavigateToCamera = { currentScreen = Screen.Camera },
                                onNavigateToLocation = { currentScreen = Screen.Location },
                                onNavigateToMedia = { currentScreen = Screen.Media }
                            )
                        }
                        is Screen.Camera -> {
                            CameraDetailScreen(
                                onBackClick = { currentScreen = Screen.Settings }
                            )
                        }
                        is Screen.Location -> {
                            LocationDetailScreen(
                                onBackClick = { currentScreen = Screen.Settings }
                            )
                        }
                        is Screen.Media -> {
                            MediaDetailScreen(
                                onBackClick = { currentScreen = Screen.Settings }
                            )
                        }
                    }
                }
            }
        }
    }
}
