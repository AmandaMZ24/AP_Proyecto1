@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.vehicles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.VehicleType
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.CenteredLoading
import com.example.controldeflotillastransandina.ui.components.ChipSelector
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.components.InfoRow
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.MaintenanceViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.MileageViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel
import com.example.controldeflotillastransandina.util.formatEpochDay

@Composable
fun VehicleDetailScreen(
    vehicleId: Long,
    vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory),
    maintenanceVm: MaintenanceViewModel = viewModel(factory = AppViewModelFactory),
    mileageVm: MileageViewModel = viewModel(factory = AppViewModelFactory),
    currentUser: UserEntity? = null,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNewMaintenance: (Long) -> Unit,
    onRegisterMileage: (Long) -> Unit,
    onOpenMaintenance: (Long) -> Unit
) {
    val user = currentUser
    val role = user?.let { runCatching { UserRole.valueOf(it.rol) }.getOrNull() } ?: UserRole.CONDUCTOR
    val isManagement = role == UserRole.ENCARGADO_FLOTA || role == UserRole.ADMINISTRADOR

    LaunchedEffect(vehicleId) {
        vehicleVm.loadVehicle(vehicleId)
        vehicleVm.loadDrivers()
        maintenanceVm.loadForVehicle(vehicleId)
        mileageVm.load(vehicleId)
    }

    val vehicle = vehicleVm.selected
    val driver = vehicleVm.drivers.firstOrNull { it.id == vehicle?.conductorId }
    val history = maintenanceVm.items
    val mileageCount = mileageVm.history.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vehicle?.placa ?: "Vehículo") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        if (vehicle == null) {
            CenteredLoading(Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${vehicle.marca} ${vehicle.modelo}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "${vehicle.anio} · ${VehicleType.entries.firstOrNull { it.name == vehicle.tipo }?.displayName ?: vehicle.tipo}" +
                                    if (!vehicle.activo) " · INACTIVO" else "",
                                color = if (vehicle.activo) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                            )
                            HorizontalDivider()
                            InfoRow("Placa", vehicle.placa)
                            InfoRow("Capacidad", vehicle.capacidad)
                            InfoRow("Odómetro", "${vehicle.kilometrajeActual.toLong()} km")
                            InfoRow("Conductor", driver?.nombre ?: "Sin asignar")
                        }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Documentos legales", fontWeight = FontWeight.Bold)
                            InfoRow("Marchamo", vehicle.fechaMarchamo?.formatEpochDay() ?: "Sin registrar")
                            InfoRow("Revisión técnica", vehicle.fechaRevision?.formatEpochDay() ?: "Sin registrar")
                            InfoRow("Seguro", vehicle.fechaSeguro?.formatEpochDay() ?: "Sin registrar")
                        }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Mantenimiento preventivo", fontWeight = FontWeight.Bold)
                            InfoRow("Frecuencia", "Cada ${vehicle.frecuenciaKm} km o ${vehicle.frecuenciaDias} días")
                            InfoRow("Registros de kilometraje", "$mileageCount")
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isManagement) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = onEdit, Modifier.weight(1f)) { Text("Editar") }
                                Button(onClick = { vehicleVm.setActive(vehicle.id, !vehicle.activo) }, Modifier.weight(1f)) {
                                    Text(if (vehicle.activo) "Inactivar" else "Reactivar")
                                }
                            }
                            OutlinedButton(onClick = { onNewMaintenance(vehicle.id) }, Modifier.fillMaxWidth()) {
                                Text("Registrar mantenimiento")
                            }
                            OutlinedButton(onClick = { onRegisterMileage(vehicle.id) }, Modifier.fillMaxWidth()) {
                                Text("Registrar / ver kilometraje")
                            }
                        } else if (role == UserRole.MECANICO) {
                            Button(onClick = { onNewMaintenance(vehicle.id) }, Modifier.fillMaxWidth()) {
                                Text("Registrar mantenimiento")
                            }
                        } else {
                            if (vehicle.activo) {
                                Button(onClick = { onRegisterMileage(vehicle.id) }, Modifier.fillMaxWidth()) {
                                    Text("Registrar kilometraje")
                                }
                                OutlinedButton(onClick = { onNewMaintenance(vehicle.id) }, Modifier.fillMaxWidth()) {
                                    Text("Registrar mantenimiento")
                                }
                            }
                        }
                    }
                }

                if (isManagement && vehicleVm.drivers.isNotEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Reasignar conductor", fontWeight = FontWeight.Bold)
                                ChipSelector(
                                    options = listOf<Long?>(null) + vehicleVm.drivers.map { it.id },
                                    selected = vehicle.conductorId,
                                    labelOf = { id -> vehicleVm.drivers.firstOrNull { it.id == id }?.nombre ?: "Sin asignar" },
                                    onSelect = { vehicleVm.reassignDriver(vehicle.id, it) }
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Historial de mantenimientos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (history.isEmpty()) {
                    item { EmptyState("Sin mantenimientos registrados.") }
                } else {
                    items(history) { item ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpenMaintenance(item.maintenance.id) }
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(item.maintenance.categoria, fontWeight = FontWeight.Bold)
                                Text(
                                    "${item.maintenance.tipo} · ${item.maintenance.fecha.formatEpochDay()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "₡${item.maintenance.costo.toInt()} · ${item.maintenance.kilometraje.toLong()} km",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}