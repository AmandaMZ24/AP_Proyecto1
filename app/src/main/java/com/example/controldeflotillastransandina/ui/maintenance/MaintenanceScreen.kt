package com.example.controldeflotillastransandina.ui.maintenance

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.data.MaintenanceType
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.ChipSelector
import com.example.controldeflotillastransandina.ui.components.DateField
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.components.ScreenHeader
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.MaintenanceViewModel
import com.example.controldeflotillastransandina.util.formatEpochDay

@Composable
fun MaintenanceScreen(
    user: UserEntity,
    modifier: Modifier = Modifier,
    onOpenDetail: (Long) -> Unit,
    onNewMaintenance: () -> Unit
) {
    val maintenanceVm: MaintenanceViewModel = viewModel(factory = AppViewModelFactory)
    val role = UserRole.valueOf(user.rol)

    var tipo by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaDesde by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaHasta by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(user.id, role, tipo, fechaDesde, fechaHasta) {
        if (role == UserRole.CONDUCTOR) {
            maintenanceVm.loadFilteredForDriver(user.id, tipo, fechaDesde, fechaHasta)
        } else {
            maintenanceVm.loadFiltered(null, tipo, fechaDesde, fechaHasta)
        }
    }

    val items = maintenanceVm.items
    val activeFilter = tipo != null || fechaDesde != null || fechaHasta != null

    LazyColumn(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            ScreenHeader("Mantenimientos", "Historial preventivo y correctivo de la flotilla.")
        }
        item {
            Button(onClick = onNewMaintenance, Modifier.fillMaxWidth()) { Text("Registrar mantenimiento") }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Filtros", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    ChipSelector(
                        options = listOf(null) + MaintenanceType.entries.map { it.name },
                        selected = tipo,
                        labelOf = { t -> t?.let { name -> MaintenanceType.entries.first { it.name == name }.displayName } ?: "Todos los tipos" },
                        onSelect = { tipo = it }
                    )
                    DateField("Desde", fechaDesde, { fechaDesde = it })
                    DateField("Hasta", fechaHasta, { fechaHasta = it })
                    if (activeFilter) {
                        TextButton(onClick = {
                            tipo = null
                            fechaDesde = null
                            fechaHasta = null
                        }) { Text("Limpiar filtros") }
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item { EmptyState("No hay mantenimientos que coincidan con los filtros.") }
        } else {
            items(items) { item ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(item.maintenance.id) }
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.placa, fontWeight = FontWeight.Bold)
                            Text("₡${item.maintenance.costo.toInt()}", color = MaterialTheme.colorScheme.primary)
                        }
                        Text(item.maintenance.categoria)
                        Text(
                            "${item.maintenance.tipo} · ${item.maintenance.fecha.formatEpochDay()} · ${item.userName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("${item.maintenance.kilometraje.toLong()} km", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}