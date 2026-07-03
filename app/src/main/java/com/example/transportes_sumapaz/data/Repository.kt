package com.example.transportes_sumapaz.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Estados de cumplimiento del viaje.
 */
enum class TripStatus {
    CUMPLIDO,      // Verde
    NO_CUMPLIDO,   // Rojo
    POR_CUMPLIR    // Amarillo
}

/**
 * Modelo que representa un Viaje agendado por el Meta Líder.
 */
data class Trip(
    val id: String,
    val date: String,             // Formato "YYYY-MM-DD"
    val route: String,            // Destino / Ruta del viaje
    val status: TripStatus,       // Estado actual
    val passengerNames: List<String>, // Nombres de pasajeros registrados por el líder para este día
    val attendance: List<String> = emptyList() // Nombres de personas que confirmaron asistencia
)

/**
 * Modelo que representa un Viaje Ocasional registrado por un usuario.
 */
data class OccasionalTrip(
    val id: String,
    val passengerName: String,
    val date: String,             // Formato "YYYY-MM-DD"
    val origin: String,
    val destination: String
)

/**
 * Cuenta de Meta Líder.
 */
data class LeaderAccount(
    val username: String,
    var name: String,
    var passwordHash: String,     // En producción se debe usar hashing (ej. bcrypt / SHA-256)
    var mustChangePassword: Boolean
)

/**
 * Interfaz que define las operaciones de datos para el aplicativo.
 * Esta abstracción permite que en el futuro se pueda implementar una clase
 * que se conecte directamente a una base de datos local (Room) o remota (Retrofit/Firebase)
 * simplemente heredando de esta interfaz sin necesidad de alterar la UI.
 */
interface TransportesDataSource {
    // Autenticación de Meta Líder
    fun getLeaderAccount(username: String): LeaderAccount?
    fun updateLeaderPassword(username: String, newPasswordHash: String): Boolean

    // Gestión de Viajes
    fun getTrips(): List<Trip>
    fun addTrip(trip: Trip): Boolean
    fun registerAttendance(tripId: String, name: String): Boolean

    // Gestión de Viajes Ocasionales
    fun getOccasionalTrips(): List<OccasionalTrip>
    fun addOccasionalTrip(trip: OccasionalTrip): Boolean
}

/**
 * Implementación de origen de datos en memoria para propósitos de prueba y demostración.
 * Contiene comentarios marcando dónde se deben integrar las llamadas a la Base de Datos.
 */
class InMemoryDataSource : TransportesDataSource {

    private val leaders = mutableMapOf(
        "lider" to LeaderAccount("lider", "Carlos Gómez", "123", mustChangePassword = true),
        "admin" to LeaderAccount("admin", "Admin Sumapaz", "admin123", mustChangePassword = true)
    )

    private val trips = mutableStateListOf<Trip>(
        Trip(
            id = "1",
            date = "2026-07-01",
            route = "Sumapaz a Bogotá (Centro)",
            status = TripStatus.CUMPLIDO,
            passengerNames = listOf("Juan Pérez", "María Rodríguez", "Pedro Gómez", "Sofía Cruz"),
            attendance = listOf("Juan Pérez", "María Rodríguez", "Pedro Gómez")
        ),
        Trip(
            id = "2",
            date = "2026-07-02",
            route = "Sumapaz a Cabrera",
            status = TripStatus.NO_CUMPLIDO,
            passengerNames = listOf("Laura Beltrán", "Esteban Rojas", "Ana Vega"),
            attendance = emptyList()
        ),
        Trip(
            id = "3",
            date = "2026-07-03", // Hoy
            route = "Sumapaz a Fusagasugá",
            status = TripStatus.POR_CUMPLIR,
            passengerNames = listOf("Luis Delgado", "Liliana Rincón", "Jorge Ortiz", "Diana Pinzón"),
            attendance = listOf("Luis Delgado")
        ),
        Trip(
            id = "4",
            date = "2026-07-05", // Futuro
            route = "Sumapaz a Bogotá (Norte)",
            status = TripStatus.POR_CUMPLIR,
            passengerNames = listOf("Carlos Mendieta", "Marta Castillo", "Andrés Felipe"),
            attendance = emptyList()
        ),
        Trip(
            id = "5",
            date = "2026-07-10", // Futuro
            route = "Sumapaz a Melgar",
            status = TripStatus.POR_CUMPLIR,
            passengerNames = listOf("Patricia Torres", "Diego Niño", "Juliana Silva"),
            attendance = emptyList()
        )
    )

    private val occasionalTrips = mutableStateListOf<OccasionalTrip>(
        OccasionalTrip(
            id = "1",
            passengerName = "Miguel Ángel",
            date = "2026-07-03",
            origin = "Vereda Las Sopas",
            destination = "Alcaldía Local"
        )
    )

    override fun getLeaderAccount(username: String): LeaderAccount? {
        /*
         * HOOK DE BASE DE DATOS:
         * En el futuro, aquí se consultaría la base de datos SQL local o remota.
         * Ejemplo Room:
         * return leaderDao.findByUsername(username)
         */
        return leaders[username]
    }

    override fun updateLeaderPassword(username: String, newPasswordHash: String): Boolean {
        /*
         * HOOK DE BASE DE DATOS:
         * Aquí se ejecutaría una consulta UPDATE en la base de datos.
         * Ejemplo Room:
         * leaderDao.updatePassword(username, newPasswordHash, false)
         */
        val leader = leaders[username] ?: return false
        leaders[username] = leader.copy(passwordHash = newPasswordHash, mustChangePassword = false)
        return true
    }

    override fun getTrips(): List<Trip> {
        /*
         * HOOK DE BASE DE DATOS:
         * Retornar la lista completa de viajes programados.
         * Ejemplo Room:
         * return tripDao.getAllTrips()
         */
        return trips
    }

    override fun addTrip(trip: Trip): Boolean {
        /*
         * HOOK DE BASE DE DATOS:
         * Guardar un nuevo viaje en la base de datos.
         * Ejemplo Room:
         * tripDao.insertTrip(trip)
         */
        trips.add(trip)
        return true
    }

    override fun registerAttendance(tripId: String, name: String): Boolean {
        /*
         * HOOK DE BASE DE DATOS:
         * Actualizar la lista de asistencia en el viaje correspondiente.
         * Ejemplo Room/API:
         * tripDao.addPassengerAttendance(tripId, name)
         */
        val index = trips.indexOfFirst { it.id == tripId }
        if (index != -1) {
            val trip = trips[index]
            if (!trip.attendance.contains(name)) {
                val updatedAttendance = trip.attendance + name
                trips[index] = trip.copy(attendance = updatedAttendance)
                return true
            }
        }
        return false
    }

    override fun getOccasionalTrips(): List<OccasionalTrip> {
        /*
         * HOOK DE BASE DE DATOS:
         * Consultar viajes ocasionales de la BD.
         */
        return occasionalTrips
    }

    override fun addOccasionalTrip(trip: OccasionalTrip): Boolean {
        /*
         * HOOK DE BASE DE DATOS:
         * Guardar viaje ocasional.
         */
        occasionalTrips.add(trip)
        return true
    }
}

/**
 * Repositorio global de la aplicación.
 * Actúa como punto único de acceso a los datos de la UI.
 * Para conectar una base de datos real en el futuro, solo se debe cambiar
 * la instancia de `dataSource` de `InMemoryDataSource` a una nueva clase
 * (por ejemplo, `RoomDataSource` o `RemoteApiDataSource`).
 */
object TransportesRepository {
    
    // Cambiar esta inicialización para conectar a base de datos en el futuro
    private val dataSource: TransportesDataSource = InMemoryDataSource()

    // Sesión activa del líder autenticado
    var loggedLeader = mutableStateOf<LeaderAccount?>(null)

    fun loginLeader(username: String, passwordPlain: String): LoginResult {
        val account = dataSource.getLeaderAccount(username) ?: return LoginResult.USER_NOT_FOUND
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
        val success = dataSource.updateLeaderPassword(username, newPasswordPlain)
        if (success) {
            // Actualizar sesión activa
            val updatedAccount = dataSource.getLeaderAccount(username)
            loggedLeader.value = updatedAccount
        }
        return success
    }

    fun logout() {
        loggedLeader.value = null
    }

    fun getTrips(): List<Trip> {
        return dataSource.getTrips()
    }

    fun getTripsForDate(date: String): List<Trip> {
        return dataSource.getTrips().filter { it.date == date }
    }

    fun addTrip(route: String, date: String, status: TripStatus, passengers: List<String>): Boolean {
        val newTrip = Trip(
            id = java.util.UUID.randomUUID().toString(),
            date = date,
            route = route,
            status = status,
            passengerNames = passengers.filter { it.isNotBlank() },
            attendance = emptyList()
        )
        return dataSource.addTrip(newTrip)
    }

    fun registerAttendance(tripId: String, name: String): Boolean {
        return dataSource.registerAttendance(tripId, name)
    }

    fun getOccasionalTrips(): List<OccasionalTrip> {
        return dataSource.getOccasionalTrips()
    }

    fun addOccasionalTrip(passengerName: String, date: String, origin: String, destination: String): Boolean {
        val newOccasional = OccasionalTrip(
            id = java.util.UUID.randomUUID().toString(),
            passengerName = passengerName,
            date = date,
            origin = origin,
            destination = destination
        )
        return dataSource.addOccasionalTrip(newOccasional)
    }
}

enum class LoginResult {
    SUCCESS,
    MUST_CHANGE_PASSWORD,
    USER_NOT_FOUND,
    WRONG_PASSWORD
}
