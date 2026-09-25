@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.fleet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.dashboard.statusColor
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.FleetViewModel

@Composable
fun FleetScreen(
    onBack: () -> Unit,
    onOpenVehicle: (Long) -> Unit
) {
    val fleetVm: FleetViewModel = viewModel(factory = AppViewModelFactory)

    LaunchedEffect(Unit) {
        fleetVm.loadAllVehicles()
        fleetVm.refreshStatus()
    }

    val items = fleetVm.statusItems
    val atrasados = items.count { it.status.name == "ATRASADO" }
    val proximos = items.count { it.status.name == "PROXIMO" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flotilla y estado") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryPill("Al día", items.count { it.status.name == "AL_DIA" }, Color(0xFF2E7D32), Modifier.weight(1f))
                    SummaryPill("Próximos", proximos, Color(0xFFF9A825), Modifier.weight(1f))
                    SummaryPill("Atrasados", atrasados, Color(0xFFC62828), Modifier.weight(1f))
                }
            }

            if (fleetVm.loading && items.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (items.isEmpty()) {
                item { EmptyState("Aún no hay vehículos registrados.") }
            } else {
                items(items) { row ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenVehicle(row.vehicle.id) }
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(Modifier.size(12.dp).background(statusColor(row.status), CircleShape))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("${row.vehicle.placa} · ${row.vehicle.marca} ${row.vehicle.modelo}", fontWeight = FontWeight.Bold)
                                Text(
                                    "${row.vehicle.kilometrajeActual.toLong()} km · ${row.vehicle.conductorId?.let { "Conductor asignado" } ?: "Sin conductor"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(row.status.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Text(row.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (row.documents.isNotEmpty()) {
                                    row.documents.forEach { doc ->
                                        Text(doc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(12.dp).background(color, CircleShape))
            Text("$count", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}