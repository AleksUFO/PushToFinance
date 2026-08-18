package com.pushtofinance.infinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pushtofinance.infinapp.ui.CaptureSheet
import com.pushtofinance.infinapp.ui.CaptureViewModel
import com.pushtofinance.infinapp.ui.theme.PushToFinanceTheme

class CaptureActivity : ComponentActivity() {

    private val captureVm: CaptureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePref by captureVm.theme.collectAsState()
            val dark = when (themePref) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }
            PushToFinanceTheme(darkTheme = dark) {
                CaptureSheet(
                    vm = captureVm,
                    onDismiss = { finish() },
                    onSaved = { finish() }
                )
            }
        }
    }
}