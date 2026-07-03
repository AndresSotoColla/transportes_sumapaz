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

// Colores del sistema de diseño
val PrimaryBlue = Color(0xFF0F4C81)      // Azul clásico y premium
val LightBlue = Color(0xFF3B82F6)        // Azul moderno
val GradientStart = Color(0xFF1E3A8A)    // Azul oscuro profundo
val GradientEnd = Color(0xFF3B82F6)      // Azul brillante
val LightBackground = Color(0xFFF8FAFC)  // Fondo gris suave

val StatusGreen = Color(0xFF2E7D32)      // Cumplido
val StatusRed = Color(0xFFC62828)        // No cumplido
val StatusYellow = Color(0xFFE65100)     // Por cumplir (ámbar/naranja)

@Composable
fun TransportesSumapazApp() {
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var loggedUserCedula by remember { mutableStateOf("") }
    var selectedTripForDetails by remember { mutableStateOf<Trip?>(null) }
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
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
@OptIn(ExperimentalLayoutApi::class)
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
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
                        // Si cambia el nombre, resetear autocompletado a menos que vuelva a elegir uno
                        val matched = TransportesRepository.getParticipantByName(it)
                        if (matched == null) {
                            isExistingParticipant = false
                        }
                    },
                    label = { Text("Nombre del Participante") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
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
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
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

                            OutlinedTextField(
                                value = docType,
                                onValueChange = { if (!isExistingParticipant) docType = it },
                                label = { Text("Tipo de Documento") },
                                readOnly = isExistingParticipant,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = docNumber,
                                onValueChange = { if (!isExistingParticipant) docNumber = it },
                                label = { Text("Cédula / Documento") },
                                readOnly = isExistingParticipant,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { if (!isExistingParticipant) phone = it },
                                label = { Text("Teléfono") },
                                readOnly = isExistingParticipant,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { if (!isExistingParticipant) email = it },
                                label = { Text("Correo Electrónico") },
                                readOnly = isExistingParticipant,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = projectNumber,
                                onValueChange = { if (!isExistingParticipant) projectNumber = it },
                                label = { Text("Número del Proyecto") },
                                readOnly = isExistingParticipant,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
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
                                Text(text = passenger.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
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
 * Pantalla Viajes Agendados - Calendario (Meta Líder)
 */
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
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
                                    text = "Ruta: ${trip.route}",
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
                                text = "Pasajeros Programados (${trip.passengers.size}):",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            
                            trip.passengers.forEach { passenger ->
                                val attendanceRecord = trip.attendanceRecords.find { it.passengerCedula == passenger.docNumber }
                                val isConfirmed = attendanceRecord != null
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isConfirmed) StatusGreen.copy(alpha = 0.04f) else Color.Transparent
                                    ),
                                    border = BorderStroke(1.dp, if (isConfirmed) StatusGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.1f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isConfirmed) Icons.Default.CheckCircle else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isConfirmed) StatusGreen else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = passenger.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isConfirmed) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isConfirmed) StatusGreen else Color.Black
                                                )
                                            )
                                        }
                                        if (isConfirmed && attendanceRecord != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Vehículo: ${attendanceRecord.vehicleType} (${attendanceRecord.plateNumber})\nConductor: ${attendanceRecord.driverName} • Salida: ${attendanceRecord.startTime}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = StatusGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                modifier = Modifier.padding(start = 26.dp)
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
                        singleLine = true
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
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                        val isAttended = trip.attendanceRecords.any { it.passengerCedula == participant.docNumber }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTrip(trip) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(
                                1.dp,
                                if (isAttended) StatusGreen.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.5f)
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
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Fecha: ${trip.date} • Estado: ${trip.status}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isAttended) StatusGreen.copy(alpha = 0.15f) else StatusYellow.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Text(
                                        text = if (isAttended) "Asistencia OK" else "Pendiente",
                                        color = if (isAttended) StatusGreen else StatusYellow,
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
 * Pantalla de Detalles de Viaje del Usuario (Apertura, Asistencia de Compañeros, y Cierre)
 */
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
    var vehicleType by remember { mutableStateOf("") }

    val allTrips = remember { derivedStateOf { TransportesRepository.getTrips() } }
    val refreshedTrip = remember(allTrips.value, trip.id) {
        allTrips.value.find { it.id == trip.id } ?: trip
    }

    // Inicializar inputs si ya el conductor se guardó previamente en alguna asistencia de este usuario
    LaunchedEffect(refreshedTrip.attendanceRecords) {
        val myRecord = refreshedTrip.attendanceRecords.find { it.passengerCedula == loggedCedula }
        if (myRecord != null) {
            driverName = myRecord.driverName
            plateNumber = myRecord.plateNumber
            startTime = myRecord.startTime
            vehicleType = myRecord.vehicleType
        }
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
            Column {
                Text(
                    text = refreshedTrip.route,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Text(
                    text = "Viaje programado: ${refreshedTrip.date}",
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
            // Formulario de apertura de viaje (Datos del vehículo)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Datos del Transporte (Apertura)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        )
                        Text(
                            text = "Registre los detalles de salida del vehículo. Estos se asociarán a las asistencias que confirme.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Nombre del Conductor") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = plateNumber,
                            onValueChange = { plateNumber = it },
                            label = { Text("Placa del Vehículo") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text("Hora de Inicio") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = vehicleType,
                                onValueChange = { vehicleType = it },
                                label = { Text("Tipo Vehículo") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
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
                            Column {
                                val suffix = if (passenger.docNumber == loggedCedula) " (Yo)" else ""
                                Text(
                                    text = passenger.name + suffix,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Proyecto: ${passenger.projectNumber} • Cédula: ${passenger.docNumber}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }

                            if (isAttended) {
                                IconButton(
                                    onClick = {
                                        TransportesRepository.removeAttendance(refreshedTrip.id, passenger.docNumber)
                                        Toast.makeText(context, "Asistencia removida para ${passenger.name}", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Remover", tint = StatusGreen)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (driverName.isBlank() || plateNumber.isBlank() || startTime.isBlank() || vehicleType.isBlank()) {
                                            Toast.makeText(context, "Debe registrar primero los 4 datos del transporte para abrir el viaje", Toast.LENGTH_LONG).show()
                                            return@Button
                                        }
                                        val success = TransportesRepository.confirmAttendance(
                                            tripId = refreshedTrip.id,
                                            passengerCedula = passenger.docNumber,
                                            driverName = driverName.trim(),
                                            plateNumber = plateNumber.trim().toUpperCase(),
                                            startTime = startTime.trim(),
                                            vehicleType = vehicleType.trim()
                                        )
                                        if (success) {
                                            Toast.makeText(context, "Confirmado en vehículo $plateNumber", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Confirmar", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        if (isAttended && passengerRecord != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Transporte Asociado:\nPlaca: ${passengerRecord.plateNumber} • Conductor: ${passengerRecord.driverName}\nHora: ${passengerRecord.startTime} • Vehículo: ${passengerRecord.vehicleType}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = StatusGreen,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Sección de Cierre del Viaje
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
                            text = "Actualice el estado final del viaje a completado o no cumplido. Esto finalizará el viaje en el calendario.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    TransportesRepository.closeTrip(refreshedTrip.id, TripStatus.CUMPLIDO)
                                    Toast.makeText(context, "Viaje Cerrado como CUMPLIDO", Toast.LENGTH_SHORT).show()
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
                                    TransportesRepository.closeTrip(refreshedTrip.id, TripStatus.NO_CUMPLIDO)
                                    Toast.makeText(context, "Viaje Cerrado como NO CUMPLIDO", Toast.LENGTH_SHORT).show()
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
