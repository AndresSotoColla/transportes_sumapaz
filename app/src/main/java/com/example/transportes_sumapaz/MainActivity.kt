package com.example.transportes_sumapaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.transportes_sumapaz.data.TransportesRepository
import com.example.transportes_sumapaz.ui.TransportesSumapazApp
import com.example.transportes_sumapaz.ui.theme.Transportes_sumapazTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar repositorio con el contexto de la aplicación para persistencia local de usuarios
        TransportesRepository.initialize(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            Transportes_sumapazTheme {
                TransportesSumapazApp()
            }
        }
    }
}