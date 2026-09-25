package com.example.controldeflotillastransandina.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.components.ScreenHeader
import com.example.controldeflotillastransandina.ui.components.StatCard
import com.example.controldeflotillastransandina.ui.viewmodel.AlertsViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.FleetStatus
import com.example.controldeflotillastransandina.ui.viewmodel.FleetViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.MaintenanceViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel

private val green = Color(0xFF2E7D32)
private val amber = Color(0xFFF9A825)
private val red = Color(0xFFC62828)

@Composable
fun statusColor(status: FleetStatus): Color = when (status) {
    FleetStatus.AL_DIA -> green
    FleetStatus.PROXIMO -> amber
    FleetStatus.ATRASADO -> red
}

@Composable
fun StatusDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(12.dp)
            .background(color, CircleShape)
    )
}

@Composable
fun DashboardScreen(
    user: UserEntity,
    modifier: Modifier = Modifier,
    onOpenVehicle: (Long) -> Unit,
    onOpenFleet: () -> Unit,
    onNewMaintenance: (Long?) -> Unit
) {
    val vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory)
    val fleetVm: FleetViewModel = viewModel(factory = AppViewModelFactory)
    val alertsVm: AlertsViewModel = viewModel(factory = AppViewModelFactory)
    val maintenanceVm: MaintenanceViewModel = viewModel(factory = AppViewModelFactory)
    val role = UserRole.valueOf(user.rol)

    LaunchedEffect(user.id, role) {
        if (role == UserRole.CONDUCTOR) {
            vehicleVm.loadForDriver(user.id)
        } else {
            vehicleVm.loadAll()
            alertsVm.loadForFleet()
        }
        fleetVm.refreshStatus()
        alertsVm.loadForUser(user.id)
        maintenanceVm.loadAll()
    }

    val visibleStatus = fleetVm.statusItems.filter { item ->
        if (role == UserRole.CONDUCTOR) item.vehicle.conductorId == user.id else true
    }

    val pendingCount = fleetVm.statusItems.count { it.status == FleetStatus.ATRASADO }
    val alertCount = alertsVm.alerts.size

    LazyColumn(
        modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Buenos días", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Centro de control", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(UserRole.valueOf(user.rol).displayName, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Vehículos", vehicleVm.vehicles.size.toString(), Modifier.weight(1f))
                StatCard(
                    if (role.isManagement) "Por atender" else "Alertas",
                    (if (role.isManagement) pendingCount else alertCount).toString(),
                    Modifier.weight(1f),
                    accent = (if (role.isManagement) pendingCount else alertCount) > 0
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Estado de la flotilla", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (role.isManagement) {
                    TextButton(onClick = onOpenFleet) { Text("Ver flotilla") }
                }
            }
        }

        val preview = visibleStatus.take(4)
        if (preview.isEmpty()) {
            item {
                EmptyState("Aún no hay vehículos registrados.")
            }
        } else {
            items(preview) { item ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenVehicle(item.vehicle.id) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusDot(statusColor(item.status))
                        Column(Modifier.weight(1f)) {
                            Text(item.vehicle.placa, fontWeight = FontWeight.Bold)
                            Text(
                                "${item.vehicle.marca} ${item.vehicle.modelo} · ${item.vehicle.kilometrajeActual.toLong()} km",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(item.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private val UserRole.isManagement: Boolean
    get() = this == UserRole.ENCARGADO_FLOTA || this == UserRole.ADMINISTRADOR