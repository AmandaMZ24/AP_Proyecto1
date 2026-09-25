@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.MaintenanceType
import com.example.controldeflotillastransandina.data.entity.VehicleEntity
import com.example.controldeflotillastransandina.ui.components.ChipSelector
import com.example.controldeflotillastransandina.ui.components.DateField
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.components.ScreenHeader
import com.example.controldeflotillastransandina.ui.components.StatCard
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.MaintenanceViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel
import com.example.controldeflotillastransandina.util.formatEpochDay

@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit
) {
    val maintenanceVm: MaintenanceViewModel = viewModel(factory = AppViewModelFactory)
    val vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory)

    var vehicleFilter by rememberSaveable { mutableStateOf<Long?>(null) }
    var tipoFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaDesde by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaHasta by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        vehicleVm.loadAll()
    }
    LaunchedEffect(vehicleFilter, tipoFilter, fechaDesde, fechaHasta) {
        maintenanceVm.loadFiltered(vehicleFilter, tipoFilter, fechaDesde, fechaHasta)
        maintenanceVm.loadSummary(vehicleFilter, tipoFilter, fechaDesde, fechaHasta)
    }

    val items = maintenanceVm.items
    val summary = maintenanceVm.summary
    val total = summary.sumOf { it.total }
    val vehicles = vehicleVm.vehicles
    val totalCount = items.size
    val activeFilter = vehicleFilter != null || tipoFilter != null || fechaDesde != null || fechaHasta != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        LazyColumn(
        Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader("Reportes", "Costos y mantenimientos por vehículo y periodo.")
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Filtros", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    VehicleFilterPicker(vehicles, vehicleFilter) { vehicleFilter = it }
                    ChipSelector(
                        options = listOf<String?>(null) + MaintenanceType.entries.map { it.name },
                        selected = tipoFilter,
                        labelOf = { t -> t?.let { name -> MaintenanceType.entries.first { it.name == name }.displayName } ?: "Todos los tipos" },
                        onSelect = { tipoFilter = it }
                    )
                    DateField("Desde", fechaDesde, { fechaDesde = it })
                    DateField("Hasta", fechaHasta, { fechaHasta = it })
                    if (activeFilter) {
                        TextButton(onClick = {
                            vehicleFilter = null
                            tipoFilter = null
                            fechaDesde = null
                            fechaHasta = null
                        }) { Text("Limpiar filtros") }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Costo total", "₡${total.toInt()}", Modifier.weight(1f), accent = true)
                StatCard("Servicios", totalCount.toString(), Modifier.weight(1f))
            }
        }
        if (summary.isNotEmpty()) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Resumen por vehículo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        summary.forEach { s ->
                            val v = vehicles.firstOrNull { it.id == s.vehicleId }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${v?.placa ?: "Vehículo #${s.vehicleId}"} (${s.count} servicios)")
                                Text("₡${s.total.toInt()}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        if (items.isEmpty()) {
            item { EmptyState("No hay registros con los filtros seleccionados.") }
        } else {
            item {
                Text("Detalle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(items) { row ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(row.maintenance.id) }
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${row.placa} · ${row.maintenance.categoria}", fontWeight = FontWeight.Bold)
                            Text("₡${row.maintenance.costo.toInt()}", color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            "${row.maintenance.tipo} · ${row.maintenance.fecha.formatEpochDay()} · ${row.userName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleFilterPicker(
    vehicles: List<VehicleEntity>,
    selected: Long?,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selected?.let { id ->
        vehicles.firstOrNull { it.id == id }?.let { "${it.placa} · ${it.marca} ${it.modelo}" } ?: "Todos los vehículos"
    } ?: "Todos los vehículos"

    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Vehículo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Todos los vehículos") },
                onClick = {
                    onSelect(null)
                    expanded = false
                }
            )
            vehicles.forEach { v ->
                DropdownMenuItem(
                    text = { Text("${v.placa} · ${v.marca} ${v.modelo}") },
                    onClick = {
                        onSelect(v.id)
                        expanded = false
                    }
                )
            }
        }
    }
}