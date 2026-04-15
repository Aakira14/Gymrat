package com.gymrat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gymrat.app.ui.theme.GymRatTheme
import com.gymrat.app.ui.GymRatApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymRatTheme {
                GymRatApp()
            }
        }
    }
}
