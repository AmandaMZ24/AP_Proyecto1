@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.mileage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.entity.OdometerRecordEntity
import com.example.controldeflotillastransandina.ui.components.AppErrorMessage
import com.example.controldeflotillastransandina.ui.components.DateField
import com.example.controldeflotillastransandina.util.formatEpochDay
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.MileageViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel
import java.time.LocalDate

@Composable
fun MileageScreen(
    vehicleId: Long,
    vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory),
    mileageVm: MileageViewModel = viewModel(factory = AppViewModelFactory),
    onBack: () -> Unit
) {
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var kilometraje by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(vehicleId) {
        mileageVm.clearFlags()
        vehicleVm.loadVehicle(vehicleId)
        mileageVm.load(vehicleId)
    }

    LaunchedEffect(mileageVm.saved) {
        if (mileageVm.saved) {
            kilometraje = ""
            vehicleVm.loadVehicle(vehicleId)
            mileageVm.saved = false
        }
    }

    val vehicle = vehicleVm.selected
    val history = mileageVm.history

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kilometraje") },
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
                vehicle?.let {
                    Text("${" "}${it.placa} · ${it.marca} ${it.modelo}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                } ?: run {
                    Text("Vehículo N° $vehicleId", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Registrar lectura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        localError?.let { AppErrorMessage(it) }
                        DateField("Fecha de la lectura", fecha, { fecha = it })
                        OutlinedTextField(
                            kilometraje,
                            { kilometraje = it.filter { c -> c.isDigit() }.take(8) },
                            Modifier.fillMaxWidth(),
                            label = { Text("Kilometraje (km)") },
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val km = kilometraje.toDoubleOrNull()
                                val prev = vehicle?.kilometrajeActual ?: history.lastOrNull()?.kilometraje ?: 0.0
                                localError = when {
                                    km == null -> "Ingrese el kilometraje."
                                    km <= prev -> "El kilometraje debe ser mayor que el último registrado (${prev.toLong()} km)."
                                    else -> null
                                }
                                if (localError == null && km != null) {
                                    mileageVm.register(vehicleId, fecha, km)
                                }
                            },
                            Modifier.fillMaxWidth(),
                            enabled = !mileageVm.busy
                        ) { Text("Guardar lectura") }
                        vehicle?.let { v ->
                            Text(
                                "Kilometraje actual del vehículo: ${v.kilometrajeActual.toLong()} km",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (history.size >= 2) {
                item {
                    Text("Histórico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                item {
                    MileageChart(history.reversed().takeLast(12))
                }
            }

            if (history.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                        Text("Aún no hay lecturas registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(history.sortedByDescending { it.fecha }) { record ->
                    OdometerRow(record, vehicle?.kilometrajeActual)
                }
            }
        }
    }
}

@Composable
private fun MileageChart(records: List<OdometerRecordEntity>) {
    val line = MaterialTheme.colorScheme.primary
    val point = MaterialTheme.colorScheme.tertiary
    Card(Modifier.fillMaxWidth().height(160.dp)) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val n = records.size
            if (n >= 2) {
                val min = records.minOf { it.kilometraje }.toFloat()
                val max = records.maxOf { it.kilometraje }.toFloat()
                val span = (max - min).coerceAtLeast(1f)
                val stepX = size.width / (n - 1)
                val pts = records.mapIndexed { i, r ->
                    Offset(i * stepX, size.height - ((r.kilometraje.toFloat() - min) / span) * (size.height - 8f) - 4f)
                }
                for (i in 0 until n - 1) {
                    drawLine(line, pts[i], pts[i + 1], strokeWidth = 3f, cap = StrokeCap.Round)
                }
                pts.forEach { p ->
                    drawCircle(point, radius = 4f, center = p)
                }
            }
        }
    }
}

@Composable
private fun OdometerRow(record: OdometerRecordEntity, current: Double?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(record.fecha.formatEpochDay(), style = MaterialTheme.typography.bodyMedium)
        Text(
            "${record.kilometraje.toLong()} km",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}