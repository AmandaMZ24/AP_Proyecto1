@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.VehicleType
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.data.entity.VehicleEntity
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.components.ScreenHeader
import com.example.controldeflotillastransandina.ui.dashboard.StatusDot
import com.example.controldeflotillastransandina.ui.dashboard.statusColor
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.FleetViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel

@Composable
fun VehiclesScreen(
    user: UserEntity,
    modifier: Modifier = Modifier,
    onOpenVehicle: (Long) -> Unit,
    onNewVehicle: () -> Unit,
    onNewMaintenance: (Long) -> Unit,
    onRegisterMileage: (Long) -> Unit
) {
    val vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory)
    val fleetVm: FleetViewModel = viewModel(factory = AppViewModelFactory)
    val role = UserRole.valueOf(user.rol)
    val isManagement = role == UserRole.ENCARGADO_FLOTA || role == UserRole.ADMINISTRADOR
    var showAll by remember { mutableStateOf(false) }

    LaunchedEffect(user.id, role) {
        if (role == UserRole.CONDUCTOR) {
            vehicleVm.loadForDriver(user.id)
        } else {
            vehicleVm.loadAll()
        }
        fleetVm.refreshStatus()
    }

    val vehicles = vehicleVm.vehicles.filter { v ->
        v.activo || (isManagement && showAll)
    }
    val statusById = fleetVm.statusItems.associateBy { it.vehicle.id }

    LazyColumn(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Vehículos", "Consulta la ficha y el estado de cada unidad.")
        }
        if (isManagement) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!showAll, { showAll = false }, label = { Text("Activos") })
                    FilterChip(showAll, { showAll = true }, label = { Text("Todos") })
                }
            }
            item {
                Button(onClick = onNewVehicle, Modifier.fillMaxWidth()) { Text("+ Registrar vehículo") }
            }
        }
        if (vehicles.isEmpty()) {
            item { EmptyState("No hay vehículos para mostrar.") }
        } else {
            items(vehicles) { v ->
                val status = statusById[v.id]
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(v.placa, fontWeight = FontWeight.Bold)
                            status?.let {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    StatusDot(statusColor(it.status))
                                    Text(it.status.displayName, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Text("${v.marca} ${v.modelo} ${v.anio} · ${VehicleType.entries.firstOrNull { t -> t.name == v.tipo }?.displayName ?: v.tipo}")
                        Text("Capacidad: ${v.capacidad}", style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Odómetro: ${v.kilometrajeActual.toLong()} km", style = MaterialTheme.typography.bodyMedium)
                            if (!v.activo) {
                                Text("INACTIVO", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { onOpenVehicle(v.id) }) { Text("Ver ficha") }
                            if (role == UserRole.CONDUCTOR && v.activo) {
                                TextButton(onClick = { onRegisterMileage(v.id) }) { Text("Kilometraje") }
                            }
                            if (role != UserRole.ADMINISTRADOR) {
                                TextButton(onClick = { onNewMaintenance(v.id) }) { Text("Mantenimiento") }
                            }
                        }
                    }
                }
            }
        }
    }
}