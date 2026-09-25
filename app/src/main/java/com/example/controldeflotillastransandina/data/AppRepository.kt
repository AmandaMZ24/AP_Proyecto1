package com.example.controldeflotillastransandina.data

import com.example.controldeflotillastransandina.core.PasswordHasher
import com.example.controldeflotillastransandina.data.dao.CostSummary
import com.example.controldeflotillastransandina.data.dao.MaintenanceWithInfo
import com.example.controldeflotillastransandina.data.entity.AlertEntity
import com.example.controldeflotillastransandina.data.entity.MaintenanceEntity
import com.example.controldeflotillastransandina.data.entity.MaintenanceImageEntity
import com.example.controldeflotillastransandina.data.entity.OdometerRecordEntity
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.data.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.Locale

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String) : AppResult<Nothing>()

    val isError: Boolean get() = this is Error
    val errorMessage: String get() = (this as? Error)?.message ?: ""
}

class AppRepository(private val db: AppDatabase) {

    private val users = db.userDao()
    private val vehicles = db.vehicleDao()
    private val maintenance = db.maintenanceDao()
    private val images = db.imageDao()
    private val odometer = db.odometerDao()
    private val alerts = db.alertDao()
    val alertService = AlertService(db)

    // ---------- Autenticación ----------

    suspend fun seedDefaultsIfEmpty() {
        if (users.countAll() > 0) return
        users.insert(
            UserEntity(
                nombre = "Administrador TransAndina",
                cedula = "000000000",
                email = "admin@transandina.com",
                telefono = "00000000",
                licencia = null,
                rol = UserRole.ENCARGADO_FLOTA.name,
                passwordHash = PasswordHasher.hash("admin123")
            )
        )
    }

    suspend fun login(email: String, password: String): AppResult<UserEntity> {
        val user = users.getByEmail(email.trim().lowercase(Locale.ROOT))
            ?: return AppResult.Error("No existe una cuenta con ese correo.")
        if (!PasswordHasher.verify(password, user.passwordHash)) {
            return AppResult.Error("Contraseña incorrecta.")
        }
        if (!user.activo) return AppResult.Error("La cuenta está desactivada. Contacte al encargado de flota.")
        return AppResult.Success(user)
    }

    suspend fun registerUser(
        nombre: String,
        cedula: String,
        email: String,
        telefono: String,
        licencia: String?,
        rol: String,
        password: String
    ): AppResult<UserEntity> {
        if (nombre.isBlank()) return AppResult.Error("El nombre es obligatorio.")
        if (cedula.isBlank() || !cedula.all { it.isDigit() } || cedula.length < 7) {
            return AppResult.Error("Ingrese una cédula válida (solo dígitos).")
        }
        val emailNorm = email.trim().lowercase(Locale.ROOT)
        if (!EmailRegex.matches(emailNorm)) return AppResult.Error("Ingrese un correo electrónico válido.")
        if (telefono.isBlank() || telefono.any { !it.isDigit() && it != ' ' && it != '-' }) {
            return AppResult.Error("Ingrese un teléfono válido.")
        }
        if (rol == UserRole.CONDUCTOR.name && licencia.isNullOrBlank()) {
            return AppResult.Error("El número de licencia es obligatorio para conductores.")
        }
        if (password.length < 6) return AppResult.Error("La contraseña debe tener al menos 6 caracteres.")
        if (users.countByEmail(emailNorm) > 0) return AppResult.Error("Ya existe una cuenta con ese correo.")
        if (users.countByCedula(cedula) > 0) return AppResult.Error("Ya existe una cuenta con esa cédula.")

        val id = users.insert(
            UserEntity(
                nombre = nombre.trim(),
                cedula = cedula,
                email = emailNorm,
                telefono = telefono.trim(),
                licencia = licencia?.trim()?.ifBlank { null },
                rol = rol,
                passwordHash = PasswordHasher.hash(password)
            )
        )
        return AppResult.Success(users.getByIdOnce(id)!!)
    }

    suspend fun resetPassword(email: String, cedula: String, newPassword: String): AppResult<Unit> {
        val user = users.getByEmail(email.trim().lowercase(Locale.ROOT))
            ?: return AppResult.Error("No existe una cuenta con ese correo.")
        if (user.cedula != cedula.trim()) return AppResult.Error("La cédula no coincide con la cuenta.")
        if (newPassword.length < 6) return AppResult.Error("La contraseña debe tener al menos 6 caracteres.")
        users.update(user.copy(passwordHash = PasswordHasher.hash(newPassword)))
        return AppResult.Success(Unit)
    }

    suspend fun updateProfile(user: UserEntity): AppResult<Unit> {
        if (user.nombre.isBlank()) return AppResult.Error("El nombre es obligatorio.")
        return AppResult.Success(users.update(user))
    }

    suspend fun changePassword(userId: Long, current: String, newPassword: String): AppResult<Unit> {
        val user = users.getByIdOnce(userId) ?: return AppResult.Error("Cuenta no encontrada.")
        if (!PasswordHasher.verify(current, user.passwordHash)) return AppResult.Error("La contraseña actual es incorrecta.")
        if (newPassword.length < 6) return AppResult.Error("La contraseña debe tener al menos 6 caracteres.")
        users.update(user.copy(passwordHash = PasswordHasher.hash(newPassword)))
        return AppResult.Success(Unit)
    }

    // ---------- Usuarios ----------

    fun getAllUsers(): Flow<List<UserEntity>> = users.getAll()
    fun getAllByRole(role: UserRole): Flow<List<UserEntity>> = users.getAllByRole(role.name)
    suspend fun getAllConductorsOnce(): List<UserEntity> = users.getAllByRoleOnce(UserRole.CONDUCTOR.name)
    suspend fun getUserOnce(id: Long): UserEntity? = users.getByIdOnce(id)
    fun getUser(id: Long): Flow<UserEntity?> = users.getById(id)
    suspend fun setUserActive(id: Long, activo: Boolean) = users.setActive(id, activo)

    // ---------- Vehículos ----------

    fun getAllVehicles(): Flow<List<VehicleEntity>> = vehicles.getAll()
    suspend fun getVehicleOnce(id: Long): VehicleEntity? = vehicles.getByIdOnce(id)
    fun getVehicle(id: Long): Flow<VehicleEntity?> = vehicles.getById(id)
    fun getVehiclesForDriver(driverId: Long): Flow<List<VehicleEntity>> = vehicles.getByDriver(driverId)

    suspend fun registerVehicle(
        placa: String, marca: String, modelo: String, anio: Int, tipo: String,
        capacidad: String, kilometraje: Double, fechaMarchamo: Long?, fechaRevision: Long?,
        fechaSeguro: Long?, conductorId: Long?, frecuenciaKm: Int, frecuenciaDias: Int
    ): AppResult<Long> {
        val validation = validateVehicle(placa, marca, modelo, anio, capacidad, kilometraje)
        if (validation.isError) return AppResult.Error(validation.errorMessage)
        if (vehicles.getByPlaca(placa.trim().uppercase()) != null) {
            return AppResult.Error("Ya existe un vehículo con la placa ${placa.trim().uppercase()}.")
        }
        val id = vehicles.insert(
            VehicleEntity(
                placa = placa.trim().uppercase(),
                marca = marca.trim(),
                modelo = modelo.trim(),
                anio = anio,
                tipo = tipo,
                capacidad = capacidad.trim(),
                kilometrajeActual = kilometraje,
                fechaMarchamo = fechaMarchamo,
                fechaRevision = fechaRevision,
                fechaSeguro = fechaSeguro,
                conductorId = conductorId,
                frecuenciaKm = frecuenciaKm,
                frecuenciaDias = frecuenciaDias
            )
        )
        vehicles.assignDriver(id, conductorId)
        return AppResult.Success(id)
    }

    suspend fun updateVehicle(vehicle: VehicleEntity): AppResult<Unit> {
        val validation = validateVehicle(
            vehicle.placa, vehicle.marca, vehicle.modelo, vehicle.anio,
            vehicle.capacidad, vehicle.kilometrajeActual
        )
        if (validation.isError) return AppResult.Error(validation.errorMessage)
        vehicles.update(vehicle)
        return AppResult.Success(Unit)
    }

    private fun validateVehicle(
        placa: String, marca: String, modelo: String, anio: Int, capacidad: String, kilometraje: Double
    ): AppResult<Unit> {
        if (placa.isBlank()) return AppResult.Error("La placa es obligatoria.")
        if (marca.isBlank()) return AppResult.Error("La marca es obligatoria.")
        if (modelo.isBlank()) return AppResult.Error("El modelo es obligatorio.")
        if (anio < 1950 || anio > LocalDate.now().year + 1) return AppResult.Error("Ingrese un año válido.")
        if (capacidad.isBlank()) return AppResult.Error("Indique la capacidad de carga o pasajeros.")
        if (kilometraje < 0) return AppResult.Error("El kilometraje no puede ser negativo.")
        return AppResult.Success(Unit)
    }

    suspend fun setVehicleActive(id: Long, activo: Boolean) = vehicles.setActive(id, activo)
    suspend fun assignDriver(vehicleId: Long, driverId: Long?) {
        val vehicle = vehicles.getByIdOnce(vehicleId) ?: return
        vehicles.assignDriver(vehicleId, driverId)
        val driver = driverId?.let { users.getByIdOnce(it) }
        alertService.notifyVehicleReassigned(
            vehicleId = vehicleId,
            driverId = driverId,
            detail = "${vehicle.placa} · Vehículo asignado a ${driver?.nombre ?: "ningún conductor"}."
        )
    }

    suspend fun setVehicleMileage(id: Long, km: Double) = vehicles.setMileage(id, km)

    // ---------- Mantenimientos ----------

    fun getAllMaintenance(): Flow<List<MaintenanceWithInfo>> = maintenance.getAllWithInfo()
    fun getMaintenanceByVehicle(vehicleId: Long): Flow<List<MaintenanceWithInfo>> =
        maintenance.getByVehicle(vehicleId)
    suspend fun getMaintenanceByIdOnce(id: Long): MaintenanceEntity? = maintenance.getByIdOnce(id)
    suspend fun getLastPreventiveOnce(vehicleId: Long): MaintenanceEntity? =
        maintenance.getLastPreventive(vehicleId)

    suspend fun getFilteredMaintenance(
        vehicleId: Long?, tipo: String?, fromDate: Long?, toDate: Long?
    ): List<MaintenanceWithInfo> = maintenance.getFiltered(vehicleId, tipo, fromDate, toDate)

    suspend fun getCostSummary(
        vehicleId: Long?, tipo: String?, fromDate: Long?, toDate: Long?
    ): List<CostSummary> = maintenance.getCostSummary(vehicleId, tipo, fromDate, toDate)

    suspend fun registerMaintenance(
        vehicleId: Long, userId: Long, tipo: String, categoria: String, fecha: Long,
        taller: String, descripcion: String, kilometraje: Double, costo: Double,
        imageUris: List<String>
    ): AppResult<Long> {
        if (taller.isBlank()) return AppResult.Error("Indique el taller o mecánico responsable.")
        if (descripcion.isBlank()) return AppResult.Error("Describe el servicio realizado.")
        if (kilometraje < 0) return AppResult.Error("El kilometraje no puede ser negativo.")
        if (costo < 0) return AppResult.Error("El costo no puede ser negativo.")

        val id = maintenance.insert(
            MaintenanceEntity(
                vehicleId = vehicleId, userId = userId, tipo = tipo, categoria = categoria,
                fecha = fecha, taller = taller.trim(), descripcion = descripcion.trim(),
                kilometraje = kilometraje, costo = costo
            )
        )
        if (imageUris.isNotEmpty()) {
            images.insertAll(imageUris.map { MaintenanceImageEntity(maintenanceId = id, uri = it) })
        }
        val vehicle = vehicles.getByIdOnce(vehicleId)
        if (vehicle != null) {
            if (kilometraje > vehicle.kilometrajeActual) {
                vehicles.setMileage(vehicleId, kilometraje)
            }
            alertService.notifyMaintenanceRegistered(vehicle)
            alertService.refreshFleetAlerts()
        }
        return AppResult.Success(id)
    }

    suspend fun updateMaintenance(m: MaintenanceEntity) = maintenance.update(m)

    suspend fun deleteMaintenance(id: Long) {
        images.deleteByMaintenance(id)
        maintenance.deleteById(id)
        alertService.refreshFleetAlerts()
    }

    suspend fun getImagesForMaintenance(id: Long): List<MaintenanceImageEntity> = images.getByMaintenance(id)

    // ---------- Kilometraje ----------

    fun getOdometerHistory(vehicleId: Long): Flow<List<OdometerRecordEntity>> =
        odometer.getByVehicle(vehicleId)

    suspend fun registerOdometer(vehicleId: Long, fecha: Long, kilometraje: Double): AppResult<Unit> {
        if (kilometraje < 0) return AppResult.Error("El kilometraje no puede ser negativo.")
        val last = odometer.getMaxMileage(vehicleId)
        if (last != null && kilometraje <= last) {
            return AppResult.Error("El kilometraje debe ser mayor al último registrado ($last).")
        }
        val vehicle = vehicles.getByIdOnce(vehicleId) ?: return AppResult.Error("Vehículo no encontrado.")
        if (kilometraje < vehicle.kilometrajeActual) {
            return AppResult.Error("El odómetro no puede ser menor al kilometraje actual del vehículo.")
        }
        odometer.insert(OdometerRecordEntity(vehicleId = vehicleId, fecha = fecha, kilometraje = kilometraje))
        vehicles.setMileage(vehicleId, kilometraje)
        alertService.refreshFleetAlerts()
        return AppResult.Success(Unit)
    }

    // ---------- Alertas ----------

    fun getActiveAlerts(): Flow<List<AlertEntity>> = alerts.getActive()
    fun getAlertsForUser(userId: Long): Flow<List<AlertEntity>> = alerts.getActiveForUser(userId)
    fun getAllAlerts(): Flow<List<AlertEntity>> = alerts.getAll()
    suspend fun markAlertAttended(id: Long) = alerts.markAttended(id)
    suspend fun markAllAlertsAttended() = alerts.markAllAttended()
    suspend fun refreshFleetAlerts() = alertService.refreshFleetAlerts()

    companion object {
        private val EmailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}