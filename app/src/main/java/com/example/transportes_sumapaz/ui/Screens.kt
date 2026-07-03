package com.example.transportes_sumapaz.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transportes_sumapaz.data.LoginResult
import com.example.transportes_sumapaz.data.OccasionalTrip
import com.example.transportes_sumapaz.data.TransportesRepository
import com.example.transportes_sumapaz.data.Trip
import com.example.transportes_sumapaz.data.TripStatus
import java.util.Calendar
import java.util.GregorianCalendar

// Estados de pantalla de la aplicación
enum class Screen {
    WELCOME,
    LEADER_LOGIN,
    LEADER_CHANGE_PASSWORD,
    LEADER_DASHBOARD,
    LEADER_REGISTER_TRIP,
    LEADER_CALENDAR,
    USER_DASHBOARD,
    USER_REGISTER_OCCASIONAL,
    USER_REGISTER_ATTENDANCE
}

// Colores del sistema de diseño
val PrimaryBlue = Color(0xFF0F4C81)      // Azul clásico y premium
val LightBlue = Color(0xFF3B82F6)        // Azul moderno
val GradientStart = Color(0xFF1E3A8A)    // Azul oscuro profundo
val GradientEnd = Color(0xFF3B82F6)      // Azul brillante
val LightBackground = Color(0xFFF8FAFC)  // Fondo gris suave

val StatusGreen = Color(0xFF2E7D32)      // Cumplido
val StatusRed = Color(0xFFC62828)        // No cumplido
val StatusYellow = Color(0xFFE65100)     // Por cumplir (ámbar/naranja de buena visibilidad)

@Composable
fun TransportesSumapazApp() {
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                Screen.WELCOME -> WelcomeScreen(
                    onLeaderClick = { currentScreen = Screen.LEADER_LOGIN },
                    onUserClick = { currentScreen = Screen.USER_DASHBOARD }
                )
                Screen.LEADER_LOGIN -> LeaderLoginScreen(
                    onBack = { currentScreen = Screen.WELCOME },
                    onLoginSuccess = { result ->
                        if (result == LoginResult.MUST_CHANGE_PASSWORD) {
                            currentScreen = Screen.LEADER_CHANGE_PASSWORD
                        } else {
                            currentScreen = Screen.LEADER_DASHBOARD
                        }
                    }
                )
                Screen.LEADER_CHANGE_PASSWORD -> LeaderChangePasswordScreen(
                    onPasswordChanged = {
                        Toast.makeText(context, "Contraseña cambiada con éxito", Toast.LENGTH_LONG).show()
                        currentScreen = Screen.LEADER_DASHBOARD
                    },
                    onBack = {
                        TransportesRepository.logout()
                        currentScreen = Screen.WELCOME
                    }
                )
                Screen.LEADER_DASHBOARD -> LeaderDashboardScreen(
                    onRegisterTrip = { currentScreen = Screen.LEADER_REGISTER_TRIP },
                    onViewCalendar = { currentScreen = Screen.LEADER_CALENDAR },
                    onLogout = {
                        TransportesRepository.logout()
                        currentScreen = Screen.WELCOME
                    }
                )
                Screen.LEADER_REGISTER_TRIP -> LeaderRegisterTripScreen(
                    onBack = { currentScreen = Screen.LEADER_DASHBOARD }
                )
                Screen.LEADER_CALENDAR -> LeaderCalendarScreen(
                    onBack = { currentScreen = Screen.LEADER_DASHBOARD }
                )
                Screen.USER_DASHBOARD -> UserDashboardScreen(
                    onRegisterOccasional = { currentScreen = Screen.USER_REGISTER_OCCASIONAL },
                    onRegisterAttendance = { currentScreen = Screen.USER_REGISTER_ATTENDANCE },
                    onBack = { currentScreen = Screen.WELCOME }
                )
                Screen.USER_REGISTER_OCCASIONAL -> UserRegisterOccasionalTripScreen(
                    onBack = { currentScreen = Screen.USER_DASHBOARD }
                )
                Screen.USER_REGISTER_ATTENDANCE -> UserRegisterAttendanceScreen(
                    onBack = { currentScreen = Screen.USER_DASHBOARD }
                )
            }
        }
    }
}

/**
 * Pantalla de Bienvenida (Welcome)
 */
@Composable
fun WelcomeScreen(
    onLeaderClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logotipo decorativo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Transporte Logo",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Transportes Sumapaz",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Vínculo y Control de Rutas Municipales",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Botón Meta Líder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable { onLeaderClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Leader Icon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Ingreso Meta Líder",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Administración, agenda y control",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Usuario General
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable { onUserClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Icon",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Ingreso Usuario",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                            Text(
                                text = "Viajes ocasionales y asistencia",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
            
            Text(
                text = "Desarrollado para la comunidad de Sumapaz",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            )
        }
    }
}

/**
 * Pantalla de Login de Meta Líder
 */
@Composable
fun LeaderLoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: (LoginResult) -> Unit
) {
    var username by remember { mutableStateOf("lider") } // Pre-populado para facilidad de pruebas
    var password by remember { mutableStateOf("123") }  // Pre-populado para facilidad de pruebas
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Acceso Meta Líder",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        )
        Text(
            text = "Ingrese sus credenciales de administrador",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            label = { Text("Usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Por favor complete todos los campos"
                    return@Button
                }
                val result = TransportesRepository.loginLeader(username, password)
                if (result == LoginResult.USER_NOT_FOUND) {
                    errorMessage = "Usuario no registrado"
                } else if (result == LoginResult.WRONG_PASSWORD) {
                    errorMessage = "Contraseña incorrecta"
                } else {
                    onLoginSuccess(result)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Pantalla para Forzar el Cambio de Contraseña
 */
@Composable
fun LeaderChangePasswordScreen(
    onPasswordChanged: () -> Unit,
    onBack: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val leader = TransportesRepository.loggedLeader.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Cancelar",
                tint = PrimaryBlue
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Actualizar Contraseña",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        )
        Text(
            text = "Por motivos de seguridad, debe cambiar su contraseña inicial.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = StatusYellow.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, StatusYellow.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = StatusYellow)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Líder: ${leader?.name ?: "Administrador"}\nUsuario: ${leader?.username}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                errorMessage = null
            },
            label = { Text("Nueva Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text("Confirmar Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (newPassword.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "Por favor complete todos los campos"
                    return@Button
                }
                if (newPassword.length < 4) {
                    errorMessage = "La contraseña debe tener al menos 4 caracteres"
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    errorMessage = "Las contraseñas no coinciden"
                    return@Button
                }
                leader?.let {
                    val success = TransportesRepository.changeLeaderPassword(it.username, newPassword)
                    if (success) {
                        onPasswordChanged()
                    } else {
                        errorMessage = "Error al actualizar la contraseña"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Guardar y Continuar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Dashboard del Meta Líder
 */
@Composable
fun LeaderDashboardScreen(
    onRegisterTrip: () -> Unit,
    onViewCalendar: () -> Unit,
    onLogout: () -> Unit
) {
    val leader = TransportesRepository.loggedLeader.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola! ${leader?.name ?: "Meta Líder"}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                )
                Text(
                    text = "Panel de Gestión",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Cerrar Sesión",
                    tint = StatusRed
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Tarjeta Registrar Viaje
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRegisterTrip() }
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Registrar viaje",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Agende una nueva ruta, asigne pasajeros y defina su estado.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }

        // Tarjeta Viajes Agendados (Calendario)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewCalendar() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Viajes agendados",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Consulte el calendario de viajes. Verifique cumplimientos y estado de rutas.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }
    }
}

/**
 * Pantalla Registrar Viaje (Meta Líder)
 */
@Composable
fun LeaderRegisterTripScreen(
    onBack: () -> Unit
) {
    var route by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-07-03") } // Fecha por defecto
    var status by remember { mutableStateOf(TripStatus.POR_CUMPLIR) }
    var newPassengerName by remember { mutableStateOf("") }
    val passengerList = remember { mutableStateListOf<String>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Registrar Viaje",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = route,
                    onValueChange = {
                        route = it
                        errorMessage = null
                    },
                    label = { Text("Ruta / Destino (ej: Sumapaz a Melgar)") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        errorMessage = null
                    },
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = "Estado Inicial del Viaje:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TripStatus.values().forEach { state ->
                        val isSelected = status == state
                        val color = when (state) {
                            TripStatus.CUMPLIDO -> StatusGreen
                            TripStatus.NO_CUMPLIDO -> StatusRed
                            TripStatus.POR_CUMPLIR -> StatusYellow
                        }
                        Button(
                            onClick = { status = state },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) color else color.copy(alpha = 0.15f),
                                contentColor = if (isSelected) Color.White else color
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = state.name.replace("_", " "),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Registrar Pasajeros para este día:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPassengerName,
                        onValueChange = { newPassengerName = it },
                        label = { Text("Nombre del Pasajero") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newPassengerName.isNotBlank()) {
                                passengerList.add(newPassengerName.trim())
                                newPassengerName = ""
                            }
                        },
                        modifier = Modifier
                            .background(PrimaryBlue, CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
                    }
                }
            }

            if (passengerList.isEmpty()) {
                item {
                    Text(
                        text = "Sin pasajeros registrados. Añada pasajeros para este día.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(passengerList) { name ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { passengerList.remove(name) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusRed)
                            }
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = StatusRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (route.isBlank()) {
                    errorMessage = "Ingrese una ruta o destino"
                    return@Button
                }
                if (date.isBlank() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    errorMessage = "Ingrese una fecha válida (YYYY-MM-DD)"
                    return@Button
                }
                val success = TransportesRepository.addTrip(
                    route = route,
                    date = date,
                    status = status,
                    passengers = passengerList.toList()
                )
                if (success) {
                    Toast.makeText(context, "Viaje agendado correctamente", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    errorMessage = "Error al guardar el viaje"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Agendar Viaje",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Estructuras para la generación del Calendario
 */
data class CalendarDay(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val dateString: String
)

/**
 * Pantalla Viajes Agendados - Calendario (Meta Líder)
 */
@Composable
fun LeaderCalendarScreen(
    onBack: () -> Unit
) {
    var calendarYear by remember { mutableStateOf(2026) }
    var calendarMonth by remember { mutableStateOf(7) } // Julio de 2026 (Tiempo de simulación)
    
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    var selectedDate by remember { mutableStateOf("2026-07-03") }
    val allTrips = remember { derivedStateOf { TransportesRepository.getTrips() } }
    
    // Obtener los viajes del día seleccionado
    val tripsForSelectedDay = remember(selectedDate, allTrips.value) {
        allTrips.value.filter { it.date == selectedDate }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Cabecera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Viajes Agendados",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controles del Mes del Calendario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (calendarMonth == 1) {
                        calendarMonth = 12
                        calendarYear -= 1
                    } else {
                        calendarMonth -= 1
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mes anterior")
            }

            Text(
                text = "${monthNames[calendarMonth - 1]} $calendarYear",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    if (calendarMonth == 12) {
                        calendarMonth = 1
                        calendarYear += 1
                    } else {
                        calendarMonth += 1
                    }
                }
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente mes")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid del Calendario
        CalendarGrid(
            year = calendarYear,
            month = calendarMonth,
            trips = allTrips.value,
            selectedDate = selectedDate,
            onDaySelected = { dateStr -> selectedDate = dateStr }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Detalle del día seleccionado
        Text(
            text = "Detalles del día ($selectedDate):",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (tripsForSelectedDay.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay viajes programados en esta fecha.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tripsForSelectedDay) { trip ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            1.dp,
                            when (trip.status) {
                                TripStatus.CUMPLIDO -> StatusGreen.copy(alpha = 0.5f)
                                TripStatus.NO_CUMPLIDO -> StatusRed.copy(alpha = 0.5f)
                                TripStatus.POR_CUMPLIR -> StatusYellow.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trip.route,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                val statusLabel = when (trip.status) {
                                    TripStatus.CUMPLIDO -> "Cumplido"
                                    TripStatus.NO_CUMPLIDO -> "No cumplido"
                                    TripStatus.POR_CUMPLIR -> "Por cumplir"
                                }
                                val statusColor = when (trip.status) {
                                    TripStatus.CUMPLIDO -> StatusGreen
                                    TripStatus.NO_CUMPLIDO -> StatusRed
                                    TripStatus.POR_CUMPLIR -> StatusYellow
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f))
                                ) {
                                    Text(
                                        text = statusLabel,
                                        color = statusColor,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Nombres registrados (${trip.passengerNames.size}):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            
                            trip.passengerNames.forEach { name ->
                                val attended = trip.attendance.contains(name)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (attended) Icons.Default.CheckCircle else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (attended) StatusGreen else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (attended) StatusGreen else Color.Black,
                                            fontWeight = if (attended) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    if (attended) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "(Asistencia Registrada)",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = StatusGreen,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente Cuadrícula de Calendario
 */
@Composable
fun CalendarGrid(
    year: Int,
    month: Int,
    trips: List<Trip>,
    selectedDate: String,
    onDaySelected: (String) -> Unit
) {
    val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    // Calcular días
    val daysInMonth = when (month) {
        1 -> 31
        2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
        3 -> 31
        4 -> 30
        5 -> 31
        6 -> 30
        7 -> 31
        8 -> 31
        9 -> 30
        10 -> 31
        11 -> 30
        12 -> 31
        else -> 30
    }

    val startCal = GregorianCalendar(year, month - 1, 1)
    val dayOfWeekVal = startCal.get(Calendar.DAY_OF_WEEK)
    // Mapear Domingo = 6, Lunes = 0, ..., Sábado = 5
    val startDayOffset = when (dayOfWeekVal) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    val calendarDays = mutableListOf<CalendarDay>()
    
    // Relleno mes anterior
    for (i in 0 until startDayOffset) {
        calendarDays.add(CalendarDay(dayNumber = 0, isCurrentMonth = false, dateString = ""))
    }
    
    // Días del mes
    for (day in 1..daysInMonth) {
        val dateStr = String.format("%04d-%02d-%02d", year, month, day)
        calendarDays.add(CalendarDay(dayNumber = day, isCurrentMonth = true, dateString = dateStr))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Nombres días de la semana
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filas del calendario
            val rows = calendarDays.chunked(7)
            rows.forEach { rowDays ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0..6) {
                        if (i < rowDays.size) {
                            val day = rowDays[i]
                            if (day.dayNumber == 0) {
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                val isSelected = day.dateString == selectedDate
                                // Buscar viajes en este día para pintar colores
                                val tripsOnDay = trips.filter { it.date == day.dateString }
                                
                                // Color de fondo si hay viaje
                                val dayBgColor = if (tripsOnDay.isNotEmpty()) {
                                    // Si hay múltiples estados, priorizamos: POR_CUMPLIR > NO_CUMPLIR > CUMPLIDO
                                    val hasPending = tripsOnDay.any { it.status == TripStatus.POR_CUMPLIR }
                                    val hasFailed = tripsOnDay.any { it.status == TripStatus.NO_CUMPLIDO }
                                    val hasSuccess = tripsOnDay.any { it.status == TripStatus.CUMPLIDO }

                                    when {
                                        hasPending -> StatusYellow.copy(alpha = 0.2f)
                                        hasFailed -> StatusRed.copy(alpha = 0.2f)
                                        hasSuccess -> StatusGreen.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }
                                } else {
                                    Color.Transparent
                                }

                                val borderColor = if (isSelected) PrimaryBlue else Color.Transparent

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(dayBgColor)
                                        .border(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onDaySelected(day.dateString) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = day.dayNumber.toString(),
                                            fontWeight = if (isSelected || tripsOnDay.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) PrimaryBlue else Color.Black
                                        )

                                        // Puntos de color para marcar visualmente el estado debajo del número
                                        if (tripsOnDay.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                tripsOnDay.take(3).forEach { trip ->
                                                    val dotColor = when (trip.status) {
                                                        TripStatus.CUMPLIDO -> StatusGreen
                                                        TripStatus.NO_CUMPLIDO -> StatusRed
                                                        TripStatus.POR_CUMPLIR -> StatusYellow
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dashboard de Usuario
 */
@Composable
fun UserDashboardScreen(
    onRegisterOccasional: () -> Unit,
    onRegisterAttendance: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = PrimaryBlue)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Portal de Usuarios",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                )
                Text(
                    text = "Servicios y Asistencias Municipales",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Tarjeta Registrar Viaje Ocasional
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRegisterOccasional() }
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryBlue)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Registrar viaje ocasional",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Añadir un viaje excepcional que requiera realizar.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray)
            }
        }

        // Tarjeta Registrar Asistencia Alcaldía
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRegisterAttendance() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Registrar asistencia alcaldía",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Busque su nombre registrado por el líder y confirme su asistencia.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}

/**
 * Pantalla Registrar Viaje Ocasional (Usuario)
 */
@Composable
fun UserRegisterOccasionalTripScreen(
    onBack: () -> Unit
) {
    var passengerName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-07-03") }
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Viaje Ocasional",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = passengerName,
            onValueChange = {
                passengerName = it
                errorMessage = null
            },
            label = { Text("Nombre del Pasajero") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = date,
            onValueChange = {
                date = it
                errorMessage = null
            },
            label = { Text("Fecha (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = origin,
            onValueChange = {
                origin = it
                errorMessage = null
            },
            label = { Text("Origen / Vereda") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = {
                destination = it
                errorMessage = null
            },
            label = { Text("Destino") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (passengerName.isBlank() || origin.isBlank() || destination.isBlank()) {
                    errorMessage = "Por favor complete todos los campos"
                    return@Button
                }
                if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    errorMessage = "Ingrese una fecha válida (YYYY-MM-DD)"
                    return@Button
                }
                val success = TransportesRepository.addOccasionalTrip(
                    passengerName = passengerName,
                    date = date,
                    origin = origin,
                    destination = destination
                )
                if (success) {
                    Toast.makeText(context, "Viaje ocasional registrado con éxito", Toast.LENGTH_LONG).show()
                    onBack()
                } else {
                    errorMessage = "Error al registrar el viaje"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Registrar Viaje Ocasional",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Pantalla Registrar Asistencia Alcaldía (Usuario)
 */
@Composable
fun UserRegisterAttendanceScreen(
    onBack: () -> Unit
) {
    var searchDate by remember { mutableStateOf("2026-07-03") }
    var showResults by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    
    val allTrips = remember { derivedStateOf { TransportesRepository.getTrips() } }
    
    // Obtener la lista de viajes para la fecha buscada
    val tripsForDate = remember(searchDate, allTrips.value) {
        allTrips.value.filter { it.date == searchDate }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Asistencia Alcaldía",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de fecha para buscar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchDate,
                onValueChange = {
                    searchDate = it
                    showResults = false
                    selectedTrip = null
                },
                label = { Text("Fecha (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { showResults = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.height(54.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (showResults) {
            if (tripsForDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusYellow, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron viajes registrados por el Meta Líder para la fecha seleccionada ($searchDate).",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            } else {
                Text(
                    text = "Seleccione el viaje programado:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Si no hay viaje seleccionado, mostramos la lista de viajes de ese día
                if (selectedTrip == null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tripsForDate) { trip ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTrip = trip },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = trip.route,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${trip.passengerNames.size} nombres registrados para hoy",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = PrimaryBlue)
                                }
                            }
                        }
                    }
                } else {
                    // Viaje seleccionado: mostrar lista de nombres programados por el Meta Líder
                    val trip = selectedTrip!!
                    // Actualizar el estado del viaje refrescando del repositorio
                    val refreshedTrip = allTrips.value.find { it.id == trip.id } ?: trip

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = refreshedTrip.route,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Confirme su asistencia seleccionando su nombre",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                            TextButton(onClick = { selectedTrip = null }) {
                                Text("Cambiar viaje")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Listado de Nombres Programados:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (refreshedTrip.passengerNames.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "El Meta Líder no registró pasajeros específicos para este viaje.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(refreshedTrip.passengerNames) { name ->
                                val isAttended = refreshedTrip.attendance.contains(name)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(!isAttended) {
                                            val success = TransportesRepository.registerAttendance(refreshedTrip.id, name)
                                            if (success) {
                                                Toast.makeText(context, "Asistencia registrada para $name", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAttended) StatusGreen.copy(alpha = 0.08f) else Color.White
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isAttended) StatusGreen.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isAttended) Icons.Default.CheckCircle else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isAttended) StatusGreen else PrimaryBlue
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (isAttended) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }

                                        if (isAttended) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.15f))
                                            ) {
                                                Text(
                                                    text = "Confirmado",
                                                    color = StatusGreen,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    val success = TransportesRepository.registerAttendance(refreshedTrip.id, name)
                                                    if (success) {
                                                        Toast.makeText(context, "Asistencia registrada para $name", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Confirmar", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Estado inicial antes de hacer click en buscar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ingrese la fecha del viaje para consultar los nombres registrados.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}
