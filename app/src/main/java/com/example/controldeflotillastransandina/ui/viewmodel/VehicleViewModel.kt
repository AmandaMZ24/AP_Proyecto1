package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldeflotillastransandina.data.AppRepository
import com.example.controldeflotillastransandina.data.AppResult
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.data.entity.VehicleEntity
import kotlinx.coroutines.launch

class VehicleViewModel(private val repo: AppRepository) : ViewModel() {

    var vehicles by mutableStateOf<List<VehicleEntity>>(emptyList())
        private set
    var selected by mutableStateOf<VehicleEntity?>(null)
        private set
    var drivers by mutableStateOf<List<UserEntity>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var saved by mutableStateOf(false)
        private set

    fun loadAll() {
        viewModelScope.launch {
            loading = true
            repo.getAllVehicles().collect { vehicles = it }
            loading = false
        }
    }

    fun loadForDriver(driverId: Long) {
        viewModelScope.launch {
            loading = true
            repo.getVehiclesForDriver(driverId).collect { vehicles = it }
            loading = false
        }
    }

    fun loadVehicle(id: Long) {
        viewModelScope.launch {
            val vehicle = repo.getVehicleOnce(id)
            if (vehicle != null) selected = vehicle
        }
    }

    fun loadDrivers() {
        viewModelScope.launch {
            drivers = repo.getAllConductorsOnce()
        }
    }

    fun saveVehicle(
        vehicleId: Long?,
        placa: String, marca: String, modelo: String, anio: Int, tipo: String,
        capacidad: String, kilometraje: Double, fechaMarchamo: Long?, fechaRevision: Long?,
        fechaSeguro: Long?, conductorId: Long?, frecuenciaKm: Int, frecuenciaDias: Int
    ) {
        viewModelScope.launch {
            busy = true
            error = null
            val result: AppResult<*> = if (vehicleId == null) {
                repo.registerVehicle(
                    placa, marca, modelo, anio, tipo, capacidad, kilometraje,
                    fechaMarchamo, fechaRevision, fechaSeguro, conductorId, frecuenciaKm, frecuenciaDias
                )
            } else {
                val current = repo.getVehicleOnce(vehicleId)
                if (current == null) {
                    AppResult.Error("Vehículo no encontrado.")
                } else {
                    repo.updateVehicle(
                        current.copy(
                            placa = placa.trim().uppercase(), marca = marca.trim(), modelo = modelo.trim(),
                            anio = anio, tipo = tipo, capacidad = capacidad.trim(),
                            kilometrajeActual = kilometraje, fechaMarchamo = fechaMarchamo,
                            fechaRevision = fechaRevision, fechaSeguro = fechaSeguro,
                            conductorId = conductorId, frecuenciaKm = frecuenciaKm, frecuenciaDias = frecuenciaDias
                        )
                    )
                }
            }
            when (result) {
                is AppResult.Success -> {
                    saved = true
                    repo.assignDriver(requireNotNull(vehicleId ?: (result.data as Long)), conductorId)
                    repo.refreshFleetAlerts()
                }
                is AppResult.Error -> error = result.message
            }
            busy = false
        }
    }

    fun setActive(id: Long, active: Boolean) {
        viewModelScope.launch {
            repo.setVehicleActive(id, active)
            repo.refreshFleetAlerts()
            selected = selected?.copy(activo = active)
        }
    }

    fun reassignDriver(vehicleId: Long, driverId: Long?) {
        viewModelScope.launch {
            repo.assignDriver(vehicleId, driverId)
            selected = selected?.copy(conductorId = driverId)
        }
    }

    fun clearFlags() {
        error = null
        saved = false
    }
}