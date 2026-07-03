package com.example.transportes_sumapaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.transportes_sumapaz.ui.TransportesSumapazApp
import com.example.transportes_sumapaz.ui.theme.Transportes_sumapazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Transportes_sumapazTheme {
                TransportesSumapazApp()
            }
        }
    }
}