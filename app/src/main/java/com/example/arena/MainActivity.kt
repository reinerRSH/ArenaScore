package com.example.arena

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.arena.View.ui.theme.EliteAthleteOSTheme
import com.example.arena.navigation.AppNavigation
import com.example.arena.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


enum class LoginMode { ATHLETE, STAFF }
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        handleIntent(intent)

        // Animación de salida personalizada
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = splashScreenView.view.animate()
                .translationY(-splashScreenView.view.height.toFloat())
                .setInterpolator(AnticipateInterpolator())
                .setDuration(500L)

            slideUp.withEndAction { splashScreenView.remove() }
            slideUp.start()
        }

        setContent {
            EliteAthleteOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val target = intent?.getStringExtra("target_screen")
        val sedeId = intent?.getStringExtra("sede_id")
        if (target != null) {
            mainViewModel.triggerNavigation(target, sedeId)
            intent.removeExtra("target_screen")
            intent.removeExtra("sede_id")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
