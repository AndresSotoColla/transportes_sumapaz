package com.example.transportes_sumapaz.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transportes_sumapaz.data.LoginResult
import com.example.transportes_sumapaz.data.OccasionalTrip
import com.example.transportes_sumapaz.data.Participant
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
    USER_TRIP_DETAILS,
    USER_REGISTER_OCCASIONAL
}

// Colores del sistema de diseño (Paleta Slate & Teal Premium)
val PrimaryBlue = Color(0xFF0F766E)      // Teal Oscuro Corporativo (Teal 700)
val LightBlue = Color(0xFF14B8A6)        // Teal Claro / Menta (Teal 500)
val GradientStart = Color(0xFF0F172A)    // Slate Oscuro Profundo (Slate 900)
val GradientEnd = Color(0xFF1E293B)      // Slate Corporativo (Slate 800)
val LightBackground = Color(0xFFF1F5F9)  // Fondo Gris Azulado Suave (Slate 100)

val StatusGreen = Color(0xFF10B981)      // Emerald (Verde Moderno)
val StatusRed = Color(0xFFEF4444)        // Rose Red (Rojo Moderno)
val StatusYellow = Color(0xFFF59E0B)     // Amber Amarillo/Naranja (Iniciado)
val StatusScheduled = Color(0xFF94A3B8)  // Programado (Slate Gris)

/**
 * Retorna la paleta de colores de textos para asegurar que el texto sea siempre negro
 * y de alta visibilidad, incluso cuando los inputs estén deshabilitados o en modo lectura.
 */
@Composable
fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    disabledTextColor = Color.Black,
    focusedLabelColor = Color.Black,
    unfocusedLabelColor = Color.Black.copy(alpha = 0.7f),
    disabledLabelColor = Color.Black.copy(alpha = 0.7f),
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = Color.LightGray,
    disabledBorderColor = Color.LightGray,
    focusedLeadingIconColor = PrimaryBlue,
    unfocusedLeadingIconColor = Color.Gray,
    disabledLeadingIconColor = Color.Gray
)

@Composable
fun TransportesSumapazApp() {
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var loggedUserCedula by remember { mutableStateOf("") }
    var selectedTripForDetails by remember { mutableStateOf<Trip?>(null) }
    val context = LocalContext.current

    // Interceptor del botón "Atrás" del celular
    BackHandler(enabled = true) {
        when (currentScreen) {
            Screen.WELCOME -> {
                Toast.makeText(context, "Use los botones de la interfaz para navegar", Toast.LENGTH_SHORT).show()
            }
            Screen.LEADER_LOGIN -> { currentScreen = Screen.WELCOME }
            Screen.LEADER_CHANGE_PASSWORD -> {
                TransportesRepository.logout()
                currentScreen = Screen.WELCOME
            }
            Screen.LEADER_DASHBOARD -> {
                TransportesRepository.logout()
                currentScreen = Screen.WELCOME
            }
            Screen.LEADER_REGISTER_TRIP -> { currentScreen = Screen.LEADER_DASHBOARD }
            Screen.LEADER_CALENDAR -> { currentScreen = Screen.LEADER_DASHBOARD }
            Screen.USER_DASHBOARD -> { currentScreen = Screen.WELCOME }
            Screen.USER_TRIP_DETAILS -> { currentScreen = Screen.USER_DASHBOARD }
            Screen.USER_REGISTER_OCCASIONAL -> { currentScreen = Screen.USER_DASHBOARD }
        }
    }

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
                    onBack = { currentScreen = Screen.WELCOME },
                    onRegisterOccasional = { currentScreen = Screen.USER_REGISTER_OCCASIONAL },
                    onLoggedIn = { cedula ->
                        loggedUserCedula = cedula
                    },
                    onSelectTrip = { trip ->
                        selectedTripForDetails = trip
                        currentScreen = Screen.USER_TRIP_DETAILS
                    }
                )
                Screen.USER_TRIP_DETAILS -> UserTripDetailsScreen(
                    trip = selectedTripForDetails,
                    loggedCedula = loggedUserCedula,
                    onBack = { currentScreen = Screen.USER_DASHBOARD }
                )
                Screen.USER_REGISTER_OCCASIONAL -> UserRegisterOccasionalTripScreen(
                    onBack = { currentScreen = Screen.USER_DASHBOARD }
                )
            }
        }
    }
}

/**
 * Componente Reutilizable de Selección de Fecha con Diálogo de Calendario Nativo (DatePicker)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        enabled = false,
        colors = getTextFieldColors()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis != null) {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }
                            onDateSelected(sdf.format(java.util.Date(selectedDateMillis)))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Componente Reutilizable de Selección de Hora en Formato Militar (24h)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelector(
    label: String,
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true)

    OutlinedTextField(
        value = selectedTime,
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showTimePicker = true },
        enabled = false,
        colors = getTextFieldColors()
    )

    if (showTimePicker && enabled) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        onTimeSelected(String.format("%02d:%02d", hour, minute))
                        showTimePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Seleccione Hora (Formato 24h)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    TimePicker(state = timePickerState)
                }
            }
        )
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
            // Logotipo decorativo personalizado
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.transportes_sumapaz.R.drawable.app_logo),
                    contentDescription = "Logo Transportes Sumapaz",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
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
                                text = "Viajes programados, asistencia y ocasionales",
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
    var username by remember { mutableStateOf("lider") }
    var password by remember { mutableStateOf("123") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            singleLine = true,
            colors = getTextFieldColors()
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
            singleLine = true,
            colors = getTextFieldColors()
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
 * Pantalla para Cambio de Contraseña Obligatorio
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
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black
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
            singleLine = true,
            colors = getTextFieldColors()
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
            singleLine = true,
            colors = getTextFieldColors()
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
 * Dashboard de Meta Líder
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

        // Tarjeta Programar Viaje
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
                    text = "Agende una ruta a Betania o San Juan y asigne participantes.",
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
                    text = "Consulte el calendario de rutas programadas y su cumplimiento.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }
    }
}

/**
 * Pantalla de Registro de Viajes (Meta Líder)
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LeaderRegisterTripScreen(
    onBack: () -> Unit
) {
    var route by remember { mutableStateOf("Sede Betania") } // Restringido
    var date by remember { mutableStateOf("2026-07-03") }
    
    // Formulario de Participante
    var nameInput by remember { mutableStateOf("") }
    var docType by remember { mutableStateOf("Cédula de Ciudadanía") }
    var docNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var projectNumber by remember { mutableStateOf("") }
    
    var isExistingParticipant by remember { mutableStateOf(false) }
    val passengerList = remember { mutableStateListOf<Participant>() }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Autocomplete Matches based on nameInput
    val autocompleteSuggestions = remember(nameInput) {
        if (nameInput.isBlank()) emptyList()
        else TransportesRepository.globalParticipants.filter {
            it.name.contains(nameInput, ignoreCase = true) && !passengerList.any { p -> p.docNumber == it.docNumber }
        }
    }

    // Dropdown de Tipo de Documento
    var expandedDocType by remember { mutableStateOf(false) }
    val docTypes = listOf("Cédula de Ciudadanía", "Cédula de Extranjería", "Pasaporte")

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
                text = "Programar Viaje",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fecha y Ruta
            item {
                Spacer(modifier = Modifier.height(8.dp))
                DateSelector(
                    label = "Fecha de Viaje",
                    selectedDate = date,
                    onDateSelected = { date = it }
                )
            }

            item {
                Text(
                    text = "Seleccione Destino de Asistencia:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val routes = listOf("Sede Betania", "Sede San Juan")
                    routes.forEach { r ->
                        val isSelected = route == r
                        Button(
                            onClick = { route = r },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.3f),
                                contentColor = if (isSelected) Color.White else PrimaryBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = r, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Sección de Agregar Pasajeros
            item {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Agregar Participante al Viaje:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
            }

            item {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        val matched = TransportesRepository.getParticipantByName(it)
                        if (matched == null) {
                            isExistingParticipant = false
                        }
                    },
                    label = { Text("Nombre del Participante") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = getTextFieldColors()
                )
            }

            // Sugerencias de Autocompletado
            if (autocompleteSuggestions.isNotEmpty() && !isExistingParticipant) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Sugerencias encontradas (toca para autocompletar):",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            autocompleteSuggestions.take(3).forEach { participant ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            nameInput = participant.name
                                            docType = participant.docType
                                            docNumber = participant.docNumber
                                            phone = participant.phone
                                            email = participant.email
                                            projectNumber = participant.projectNumber
                                            isExistingParticipant = true
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${participant.name} (${participant.docNumber})",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Si es un participante nuevo, pedimos el resto de datos
            item {
                AnimatedVisibility(visible = nameInput.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExistingParticipant) StatusGreen.copy(alpha = 0.05f) else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isExistingParticipant) StatusGreen.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (isExistingParticipant) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Participante Registrado en la Base de Datos",
                                        color = StatusGreen,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Participante nuevo. Digite los datos faltantes:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StatusYellow
                                    )
                                )
                            }

                            // Selector tipo de documento (Cédula de ciudadanía, extranjería, pasaporte)
                            ExposedDropdownMenuBox(
                                expanded = expandedDocType && !isExistingParticipant,
                                onExpandedChange = { if (!isExistingParticipant) expandedDocType = it }
                            ) {
                                OutlinedTextField(
                                    value = docType,
                                    onValueChange = {},
                                    label = { Text("Tipo de Documento") },
                                    readOnly = true,
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = getTextFieldColors(),
                                    enabled = !isExistingParticipant
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDocType && !isExistingParticipant,
                                    onDismissRequest = { expandedDocType = false }
                                ) {
                                    docTypes.forEach { selection ->
                                        DropdownMenuItem(
                                            text = { Text(selection) },
                                            onClick = {
                                                docType = selection
                                                expandedDocType = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = docNumber,
                                onValueChange = { if (!isExistingParticipant) docNumber = it },
                                label = { Text("Cédula / Documento") },
                                readOnly = isExistingParticipant,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = getTextFieldColors(),
                                enabled = !isExistingParticipant
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { if (!isExistingParticipant) phone = it },
                                label = { Text("Teléfono") },
                                readOnly = isExistingParticipant,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = getTextFieldColors(),
                                enabled = !isExistingParticipant
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { if (!isExistingParticipant) email = it },
                                label = { Text("Correo Electrónico") },
                                readOnly = isExistingParticipant,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = getTextFieldColors(),
                                enabled = !isExistingParticipant
                            )

                            OutlinedTextField(
                                value = projectNumber,
                                onValueChange = { if (!isExistingParticipant) projectNumber = it },
                                label = { Text("Número del Proyecto") },
                                readOnly = isExistingParticipant,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = getTextFieldColors(),
                                enabled = !isExistingParticipant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (nameInput.isBlank() || docNumber.isBlank() || projectNumber.isBlank()) {
                                        Toast.makeText(context, "Nombre, Cédula y Proyecto son obligatorios", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    val participant = Participant(
                                        name = nameInput.trim(),
                                        docType = docType.trim(),
                                        docNumber = docNumber.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        projectNumber = projectNumber.trim()
                                    )
                                    
                                    if (passengerList.any { it.docNumber == participant.docNumber }) {
                                        Toast.makeText(context, "El participante ya está agregado al viaje", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    // Registrar en BD global si no existe
                                    if (!isExistingParticipant) {
                                        TransportesRepository.registerParticipant(participant)
                                    }
                                    
                                    passengerList.add(participant)
                                    
                                    // Limpiar inputs
                                    nameInput = ""
                                    docNumber = ""
                                    phone = ""
                                    email = ""
                                    projectNumber = ""
                                    isExistingParticipant = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text("Añadir al Viaje")
                            }
                        }
                    }
                }
            }

            // Listado de pasajeros agregados al viaje actual
            item {
                Text(
                    text = "Pasajeros Agendados (${passengerList.size}):",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black
                )
            }

            if (passengerList.isEmpty()) {
                item {
                    Text(
                        text = "Ningún pasajero programado para este viaje.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            } else {
                items(passengerList) { passenger ->
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
                            Column {
                                Text(text = passenger.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                Text(
                                    text = "Cédula: ${passenger.docNumber} • Proy: ${passenger.projectNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                            IconButton(onClick = { passengerList.remove(passenger) }) {
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
                if (passengerList.isEmpty()) {
                    errorMessage = "Debe agregar al menos un pasajero al viaje"
                    return@Button
                }
                val success = TransportesRepository.addTrip(
                    route = route,
                    date = date,
                    passengers = passengerList.toList()
                )
                if (success) {
                    Toast.makeText(context, "Viaje agendado correctamente en $route", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    errorMessage = "Error al agendar el viaje"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Agendar e Iniciar Viaje",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * Pantalla Viajes Agendados - Calendario y Manifiesto Detallado (Meta Líder)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderCalendarScreen(
    onBack: () -> Unit
) {
    var calendarYear by remember { mutableStateOf(2026) }
    var calendarMonth by remember { mutableStateOf(7) } // Julio de 2026
    
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    var selectedDate by remember { mutableStateOf("2026-07-03") }
    val allTrips = remember { derivedStateOf { TransportesRepository.getTrips() } }
    
    val tripsForSelectedDay = remember(selectedDate, allTrips.value) {
        allTrips.value.filter { it.date == selectedDate }
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
                text = "Viajes Agendados",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                textAlign = TextAlign.Center,
                color = Color.Black
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

        CalendarGrid(
            year = calendarYear,
            month = calendarMonth,
            trips = allTrips.value,
            selectedDate = selectedDate,
            onDaySelected = { dateStr -> selectedDate = dateStr }
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "No hay viajes agendados.", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tripsForSelectedDay) { trip ->
                    val isManifestLocked = trip.attendanceRecords.isNotEmpty()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(
                            1.dp,
                            when (trip.status) {
                                TripStatus.CUMPLIDO -> StatusGreen.copy(alpha = 0.5f)
                                TripStatus.NO_CUMPLIDO -> StatusRed.copy(alpha = 0.5f)
                                TripStatus.INICIADO -> StatusYellow.copy(alpha = 0.5f)
                                TripStatus.POR_CUMPLIR -> StatusScheduled.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Encabezado de Ruta y Estado Global
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ruta: ${trip.route}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f),
                                    color = Color.Black
                                )
                                val statusLabel = when (trip.status) {
                                    TripStatus.CUMPLIDO -> "Cumplido"
                                    TripStatus.NO_CUMPLIDO -> "No cumplido"
                                    TripStatus.INICIADO -> "Iniciado"
                                    TripStatus.POR_CUMPLIR -> "Programado"
                                }
                                val statusColor = when (trip.status) {
                                    TripStatus.CUMPLIDO -> StatusGreen
                                    TripStatus.NO_CUMPLIDO -> StatusRed
                                    TripStatus.INICIADO -> StatusYellow
                                    TripStatus.POR_CUMPLIR -> StatusScheduled
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

                            Spacer(modifier = Modifier.height(8.dp))

                            // Banner sobre la edición del manifiesto
                            if (isManifestLocked) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StatusYellow.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, StatusYellow.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = StatusYellow, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Manifiesto cerrado: El viaje ya ha sido iniciado por uno o más pasajeros.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            color = StatusYellow
                                        )
                                    }
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.05f)),
                                    border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Manifiesto abierto: Puede añadir o quitar personas.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            color = StatusGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // --- CATEGORÍA 1: NO INICIADOS / PENDIENTES ---
                            val pendingPassengers = trip.passengers.filter { p ->
                                trip.attendanceRecords.none { it.passengerCedula == p.docNumber }
                            }
                            Text(
                                text = "1. No Iniciados / Pendientes (${pendingPassengers.size}):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            pendingPassengers.forEach { passenger ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = passenger.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                            Text(text = "Cédula: ${passenger.docNumber} • Proy: ${passenger.projectNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // El líder puede forzar el inicio
                                            IconButton(onClick = {
                                                TransportesRepository.updateAttendanceStatus(trip.id, passenger.docNumber, TripStatus.INICIADO)
                                                Toast.makeText(context, "Estado de ${passenger.name} forzado a INICIADO", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Forzar Inicio", tint = StatusYellow)
                                            }
                                            // O sacar de viaje (si no está bloqueado)
                                            if (!isManifestLocked) {
                                                IconButton(onClick = {
                                                    TransportesRepository.removePassengerFromTrip(trip.id, passenger.docNumber)
                                                    Toast.makeText(context, "Removido del manifiesto", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Sacar del viaje", tint = StatusRed)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // --- CATEGORÍA 2: INICIADOS / EN RUTA ---
                            val activeRecordPassengers = trip.attendanceRecords.filter { it.status == TripStatus.INICIADO }
                            Text(
                                text = "2. Iniciados / En Ruta (${activeRecordPassengers.size}):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusYellow,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            activeRecordPassengers.forEach { record ->
                                val passengerName = trip.passengers.find { it.docNumber == record.passengerCedula }?.name ?: record.passengerCedula
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = StatusYellow.copy(alpha = 0.03f)),
                                    border = BorderStroke(1.dp, StatusYellow.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = passengerName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                                Text(text = "Cédula: ${record.passengerCedula}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                Text(
                                                    text = "Vehículo: ${record.vehicleType} (${record.plateNumber})\nConductor: ${record.driverName} • Salida: ${record.startTime}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(onClick = {
                                                    TransportesRepository.updateAttendanceStatus(trip.id, record.passengerCedula, TripStatus.CUMPLIDO)
                                                    Toast.makeText(context, "Estado de $passengerName forzado a CUMPLIDO", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Check, contentDescription = "Forzar Cumplido", tint = StatusGreen)
                                                }
                                                IconButton(onClick = {
                                                    TransportesRepository.updateAttendanceStatus(trip.id, record.passengerCedula, TripStatus.NO_CUMPLIDO)
                                                    Toast.makeText(context, "Estado de $passengerName forzado a NO CUMPLIDO", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Warning, contentDescription = "Forzar No Cumplido", tint = StatusRed)
                                                }
                                                IconButton(onClick = {
                                                    TransportesRepository.deleteAttendanceRecord(trip.id, record.passengerCedula)
                                                    Toast.makeText(context, "Asistencia de $passengerName reseteada a Pendiente", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Refresh, contentDescription = "Reset a Pendiente", tint = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // --- CATEGORÍA 3: CERRADOS / COMPLETADOS ---
                            val closedRecordPassengers = trip.attendanceRecords.filter { it.status == TripStatus.CUMPLIDO || it.status == TripStatus.NO_CUMPLIDO }
                            Text(
                                text = "3. Cerrados / Finalizados (${closedRecordPassengers.size}):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusGreen,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            closedRecordPassengers.forEach { record ->
                                val passengerName = trip.passengers.find { it.docNumber == record.passengerCedula }?.name ?: record.passengerCedula
                                val isSuccess = record.status == TripStatus.CUMPLIDO
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = (if (isSuccess) StatusGreen else StatusRed).copy(alpha = 0.04f)),
                                    border = BorderStroke(1.dp, (if (isSuccess) StatusGreen else StatusRed).copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "$passengerName (" + (if (isSuccess) "Cumplido" else "No cumplido") + ")",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSuccess) StatusGreen else StatusRed
                                            )
                                            Text(text = "Cédula: ${record.passengerCedula}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            Text(
                                                text = "Vehículo: ${record.vehicleType} (${record.plateNumber})\nConductor: ${record.driverName} • Salida: ${record.startTime}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(onClick = {
                                                TransportesRepository.updateAttendanceStatus(trip.id, record.passengerCedula, TripStatus.INICIADO)
                                                Toast.makeText(context, "Estado de $passengerName cambiado a INICIADO", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Cambiar a Iniciado", tint = StatusYellow)
                                            }
                                            IconButton(onClick = {
                                                TransportesRepository.deleteAttendanceRecord(trip.id, record.passengerCedula)
                                                Toast.makeText(context, "Asistencia de $passengerName reseteada a Pendiente", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Refresh, contentDescription = "Reset a Pendiente", tint = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }

                            // --- AGREGAR PARTICIPANTE AL MANIFIESTO (Si no está bloqueado) ---
                            if (!isManifestLocked) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Añadir Participante al Manifiesto del Viaje:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                                
                                var searchInput by remember { mutableStateOf("") }
                                val addSuggestions = remember(searchInput) {
                                    if (searchInput.isBlank()) emptyList()
                                    else TransportesRepository.globalParticipants.filter { gp ->
                                        gp.name.contains(searchInput, ignoreCase = true) &&
                                        trip.passengers.none { p -> p.docNumber == gp.docNumber }
                                    }
                                }

                                OutlinedTextField(
                                    value = searchInput,
                                    onValueChange = { searchInput = it },
                                    label = { Text("Buscar participante global...") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    colors = getTextFieldColors()
                                )

                                if (addSuggestions.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            addSuggestions.take(3).forEach { participant ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            TransportesRepository.addPassengerToTrip(trip.id, participant)
                                                            searchInput = ""
                                                            Toast.makeText(context, "${participant.name} agregado al manifiesto", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${participant.name} (${participant.docNumber})",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                        color = Color.Black
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
        }
    }
}

/**
 * Pantalla de Portal del Usuario (Login y Selección de Viajes)
 */
@Composable
fun UserDashboardScreen(
    onBack: () -> Unit,
    onRegisterOccasional: () -> Unit,
    onLoggedIn: (String) -> Unit,
    onSelectTrip: (Trip) -> Unit
) {
    var docNumberInput by remember { mutableStateOf("") }
    var loggedParticipant by remember { mutableStateOf<Participant?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Consulta de viajes asociados al usuario logueado
    val userTrips = remember(loggedParticipant) {
        loggedParticipant?.let {
            TransportesRepository.getTripsForParticipant(it.docNumber)
        } ?: emptyList()
    }

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
            IconButton(onClick = {
                if (loggedParticipant != null) {
                    loggedParticipant = null
                    docNumberInput = ""
                } else {
                    onBack()
                }
            }) {
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
                    text = "Consulte sus programaciones y confirme asistencia",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (loggedParticipant == null) {
            // Pantalla de Ingreso por Cédula
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Identificación de Usuario",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )

                    OutlinedTextField(
                        value = docNumberInput,
                        onValueChange = {
                            docNumberInput = it
                            errorMessage = null
                        },
                        label = { Text("Ingrese su Cédula / Documento") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = getTextFieldColors()
                    )

                    errorMessage?.let {
                        Text(text = it, color = StatusRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            if (docNumberInput.isBlank()) {
                                errorMessage = "Debe digitar su número de documento"
                                return@Button
                            }
                            val participant = TransportesRepository.getParticipantByCedula(docNumberInput.trim())
                            if (participant == null) {
                                errorMessage = "Documento no registrado en la base de datos de participantes. Consulte con el Meta Líder."
                            } else {
                                loggedParticipant = participant
                                onLoggedIn(participant.docNumber)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Ingresar", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Viaje Ocasional
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRegisterOccasional() },
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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                        Text(
                            text = "Para traslados excepcionales fuera de programación.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.LightGray)
                }
            }
        } else {
            // Panel del Usuario Logueado (Muestra sus viajes)
            val participant = loggedParticipant!!
            
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bienvenido, ${participant.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    )
                    Text(
                        text = "Cédula: ${participant.docNumber} • Teléfono: ${participant.phone}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                    Text(
                        text = "Proyecto: ${participant.projectNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tus viajes programados:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (userTrips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes viajes programados actualmente.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(userTrips) { trip ->
                        val myRecord = trip.attendanceRecords.find { it.passengerCedula == participant.docNumber }
                        val isAttended = myRecord != null
                        
                        val statusLabel = when {
                            isAttended && myRecord?.status == TripStatus.CUMPLIDO -> "Cumplido"
                            isAttended && myRecord?.status == TripStatus.NO_CUMPLIDO -> "No cumplido"
                            isAttended && myRecord?.status == TripStatus.INICIADO -> "Iniciado"
                            else -> "Pendiente de Inicio"
                        }
                        val statusColor = when {
                            isAttended && myRecord?.status == TripStatus.CUMPLIDO -> StatusGreen
                            isAttended && myRecord?.status == TripStatus.NO_CUMPLIDO -> StatusRed
                            isAttended && myRecord?.status == TripStatus.INICIADO -> StatusYellow
                            else -> StatusScheduled
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTrip(trip) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                1.dp,
                                if (isAttended && myRecord?.status == TripStatus.CUMPLIDO) StatusGreen.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Asistencia: ${trip.route}",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Fecha: ${trip.date} • Tu Estado: $statusLabel",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }
                                
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f))
                                ) {
                                    Text(
                                        text = if (isAttended && myRecord?.status == TripStatus.CUMPLIDO) "Asistencia OK" else statusLabel,
                                        color = if (isAttended && myRecord?.status == TripStatus.CUMPLIDO) StatusGreen else statusColor,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

/**
 * Pantalla de Detalles de Viaje del Usuario (Apertura, Asistencia de Compañeros, e Inicio / Cierre del Viaje)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTripDetailsScreen(
    trip: Trip?,
    loggedCedula: String,
    onBack: () -> Unit
) {
    if (trip == null) {
        onBack()
        return
    }

    var driverName by remember { mutableStateOf("") }
    var plateNumber by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    
    var selectedVehicleType by remember { mutableStateOf("Van") }
    var customVehicleType by remember { mutableStateOf("") }
    var expandedVehicle by remember { mutableStateOf(false) }
    
    val allTrips = remember { derivedStateOf { TransportesRepository.getTrips() } }
    val refreshedTrip = remember(allTrips.value, trip.id) {
        allTrips.value.find { it.id == trip.id } ?: trip
    }

    // Buscar si el usuario ya tiene un registro de vehículo en este viaje
    val userRecord = remember(refreshedTrip.attendanceRecords, loggedCedula) {
        refreshedTrip.attendanceRecords.find { it.passengerCedula == loggedCedula }
    }

    // Inicializar inputs:
    // - Si el usuario ya tiene registro, cargar sus datos.
    // - Si no, pero hay otros registros de otros pasajeros, autocompletar con el último registro existente
    LaunchedEffect(userRecord, refreshedTrip.attendanceRecords) {
        if (userRecord != null) {
            driverName = userRecord.driverName
            plateNumber = userRecord.plateNumber
            startTime = userRecord.startTime
            
            val matchesType = listOf("Van", "Camioneta", "Bus").contains(userRecord.vehicleType)
            if (matchesType) {
                selectedVehicleType = userRecord.vehicleType
            } else {
                selectedVehicleType = "Otro"
                customVehicleType = userRecord.vehicleType
            }
        } else {
            val genericRecord = refreshedTrip.attendanceRecords.firstOrNull()
            if (genericRecord != null && driverName.isBlank() && plateNumber.isBlank()) {
                driverName = genericRecord.driverName
                plateNumber = genericRecord.plateNumber
                startTime = genericRecord.startTime
                
                val matchesType = listOf("Van", "Camioneta", "Bus").contains(genericRecord.vehicleType)
                if (matchesType) {
                    selectedVehicleType = genericRecord.vehicleType
                } else {
                    selectedVehicleType = "Otro"
                    customVehicleType = genericRecord.vehicleType
                }
            }
        }
    }

    val context = LocalContext.current
    
    // El viaje es editable/iniciable para mí si no tengo ningún registro en absoluto.
    val isTripPendingForMe = userRecord == null
    val isTripInitiatedForMe = userRecord != null && userRecord.status == TripStatus.INICIADO
    val isTripClosedForMe = userRecord != null && (userRecord.status == TripStatus.CUMPLIDO || userRecord.status == TripStatus.NO_CUMPLIDO)

    // Validación de fecha futura
    val today = "2026-07-03"
    val isFutureDate = refreshedTrip.date > today

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
            Column {
                Text(
                    text = refreshedTrip.route,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Text(
                    text = "Fecha programada: ${refreshedTrip.date}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Advertencia si es una fecha futura
            if (isFutureDate) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusRed.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "El viaje está programado para una fecha futura (${refreshedTrip.date}). No se permite iniciar o registrar asistencias antes de la fecha establecida.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusRed
                            )
                        }
                    }
                }
            }

            // Listado de vehículos iniciados por otros (Para dar visibilidad al usuario)
            val otherRecords = refreshedTrip.attendanceRecords.filter { it.passengerCedula != loggedCedula }
            if (otherRecords.isNotEmpty() && isTripPendingForMe) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusYellow.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, StatusYellow.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = StatusYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Iniciado por otros usuarios (Vehículos paralelos):",
                                    fontWeight = FontWeight.Bold,
                                    color = StatusYellow,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            otherRecords.forEach { record ->
                                val passengerName = refreshedTrip.passengers.find { it.docNumber == record.passengerCedula }?.name ?: record.passengerCedula
                                val statusText = when (record.status) {
                                    TripStatus.CUMPLIDO -> "Cumplido"
                                    TripStatus.NO_CUMPLIDO -> "No cumplido"
                                    TripStatus.INICIADO -> "Iniciado"
                                    else -> ""
                                }
                                Text(
                                    text = "• $passengerName viaja en ${record.vehicleType} (${record.plateNumber}) con ${record.driverName} - [$statusText]",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Formulario de datos del vehículo
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Datos del Transporte",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        )

                        // Deshabilitar edición si es fecha futura o si ya inicié o cerré mi viaje
                        val editable = isTripPendingForMe && !isFutureDate

                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Nombre del Conductor") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            enabled = editable,
                            colors = getTextFieldColors()
                        )

                        OutlinedTextField(
                            value = plateNumber,
                            onValueChange = { plateNumber = it },
                            label = { Text("Placa del Vehículo") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            enabled = editable,
                            colors = getTextFieldColors()
                        )

                        TimeSelector(
                            label = "Hora de Salida (Inicio)",
                            selectedTime = startTime,
                            onTimeSelected = { startTime = it },
                            enabled = editable
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedVehicle && editable,
                            onExpandedChange = { if (editable) expandedVehicle = it }
                        ) {
                            OutlinedTextField(
                                value = selectedVehicleType,
                                onValueChange = {},
                                label = { Text("Tipo de Vehículo") },
                                readOnly = true,
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = getTextFieldColors(),
                                enabled = editable
                            )
                            ExposedDropdownMenu(
                                expanded = expandedVehicle && editable,
                                onDismissRequest = { expandedVehicle = false }
                            ) {
                                val vehicleTypes = listOf("Van", "Camioneta", "Bus", "Otro")
                                vehicleTypes.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection) },
                                        onClick = {
                                            selectedVehicleType = selection
                                            expandedVehicle = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedVehicleType == "Otro") {
                            OutlinedTextField(
                                value = customVehicleType,
                                onValueChange = { customVehicleType = it },
                                label = { Text("Especifique tipo de vehículo") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                enabled = editable,
                                colors = getTextFieldColors()
                            )
                        }
                    }
                }
            }

            // Listado de compañeros del mismo viaje
            item {
                Text(
                    text = "Asistencia de Participantes del Viaje:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue
                )
            }

            items(refreshedTrip.passengers) { passenger ->
                val passengerRecord = refreshedTrip.attendanceRecords.find { it.passengerCedula == passenger.docNumber }
                val isAttended = passengerRecord != null
                
                // Un pasajero es editable por mí si no está confirmado en ningún vehículo, 
                // o si está confirmado específicamente en mi vehículo.
                val isConfirmedByMe = isAttended && passengerRecord?.passengerCedula == loggedCedula
                val isConfirmedInMyVehicle = isAttended && userRecord != null && passengerRecord?.plateNumber == userRecord.plateNumber

                val canModifyThisPassenger = !isFutureDate && (isTripPendingForMe && !isAttended || isTripInitiatedForMe && isConfirmedInMyVehicle)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAttended) StatusGreen.copy(alpha = 0.05f) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isAttended) StatusGreen.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val suffix = if (passenger.docNumber == loggedCedula) " (Yo)" else ""
                                Text(
                                    text = passenger.name + suffix,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.Black
                                )
                                if (passenger.docNumber == loggedCedula) {
                                    Text(
                                        text = "Proyecto: ${passenger.projectNumber} • Cédula: ${passenger.docNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                } else {
                                    Text(
                                        text = "Proyecto: ${passenger.projectNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }
                            }

                            if (isAttended) {
                                if (canModifyThisPassenger) {
                                    IconButton(
                                        onClick = {
                                            TransportesRepository.removeAttendance(refreshedTrip.id, passenger.docNumber)
                                            Toast.makeText(context, "Asistencia removida para ${passenger.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Remover", tint = StatusGreen)
                                    }
                                } else {
                                    // Bloqueado: Viaja en otro vehículo
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = StatusScheduled.copy(alpha = 0.1f))
                                    ) {
                                        Text(
                                            text = "Viaja en ${passengerRecord?.plateNumber}",
                                            color = StatusScheduled,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (isFutureDate) {
                                            Toast.makeText(context, "No se puede iniciar antes de la fecha", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (driverName.isBlank() || plateNumber.isBlank() || startTime.isBlank()) {
                                            Toast.makeText(context, "Debe registrar primero los datos del transporte", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        val finalVehicle = if (selectedVehicleType == "Otro") customVehicleType.trim() else selectedVehicleType
                                        if (selectedVehicleType == "Otro" && finalVehicle.isBlank()) {
                                            Toast.makeText(context, "Especifique el tipo de vehículo", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }

                                        val success = TransportesRepository.confirmAttendance(
                                            tripId = refreshedTrip.id,
                                            passengerCedula = passenger.docNumber,
                                            driverName = driverName.trim(),
                                            plateNumber = plateNumber.trim().uppercase(),
                                            startTime = startTime.trim(),
                                            vehicleType = finalVehicle
                                        )
                                        if (success) {
                                            Toast.makeText(context, "Asistencia confirmada para ${passenger.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    enabled = canModifyThisPassenger
                                ) {
                                    Text("Confirmar", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        if (isAttended && passengerRecord != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            val isMyGroup = userRecord != null && passengerRecord.plateNumber == userRecord.plateNumber
                            Text(
                                text = (if (isMyGroup) "Tu Transporte:\n" else "Transporte de Compañero:\n") +
                                        "Placa: ${passengerRecord.plateNumber} • Conductor: ${passengerRecord.driverName}\n" +
                                        "Hora: ${passengerRecord.startTime} • Vehículo: ${passengerRecord.vehicleType}\n" +
                                        "Estado: " + (if (passengerRecord.status == TripStatus.CUMPLIDO) "Cumplido" else if (passengerRecord.status == TripStatus.NO_CUMPLIDO) "No cumplido" else "Iniciado"),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isMyGroup) StatusGreen else StatusScheduled,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Flujo de Inicio de Viaje (Si aún no he iniciado)
            if (isTripPendingForMe && !isFutureDate) {
                item {
                    Button(
                        onClick = {
                            if (driverName.isBlank() || plateNumber.isBlank() || startTime.isBlank()) {
                                Toast.makeText(context, "Complete todos los campos del vehículo para iniciar", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            val finalVehicle = if (selectedVehicleType == "Otro") customVehicleType.trim() else selectedVehicleType
                            if (selectedVehicleType == "Otro" && finalVehicle.isBlank()) {
                                Toast.makeText(context, "Especifique el tipo de vehículo", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            // Confirmarse a sí mismo en el vehículo si no lo ha hecho en la lista
                            if (refreshedTrip.attendanceRecords.none { it.passengerCedula == loggedCedula }) {
                                TransportesRepository.confirmAttendance(
                                    tripId = refreshedTrip.id,
                                    passengerCedula = loggedCedula,
                                    driverName = driverName.trim(),
                                    plateNumber = plateNumber.trim().uppercase(),
                                    startTime = startTime.trim(),
                                    vehicleType = finalVehicle
                                )
                            }

                            // Poner estado de inicio
                            Toast.makeText(context, "¡Aceptado! Inicio de viaje registrado para tu vehículo", Toast.LENGTH_LONG).show()
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusYellow)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aceptar inicio del viaje",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Sección de Cierre del Viaje (Si ya lo inicié y está activo para mí)
            if (isTripInitiatedForMe) {
                item {
                    Divider()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.03f)),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Cerrar Viaje",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                            Text(
                                text = "Tu vehículo ya está en ruta. Al concluir el recorrido, seleccione el cumplimiento del viaje para tu grupo.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        TransportesRepository.closeTripForUser(refreshedTrip.id, loggedCedula, TripStatus.CUMPLIDO)
                                        Toast.makeText(context, "Viaje Cerrado como CUMPLIDO", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cumplido", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }

                                Button(
                                    onClick = {
                                        TransportesRepository.closeTripForUser(refreshedTrip.id, loggedCedula, TripStatus.NO_CUMPLIDO)
                                        Toast.makeText(context, "Viaje Cerrado como NO CUMPLIDO", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("No Cumplido", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            // Si ya fue cerrado para mí
            if (isTripClosedForMe) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (userRecord?.status == TripStatus.CUMPLIDO) StatusGreen.copy(alpha = 0.05f) else StatusRed.copy(alpha = 0.05f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (userRecord?.status == TripStatus.CUMPLIDO) StatusGreen.copy(alpha = 0.2f) else StatusRed.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (userRecord?.status == TripStatus.CUMPLIDO) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (userRecord?.status == TripStatus.CUMPLIDO) StatusGreen else StatusRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Has finalizado el viaje como: " + (if (userRecord?.status == TripStatus.CUMPLIDO) "CUMPLIDO" else "NO CUMPLIDO"),
                                fontWeight = FontWeight.Bold,
                                color = if (userRecord?.status == TripStatus.CUMPLIDO) StatusGreen else StatusRed
                            )
                        }
                    }
                }
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
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
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
            singleLine = true,
            colors = getTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DateSelector(
            label = "Fecha de Viaje",
            selectedDate = date,
            onDateSelected = { date = it }
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
            singleLine = true,
            colors = getTextFieldColors()
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
            singleLine = true,
            colors = getTextFieldColors()
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
                val success = TransportesRepository.addOccasionalTrip(
                    passengerName = passengerName.trim(),
                    date = date,
                    origin = origin.trim(),
                    destination = destination.trim()
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

data class CalendarDay(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val dateString: String
)

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
    for (i in 0 until startDayOffset) {
        calendarDays.add(CalendarDay(dayNumber = 0, isCurrentMonth = false, dateString = ""))
    }
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
                                val tripsOnDay = trips.filter { it.date == day.dateString }
                                
                                val dayBgColor = if (tripsOnDay.isNotEmpty()) {
                                    val hasPending = tripsOnDay.any { it.status == TripStatus.POR_CUMPLIR }
                                    val hasIniciado = tripsOnDay.any { it.status == TripStatus.INICIADO }
                                    val hasFailed = tripsOnDay.any { it.status == TripStatus.NO_CUMPLIDO }
                                    val hasSuccess = tripsOnDay.any { it.status == TripStatus.CUMPLIDO }

                                    when {
                                        hasIniciado -> StatusYellow.copy(alpha = 0.2f)
                                        hasFailed -> StatusRed.copy(alpha = 0.2f)
                                        hasSuccess -> StatusGreen.copy(alpha = 0.2f)
                                        hasPending -> StatusScheduled.copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }
                                } else {
                                    Color.Transparent
                                }

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

                                        if (tripsOnDay.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                tripsOnDay.take(3).forEach { trip ->
                                                    val dotColor = when (trip.status) {
                                                        TripStatus.CUMPLIDO -> StatusGreen
                                                        TripStatus.NO_CUMPLIDO -> StatusRed
                                                        TripStatus.INICIADO -> StatusYellow
                                                        TripStatus.POR_CUMPLIR -> StatusScheduled
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
