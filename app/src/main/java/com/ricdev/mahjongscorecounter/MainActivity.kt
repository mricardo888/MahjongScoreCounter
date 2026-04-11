package com.ricdev.mahjongscorecounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ricdev.mahjongscorecounter.ui.MahjongApp
import com.ricdev.mahjongscorecounter.ui.theme.MahjongScoreCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MahjongScoreCounterTheme {
                MahjongApp()
            }
        }
    }
}
