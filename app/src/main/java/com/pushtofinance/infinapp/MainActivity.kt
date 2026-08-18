package com.pushtofinance.infinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.pushtofinance.infinapp.ui.AppNavHost
import com.pushtofinance.infinapp.ui.MainViewModel
import com.pushtofinance.infinapp.ui.OnboardingScreen
import com.pushtofinance.infinapp.ui.theme.PushToFinanceTheme

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePref by vm.theme.collectAsState()
            val dark = when (themePref) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            PushToFinanceTheme(darkTheme = dark) {
                val onboarded by vm.onboarded.collectAsState()
                if (!onboarded) {
                    OnboardingScreen(vm)
                } else {
                    AppNavHost(vm)
                }
            }
        }
    }
}