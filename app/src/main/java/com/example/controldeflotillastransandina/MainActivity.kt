package com.example.controldeflotillastransandina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.controldeflotillastransandina.core.AppContainer
import com.example.controldeflotillastransandina.ui.TransAndinaApp
import com.example.controldeflotillastransandina.ui.theme.ControlDeFlotillasTransAndinaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppContainer.init(this)
        setContent {
            ControlDeFlotillasTransAndinaTheme {
                TransAndinaApp()
            }
        }
    }
}