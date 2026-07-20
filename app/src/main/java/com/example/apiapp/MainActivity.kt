package com.example.apiapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.apiapp.shortcuts.AppShortcuts
import com.example.apiapp.ui.CharacterViewModel
import com.example.apiapp.ui.navigation.AppNavHost
import com.example.apiapp.ui.theme.ApiAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val shortcutDestination = intent?.getStringExtra(AppShortcuts.EXTRA_DESTINATION)

        setContent {
            ApiAppTheme {
                val viewModel: CharacterViewModel = hiltViewModel()
                AppNavHost(viewModel = viewModel, initialDestination = shortcutDestination)
            }
        }
    }
}
