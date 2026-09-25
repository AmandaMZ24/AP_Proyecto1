package com.example.controldeflotillastransandina.ui.maintenance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.MaintenanceCategory
import com.example.controldeflotillastransandina.data.MaintenanceType
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.AppErrorMessage
import com.example.controldeflotillastransandina.ui.components.ChipSelector
import com.example.controldeflotillastransandina.ui.components.DateField
import com.example.controldeflotillastransandina.ui.components.UriImage
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.MaintenanceViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel
import com.example.controldeflotillastransandina.util.formatEpochDay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormScreen(
    vehicleId: Long?,
    maintenanceId: Long?,
    vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory),
    maintenanceVm: MaintenanceViewModel = viewModel(factory = AppViewModelFactory),
    currentUser: UserEntity? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user = currentUser

    var selectedVehicleId by rememberSaveable { mutableStateOf<Long?>(vehicleId) }
    var tipo by rememberSaveable { mutableStateOf(MaintenanceType.PREVENTIVO) }
    var categoria by rememberSaveable { mutableStateOf(MaintenanceCategory.ACEITE) }
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var taller by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var kilometraje by rememberSaveable { mutableStateOf("") }
    var costo by rememberSaveable { mutableStateOf("") }
    var images by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var loaded by rememberSaveable { mutableStateOf(maintenanceId == null) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val isEdit = maintenanceId != null
    val isManagement = user?.let { it.rol == UserRole.ENCARGADO_FLOTA.name || it.rol == UserRole.ADMINISTRADOR.name } == true
    val isConductor = user?.rol == UserRole.CONDUCTOR.name

    LaunchedEffect(maintenanceId) {
        if (isEdit) {
            maintenanceVm.loadMaintenance(maintenanceId)
        }
        vehicleVm.loadAll()
    }

    val editing = maintenanceVm.selected
    LaunchedEffect(editing?.id) {
        if (editing != null && editing.id == maintenanceId) {
            selectedVehicleId = editing.vehicleId
            tipo = runCatching { MaintenanceType.valueOf(editing.tipo) }.getOrDefault(MaintenanceType.PREVENTIVO)
            categoria = runCatching { MaintenanceCategory.valueOf(editing.categoria) }.getOrDefault(MaintenanceCategory.OTRO)
            fecha = editing.fecha
            taller = editing.taller
            descripcion = editing.descripcion
            kilometraje = editing.kilometraje.toLong().toString()
            costo = editing.costo.toLong().toString()
            images = maintenanceVm.images.map { it.uri }
            loaded = true
        }
    }

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) pendingPhotoUri?.let { images = images + it.toString() }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) images = images + uri.toString()
    }

    LaunchedEffect(maintenanceVm.saved) {
        if (maintenanceVm.saved) onBack()
    }

    val vehicles = vehicleVm.vehicles.filter { it.activo }
    val vehiclesForPicker = if (isConductor) vehicles.filter { it.conductorId == user?.id } else vehicles
    val selectedVehicle = vehicles.firstOrNull { it.id == selectedVehicleId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Editar mantenimiento" else "Registrar mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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
                    "Servicio realizado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                localError?.let { AppErrorMessage(it) }
                maintenanceVm.error?.let { AppErrorMessage(it) }

                Text("Vehículo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                VehiclePicker(
                    vehicles = vehiclesForPicker,
                    selected = selectedVehicleId,
                    onSelect = { id ->
                        selectedVehicleId = id
                        if (!isEdit) {
                            val v = vehicles.firstOrNull { it.id == id }
                            if (v != null && kilometraje.isBlank()) kilometraje = v.kilometrajeActual.toLong().toString()
                        }
                    },
                    enabled = !isEdit
                )

                Text("Tipo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ChipSelector(MaintenanceType.entries, tipo, { it.displayName }, { tipo = it })

                Text("Categoría del servicio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ChipSelector(MaintenanceCategory.entries, categoria, { it.displayName }, { categoria = it })

                Text("Fecha", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                DateField("Fecha del mantenimiento", fecha, {
                    fecha = it
                })
                if (!isEdit) {
                    Text(
                        selectedVehicle?.let { "Kilometraje actual del vehículo: ${it.kilometrajeActual.toLong()} km" } ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(taller, { taller = it }, Modifier.fillMaxWidth(), label = { Text("Taller o mecánico responsable") }, singleLine = true)
                OutlinedTextField(descripcion, { descripcion = it }, Modifier.fillMaxWidth(), label = { Text("Descripción del servicio") })

                OutlinedTextField(
                    kilometraje,
                    { kilometraje = it.filter { c -> c.isDigit() }.take(8) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Kilometraje al momento (km)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    costo,
                    { costo = it.filter { c -> c.isDigit() }.take(10) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Costo aproximado (₡)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (!isEdit) {
                    Text("Evidencia fotográfica", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            pendingPhotoUri = createImageFileUri(context)
                            pendingPhotoUri?.let { cameraLauncher.launch(it) }
                        }, Modifier.weight(1f)) { Text("Cámara") }
                        Button(onClick = { galleryLauncher.launch("image/*") }, Modifier.weight(1f)) { Text("Galería") }
                    }
                    if (images.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(images) { uri ->
                                PhotoThumb(
                                    uri,
                                    onRemove = { images = images - uri }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        localError = when {
                            selectedVehicleId == null -> "Seleccione un vehículo."
                            taller.isBlank() -> "Indique el taller o mecánico responsable."
                            descripcion.isBlank() -> "Describe el servicio realizado."
                            else -> null
                        }
                        val selectedId = selectedVehicleId
                        if (localError == null && user != null && selectedId != null) {
                            if (isEdit && isManagement) {
                                maintenanceVm.updateMaintenance(
                                    maintenanceId, selectedId, editing?.userId ?: user.id,
                                    tipo.name, categoria.name, fecha, taller, descripcion,
                                    kilometraje.toDoubleOrNull() ?: 0.0, costo.toDoubleOrNull() ?: 0.0
                                )
                            } else {
                                maintenanceVm.registerMaintenance(
                                    selectedId, user.id,
                                    tipo.name, categoria.name, fecha, taller, descripcion,
                                    kilometraje.toDoubleOrNull() ?: 0.0, costo.toDoubleOrNull() ?: 0.0,
                                    images
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !maintenanceVm.busy
                ) {
                    Text(if (maintenanceVm.busy) "Guardando…" else if (isEdit) "Guardar cambios" else "Registrar mantenimiento")
                }
                TextButton(onClick = onBack, Modifier.fillMaxWidth()) { Text("Cancelar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehiclePicker(
    vehicles: List<com.example.controldeflotillastransandina.data.entity.VehicleEntity>,
    selected: Long?,
    onSelect: (Long?) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val label = vehicles.firstOrNull { it.id == selected }?.let { "${it.placa} · ${it.marca} ${it.modelo}" } ?: "Seleccione un vehículo"

    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Vehículo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            enabled = enabled
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            vehicles.forEach { v ->
                DropdownMenuItem(
                    text = { Text("${v.placa} · ${v.marca} ${v.modelo}") },
                    onClick = {
                        onSelect(v.id)
                        expanded = false
                    }
                )
            }
            if (vehicles.isEmpty()) {
                DropdownMenuItem(text = { Text("No hay vehículos activos") }, onClick = { expanded = false })
            }
        }
    }
}

@Composable
private fun PhotoThumb(uri: String, onRemove: () -> Unit) {
    Box {
        UriImage(
            uriString = uri,
            contentDescription = "Foto",
            modifier = Modifier
                .size(96.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_delete),
                contentDescription = "Quitar",
                tint = Color.White
            )
        }
    }
}