@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.VehicleType
import com.example.controldeflotillastransandina.ui.components.AppErrorMessage
import com.example.controldeflotillastransandina.ui.components.ChipSelector
import com.example.controldeflotillastransandina.ui.components.DateField
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel

@Composable
fun VehicleFormScreen(
    vehicleId: Long?,
    vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory),
    onBack: () -> Unit
) {
    var placa by rememberSaveable { mutableStateOf("") }
    var marca by rememberSaveable { mutableStateOf("") }
    var modelo by rememberSaveable { mutableStateOf("") }
    var anio by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf(VehicleType.LIVIANO) }
    var capacidad by rememberSaveable { mutableStateOf("") }
    var kilometraje by rememberSaveable { mutableStateOf("") }
    var fechaMarchamo by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaRevision by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaSeguro by rememberSaveable { mutableStateOf<Long?>(null) }
    var conductorId by rememberSaveable { mutableStateOf<Long?>(null) }
    var frecuenciaKm by rememberSaveable { mutableStateOf("10000") }
    var frecuenciaDias by rememberSaveable { mutableStateOf("90") }
    var loaded by rememberSaveable { mutableStateOf(vehicleId == null) }

    LaunchedEffect(vehicleId) {
        vehicleVm.loadDrivers()
        if (vehicleId != null) {
            vehicleVm.loadVehicle(vehicleId)
            vehicleVm.vehicles.let { } // no-op: read from state below
        }
    }

    val editing = vehicleVm.selected
    LaunchedEffect(editing?.id) {
        if (editing != null && editing.id == vehicleId) {
            placa = editing.placa
            marca = editing.marca
            modelo = editing.modelo
            anio = editing.anio.toString()
            tipo = VehicleType.entries.firstOrNull { it.name == editing.tipo } ?: VehicleType.LIVIANO
            capacidad = editing.capacidad
            kilometraje = editing.kilometrajeActual.toString()
            fechaMarchamo = editing.fechaMarchamo
            fechaRevision = editing.fechaRevision
            fechaSeguro = editing.fechaSeguro
            conductorId = editing.conductorId
            frecuenciaKm = editing.frecuenciaKm.toString()
            frecuenciaDias = editing.frecuenciaDias.toString()
            loaded = true
        }
    }

    LaunchedEffect(vehicleVm.saved) {
        if (vehicleVm.saved) onBack()
    }

    val drivers = vehicleVm.drivers

    val yearNow = java.time.LocalDate.now().year

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vehicleId == null) "Registrar vehículo" else "Editar vehículo") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding)) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Datos del vehículo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                vehicleVm.error?.let { AppErrorMessage(it) }

                OutlinedTextField(placa, { placa = it.uppercase() }, Modifier.fillMaxWidth(), label = { Text("Placa") }, singleLine = true)
                OutlinedTextField(marca, { marca = it }, Modifier.fillMaxWidth(), label = { Text("Marca") }, singleLine = true)
                OutlinedTextField(modelo, { modelo = it }, Modifier.fillMaxWidth(), label = { Text("Modelo") }, singleLine = true)

                Text("Año", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    anio,
                    { anio = it.filter { c -> c.isDigit() }.take(4) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Año (mín $yearNow anterior)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Tipo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ChipSelector(VehicleType.entries, tipo, { it.displayName }, { tipo = it })

                OutlinedTextField(capacidad, { capacidad = it }, Modifier.fillMaxWidth(), label = { Text("Capacidad (carga o pasajeros)") }, singleLine = true)

                Text("Kilometraje", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    kilometraje,
                    { kilometraje = it.filter { c -> c.isDigit() }.take(8) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Kilometraje actual (km)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Documentos legales", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                DateField("Vencimiento marchamo", fechaMarchamo, { fechaMarchamo = it })
                DateField("Vencimiento revisión técnica", fechaRevision, { fechaRevision = it })
                DateField("Vencimiento seguro", fechaSeguro, { fechaSeguro = it })

                Text("Conductor asignado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ChipSelector(
                    listOf<Long?>(null) + drivers.filter { it.rol == UserRole.CONDUCTOR.name }.map { it.id },
                    conductorId,
                    { id -> drivers.firstOrNull { it.id == id }?.nombre ?: "Sin asignar" },
                    { conductorId = it }
                )

                Text("Frecuencia de mantenimiento preventivo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    frecuenciaKm,
                    { frecuenciaKm = it.filter { c -> c.isDigit() }.take(6) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Kilómetros entre servicios") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    frecuenciaDias,
                    { frecuenciaDias = it.filter { c -> c.isDigit() }.take(3) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Días entre servicios") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = {
                        vehicleVm.saveVehicle(
                            vehicleId,
                            placa, marca, modelo,
                            anio.toIntOrNull() ?: 0,
                            tipo.name,
                            capacidad,
                            kilometraje.toDoubleOrNull() ?: 0.0,
                            fechaMarchamo, fechaRevision, fechaSeguro,
                            conductorId,
                            frecuenciaKm.toIntOrNull() ?: 10000,
                            frecuenciaDias.toIntOrNull() ?: 90
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !vehicleVm.busy
                ) {
                    Text(if (vehicleVm.busy) "Guardandoâ€¦" else if (vehicleId == null) "Registrar vehículo" else "Guardar cambios")
                }
                TextButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("Cancelar") }
            }
        }
    }
}