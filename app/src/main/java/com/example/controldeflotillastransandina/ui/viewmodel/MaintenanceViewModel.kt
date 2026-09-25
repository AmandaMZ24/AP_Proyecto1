package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldeflotillastransandina.data.AppRepository
import com.example.controldeflotillastransandina.data.AppResult
import com.example.controldeflotillastransandina.data.dao.CostSummary
import com.example.controldeflotillastransandina.data.dao.MaintenanceWithInfo
import com.example.controldeflotillastransandina.data.entity.MaintenanceEntity
import com.example.controldeflotillastransandina.data.entity.MaintenanceImageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MaintenanceViewModel(private val repo: AppRepository) : ViewModel() {

    var items by mutableStateOf<List<MaintenanceWithInfo>>(emptyList())
        private set
    var summary by mutableStateOf<List<CostSummary>>(emptyList())
        private set
    var images by mutableStateOf<List<MaintenanceImageEntity>>(emptyList())
        private set
    var selected by mutableStateOf<MaintenanceEntity?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var saved by mutableStateOf(false)
        private set
    var deleted by mutableStateOf(false)
        private set

    fun loadAll() {
        viewModelScope.launch {
            loading = true
            repo.getAllMaintenance().collect { items = it }
            loading = false
        }
    }

    fun loadForVehicle(vehicleId: Long) {
        viewModelScope.launch {
            loading = true
            repo.getMaintenanceByVehicle(vehicleId).collect { items = it }
            loading = false
        }
    }

    fun loadFiltered(vehicleId: Long?, tipo: String?, fromDate: Long?, toDate: Long?) {
        viewModelScope.launch {
            loading = true
            items = repo.getFilteredMaintenance(vehicleId, tipo, fromDate, toDate)
            loading = false
        }
    }

    fun loadFilteredForDriver(driverId: Long, tipo: String?, fromDate: Long?, toDate: Long?) {
        viewModelScope.launch {
            loading = true
            val vehicles = repo.getVehiclesForDriver(driverId).first()
            items = vehicles
                .flatMap { repo.getFilteredMaintenance(it.id, tipo, fromDate, toDate) }
                .sortedByDescending { it.maintenance.fecha }
            loading = false
        }
    }

    fun loadMaintenance(id: Long) {
        viewModelScope.launch {
            selected = repo.getMaintenanceByIdOnce(id)
            images = repo.getImagesForMaintenance(id)
        }
    }

    fun updateMaintenance(
        id: Long,
        vehicleId: Long,
        userId: Long,
        tipo: String, categoria: String, fecha: Long,
        taller: String, descripcion: String, kilometraje: Double, costo: Double
    ) {
        viewModelScope.launch {
            busy = true
            error = null
            selected?.let {
                repo.updateMaintenance(
                    it.copy(
                        vehicleId = vehicleId, userId = userId, tipo = tipo, categoria = categoria,
                        fecha = fecha, taller = taller, descripcion = descripcion,
                        kilometraje = kilometraje, costo = costo
                    )
                )
                saved = true
            }
            busy = false
        }
    }

    fun loadSummary(vehicleId: Long?, tipo: String?, fromDate: Long?, toDate: Long?) {
        viewModelScope.launch {
            summary = repo.getCostSummary(vehicleId, tipo, fromDate, toDate)
        }
    }

    fun loadImages(maintenanceId: Long) {
        viewModelScope.launch {
            images = repo.getImagesForMaintenance(maintenanceId)
        }
    }

    fun registerMaintenance(
        vehicleId: Long, userId: Long, tipo: String, categoria: String, fecha: Long,
        taller: String, descripcion: String, kilometraje: Double, costo: Double,
        imageUris: List<String>
    ) {
        viewModelScope.launch {
            busy = true
            error = null
            when (val result = repo.registerMaintenance(
                vehicleId, userId, tipo, categoria, fecha, taller,
                descripcion, kilometraje, costo, imageUris
            )) {
                is AppResult.Success -> saved = true
                is AppResult.Error -> error = result.message
            }
            busy = false
        }
    }

    fun deleteMaintenance(id: Long) {
        viewModelScope.launch {
            busy = true
            repo.deleteMaintenance(id)
            deleted = true
            busy = false
        }
    }

    fun clearFlags() {
        error = null
        saved = false
        deleted = false
    }
}