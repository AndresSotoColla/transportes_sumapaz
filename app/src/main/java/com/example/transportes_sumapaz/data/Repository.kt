package com.example.transportes_sumapaz.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

enum class TripStatus {
    CUMPLIDO,      // Verde
    NO_CUMPLIDO,   // Rojo
    POR_CUMPLIR,   // Gris (Programado)
    INICIADO       // Amarillo/Naranja (Iniciado)
}

/**
 * Modelo para un Participante / Usuario de la comunidad de Sumapaz.
 */
data class Participant(
    val name: String,
    val docType: String,
    val docNumber: String,      // Cédula (Llave primaria/ID único)
    val phone: String,
    val email: String,
    val projectNumber: String
)

/**
 * Registro de asistencia que asocia a un pasajero con los datos del vehículo en el que viaja.
 */
data class AttendanceRecord(
    val passengerCedula: String,
    val driverName: String,
    val plateNumber: String,
    val startTime: String,
    val vehicleType: String,
    var status: TripStatus = TripStatus.INICIADO,
    // Telemetría de celular
    var startDeviceTime: String = "",
    var startCoordinates: String = "",
    var endDeviceTime: String = "",
    var endCoordinates: String = ""
)

/**
 * Viaje programado por el Meta Líder.
 */
data class Trip(
    val id: String,
    val date: String,                   // Formato "YYYY-MM-DD"
    val route: String,                  // "Sede Betania" o "Sede San Juan"
    var status: TripStatus,             // CUMPLIDO, NO_CUMPLIDO, POR_CUMPLIR
    var passengers: List<Participant>,  // Pasajeros programados por el líder (var para permitir edición)
    val attendanceRecords: MutableList<AttendanceRecord> = mutableStateListOf() // Asistencias confirmadas con datos de vehículo
) {
    fun updateStatus() {
        if (attendanceRecords.isEmpty()) {
            status = TripStatus.POR_CUMPLIR
        } else if (attendanceRecords.any { it.status == TripStatus.INICIADO }) {
            status = TripStatus.INICIADO
        } else if (attendanceRecords.any { it.status == TripStatus.CUMPLIDO }) {
            status = TripStatus.CUMPLIDO
        } else {
            status = TripStatus.NO_CUMPLIDO
        }
    }
}

/**
 * Viaje Ocasional registrado por un usuario.
 */
data class OccasionalTrip(
    val id: String,
    val passengerName: String,
    val date: String,
    val origin: String,
    val destination: String
)

/**
 * Cuenta de Meta Líder.
 */
data class LeaderAccount(
    val username: String,
    var name: String,
    var passwordHash: String,
    var mustChangePassword: Boolean
)

/**
 * Repositorio global de la aplicación.
 * Sostiene las listas globales de participantes, viajes y sesiones.
 */
object TransportesRepository {

    // Cuentas de Meta Líderes
    private val leaders = mutableMapOf(
        "lider" to LeaderAccount("lider", "Carlos Gómez", "123", mustChangePassword = true),
        "admin" to LeaderAccount("admin", "Admin Sumapaz", "admin123", mustChangePassword = true)
    )

    // Base de datos global de Participantes para autocompletado
    val globalParticipants = mutableStateListOf<Participant>(
        Participant("Juan Pérez", "Cédula de Ciudadanía", "1010", "3111234567", "juan@mail.com", "PROJ-101"),
        Participant("María Rodríguez", "Cédula de Ciudadanía", "2020", "3127654321", "maria@mail.com", "PROJ-101"),
        Participant("Pedro Gómez", "Cédula de Ciudadanía", "3030", "3139876543", "pedro@mail.com", "PROJ-102"),
        Participant("Ana Vega", "Cédula de Ciudadanía", "4040", "Cédula de Ciudadanía", "ana@mail.com", "PROJ-103")
    )

    // Lista global de viajes programados
    private val trips = mutableStateListOf<Trip>()

    // Lista global de viajes ocasionales
    private val occasionalTrips = mutableStateListOf<OccasionalTrip>()

    // Sesión activa del líder autenticado
    var loggedLeader = mutableStateOf<LeaderAccount?>(null)

    // Inicialización de datos de prueba
    init {
        // Viaje a Sede Betania para Hoy (2026-07-03) - Programado (POR_CUMPLIR)
        trips.add(
            Trip(
                id = "trip-betania-today",
                date = "2026-07-03",
                route = "Sede Betania",
                status = TripStatus.POR_CUMPLIR,
                passengers = listOf(
                    globalParticipants[0], // Juan Pérez (1010)
                    globalParticipants[1]  // María Rodríguez (2020)
                )
            )
        )

        // Viaje a Sede San Juan para Hoy (2026-07-03) - Iniciado (INICIADO) para pruebas
        val tripSanJuan = Trip(
            id = "trip-sanjuan-today",
            date = "2026-07-03",
            route = "Sede San Juan",
            status = TripStatus.INICIADO,
            passengers = listOf(
                globalParticipants[2], // Pedro Gómez (3030)
                globalParticipants[3]  // Ana Vega (4040)
            )
        )
        tripSanJuan.attendanceRecords.add(
            AttendanceRecord("3030", "Andrés Conductor", "OPQ-789", "09:30", "Van")
        )
        trips.add(tripSanJuan)

        // Viaje pasado ya Cumplido (2026-07-01)
        val tripPast = Trip(
            id = "trip-past",
            date = "2026-07-01",
            route = "Sede Betania",
            status = TripStatus.CUMPLIDO,
            passengers = listOf(globalParticipants[0])
        )
        tripPast.attendanceRecords.add(
            AttendanceRecord("1010", "Carlos Conductor", "XYZ-123", "07:30", "Bus", TripStatus.CUMPLIDO)
        )
        trips.add(tripPast)
    }

    // --- Autenticación Meta Líder ---
    fun loginLeader(username: String, passwordPlain: String): LoginResult {
        val account = leaders[username] ?: return LoginResult.USER_NOT_FOUND
        if (account.passwordHash != passwordPlain) {
            return LoginResult.WRONG_PASSWORD
        }
        if (account.mustChangePassword) {
            loggedLeader.value = account
            return LoginResult.MUST_CHANGE_PASSWORD
        }
        loggedLeader.value = account
        return LoginResult.SUCCESS
    }

    fun changeLeaderPassword(username: String, newPasswordPlain: String): Boolean {
        val leader = leaders[username] ?: return false
        leaders[username] = leader.copy(passwordHash = newPasswordPlain, mustChangePassword = false)
        loggedLeader.value = leaders[username]
        return true
    }

    fun logout() {
        loggedLeader.value = null
    }

    // --- Gestión de Participantes ---
    fun getParticipantByCedula(cedula: String): Participant? {
        /*
         * HOOK DE BASE DE DATOS:
         * select * from participants where docNumber = :cedula
         */
        return globalParticipants.find { it.docNumber == cedula }
    }

    fun getParticipantByName(name: String): Participant? {
        return globalParticipants.find { it.name.equals(name, ignoreCase = true) }
    }

    fun registerParticipant(participant: Participant) {
        /*
         * HOOK DE BASE DE DATOS:
         * insert into participants values (...)
         */
        if (globalParticipants.none { it.docNumber == participant.docNumber }) {
            globalParticipants.add(participant)
        }
    }

    // --- Gestión de Viajes ---
    fun getTrips(): List<Trip> {
        return trips
    }

    fun addTrip(route: String, date: String, passengers: List<Participant>): Boolean {
        /*
         * HOOK DE BASE DE DATOS:
         * insert into trips ...
         * insert into trip_passengers ...
         */
        val newTrip = Trip(
            id = java.util.UUID.randomUUID().toString(),
            date = date,
            route = route,
            status = TripStatus.POR_CUMPLIR,
            passengers = passengers
        )
        trips.add(newTrip)
        return true
    }

    fun getTripsForParticipant(cedula: String, date: String = ""): List<Trip> {
        return trips.filter { trip ->
            val isPassenger = trip.passengers.any { it.docNumber == cedula }
            val matchesDate = date.isEmpty() || trip.date == date
            isPassenger && matchesDate
        }
    }

    /**
     * Confirma la asistencia de un pasajero en un viaje específico,
     * asociándole los datos de vehículo de la sesión del usuario que confirma.
     */
    fun confirmAttendance(
        tripId: String,
        passengerCedula: String,
        driverName: String,
        plateNumber: String,
        startTime: String,
        vehicleType: String
    ): Boolean {
        val trip = trips.find { it.id == tripId } ?: return false
        trip.attendanceRecords.removeAll { it.passengerCedula == passengerCedula }
        
        val deviceTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val deviceCoords = String.format(java.util.Locale.US, "%.5f° N, %.5f° W", 4.15 + (Math.random() * 0.08), 74.20 + (Math.random() * 0.08))
        
        trip.attendanceRecords.add(
            AttendanceRecord(
                passengerCedula = passengerCedula,
                driverName = driverName,
                plateNumber = plateNumber,
                startTime = startTime,
                vehicleType = vehicleType,
                status = TripStatus.INICIADO,
                startDeviceTime = deviceTime,
                startCoordinates = deviceCoords
            )
        )
        trip.updateStatus()
        return true
    }

    /**
     * Remueve la confirmación de asistencia de un pasajero.
     */
    fun removeAttendance(tripId: String, passengerCedula: String): Boolean {
        val trip = trips.find { it.id == tripId } ?: return false
        trip.attendanceRecords.removeAll { it.passengerCedula == passengerCedula }
        trip.updateStatus()
        return true
    }

    /**
     * Cierra el viaje para un pasajero, aplicando su re-confirmación y telemetría de cierre.
     */
    fun closeTripForUser(
        tripId: String,
        passengerCedula: String,
        status: TripStatus,
        confirmedPassengers: List<String>
    ): Boolean {
        val trip = trips.find { it.id == tripId } ?: return false
        val userRecord = trip.attendanceRecords.find { it.passengerCedula == passengerCedula } ?: return false
        val userPlate = userRecord.plateNumber
        
        val deviceTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val deviceCoords = String.format(java.util.Locale.US, "%.5f° N, %.5f° W", 4.15 + (Math.random() * 0.08), 74.20 + (Math.random() * 0.08))
        
        trip.attendanceRecords.forEach { record ->
            if (record.plateNumber == userPlate) {
                if (confirmedPassengers.contains(record.passengerCedula)) {
                    record.status = status
                } else {
                    record.status = TripStatus.NO_CUMPLIDO
                }
                record.endDeviceTime = deviceTime
                record.endCoordinates = deviceCoords
            }
        }
        trip.updateStatus()
        return true
    }

    // --- Funciones del Líder para Administrar Manifiesto y Estados ---

    fun addPassengerToTrip(tripId: String, participant: Participant): Boolean {
        val trip = trips.find { it.id == tripId } ?: return false
        if (trip.attendanceRecords.isNotEmpty()) return false // Bloqueado si ya inició alguien
        if (trip.passengers.any { it.docNumber == participant.docNumber }) return false
        trip.passengers = trip.passengers + participant
        return true
    }

    fun removePassengerFromTrip(tripId: String, passengerCedula: String): Boolean {
        val trip = trips.find { it.id == tripId } ?: return false
        if (trip.attendanceRecords.isNotEmpty()) return false // Bloqueado si ya inició alguien
        trip.passengers = trip.passengers.filter { it.docNumber != passengerCedula }
        return true
    }

    fun updateAttendanceStatus(tripId: String, passengerCedula: String, newStatus: TripStatus): Boolean {
        val trip = trips.find { it.id == tripId } ?: return false
        val record = trip.attendanceRecords.find { it.passengerCedula == passengerCedula }
        val deviceTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val deviceCoords = String.format(java.util.Locale.US, "%.5f° N, %.5f° W", 4.15 + (Math.random() * 0.08), 74.20 + (Math.random() * 0.08))
        
        if (record != null) {
            record.status = newStatus
            if (newStatus == TripStatus.CUMPLIDO || newStatus == TripStatus.NO_CUMPLIDO) {
                record.endDeviceTime = deviceTime
                record.endCoordinates = deviceCoords
            }
        } else {
            if (newStatus != TripStatus.POR_CUMPLIR) {
                val newRecord = AttendanceRecord(
                    passengerCedula = passengerCedula,
                    driverName = "Líder (Forzado)",
                    plateNumber = "LID-000",
                    startTime = "00:00",
                    vehicleType = "Otro",
                    status = newStatus,
                    startDeviceTime = deviceTime,
                    startCoordinates = deviceCoords
                )
                if (newStatus == TripStatus.CUMPLIDO || newStatus == TripStatus.NO_CUMPLIDO) {
                    newRecord.endDeviceTime = deviceTime
                    newRecord.endCoordinates = deviceCoords
                }
                trip.attendanceRecords.add(newRecord)
            }
        }
        trip.updateStatus()
        return true
    }

    fun deleteAttendanceRecord(tripId: String, passengerCedula: String): Boolean {
        val trip = trips.find { it.id == tripId } ?: return false
        val removed = trip.attendanceRecords.removeAll { it.passengerCedula == passengerCedula }
        trip.updateStatus()
        return removed
    }

    // --- Viajes Ocasionales ---
    fun getOccasionalTrips(): List<OccasionalTrip> {
        return occasionalTrips
    }

    fun addOccasionalTrip(passengerName: String, date: String, origin: String, destination: String): Boolean {
        val newTrip = OccasionalTrip(
            id = java.util.UUID.randomUUID().toString(),
            passengerName = passengerName,
            date = date,
            origin = origin,
            destination = destination
        )
        occasionalTrips.add(newTrip)
        return true
    }
}

enum class LoginResult {
    SUCCESS,
    MUST_CHANGE_PASSWORD,
    USER_NOT_FOUND,
    WRONG_PASSWORD
}

