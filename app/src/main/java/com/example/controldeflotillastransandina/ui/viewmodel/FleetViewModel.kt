package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldeflotillastransandina.data.AppRepository
import com.example.controldeflotillastransandina.data.entity.VehicleEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class FleetStatus(val displayName: String, val colorOrdinal: Int) {
    AL_DIA("Al día", 0),
    PROXIMO("Próximo a mantenimiento", 1),
    ATRASADO("Mantenimiento atrasado", 2)
}

data class FleetStatusItem(
    val vehicle: VehicleEntity,
    val status: FleetStatus,
    val detail: String,
    val documents: List<String>
)

class FleetViewModel(private val repo: AppRepository) : ViewModel() {

    var vehicles by mutableStateOf<List<VehicleEntity>>(emptyList())
        private set
    var statusItems by mutableStateOf<List<FleetStatusItem>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set

    fun loadAllVehicles() {
        viewModelScope.launch {
            repo.getAllVehicles().collect { vehicles = it }
        }
    }

    fun refreshStatus() {
        refreshStatusInternal()
    }

    private fun refreshStatusInternal() {
        viewModelScope.launch {
            loading = true
            val list = repo.getAllVehicles().firstOrNull().orEmpty()
            val deferred = list.map { v ->
                async { computeStatus(v) }
            }
            statusItems = deferred.awaitAll().sortedBy { it.vehicle.placa }
            loading = false
        }
    }

    private suspend fun computeStatus(v: VehicleEntity): FleetStatusItem {
        val lastPrev = repo.getLastPreventiveOnce(v.id)
        val today = java.time.LocalDate.now().toEpochDay()
        val docs = mutableListOf<String>()

        if (v.fechaMarchamo != null) docs += docState("marchamo", v.fechaMarchamo, today)
        if (v.fechaRevision != null) docs += docState("revisión técnica", v.fechaRevision, today)
        if (v.fechaSeguro != null) docs += docState("seguro", v.fechaSeguro, today)

        val status: FleetStatus
        val detail: String
        if (lastPrev == null) {
            status = FleetStatus.AL_DIA
            detail = "Sin mantenimiento preventivo registrado"
        } else {
            val dueKm = lastPrev.kilometraje + v.frecuenciaKm
            val remainingKm = (dueKm - v.kilometrajeActual).toLong()
            val betweenDays = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.ofEpochDay(lastPrev.fecha),
                java.time.LocalDate.now()
            )
            status = when {
                v.kilometrajeActual > dueKm -> FleetStatus.ATRASADO
                betweenDays > v.frecuenciaDias -> FleetStatus.ATRASADO
                remainingKm < v.frecuenciaKm / 5 || betweenDays > v.frecuenciaDias - 15 -> FleetStatus.PROXIMO
                else -> FleetStatus.AL_DIA
            }
            detail = when (status) {
                FleetStatus.ATRASADO -> "Supera el intervalo preventivo"
                FleetStatus.PROXIMO -> "Próximo servicio a ${"%,d".format(remainingKm).replace(',', ' ')} km"
                else -> "Último servicio hace ${betweenDays} días"
            }
        }
        return FleetStatusItem(v, status, detail, docs)
    }

    private fun docState(label: String, date: Long, today: Long): String {
        val diff = java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.ofEpochDay(today),
            java.time.LocalDate.ofEpochDay(date)
        )
        return when {
            diff < 0 -> "$label vencido"
            diff <= 30 -> "$label en $diff días"
            else -> "$label OK"
        }
    }
}