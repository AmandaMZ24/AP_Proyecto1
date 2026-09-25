@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.InfoRow
import com.example.controldeflotillastransandina.ui.components.UriImage
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.MaintenanceViewModel
import com.example.controldeflotillastransandina.util.formatEpochDay

@Composable
fun MaintenanceDetailScreen(
    maintenanceId: Long,
    maintenanceVm: MaintenanceViewModel = viewModel(factory = AppViewModelFactory),
    currentUser: UserEntity? = null,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit
) {
    val m = maintenanceVm.selected
    val images = maintenanceVm.images
    var confirmDelete by remember { mutableStateOf(false) }
    val isManagement = currentUser?.let {
        it.rol == UserRole.ENCARGADO_FLOTA.name || it.rol == UserRole.ADMINISTRADOR.name
    } == true

    LaunchedEffect(maintenanceId) {
        maintenanceVm.clearFlags()
        maintenanceVm.loadMaintenance(maintenanceId)
    }

    LaunchedEffect(maintenanceVm.deleted) {
        if (maintenanceVm.deleted) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isManagement) {
                        IconButton(onClick = onEdit) {
                            Icon(painterResource(R.drawable.ic_edit), contentDescription = "Editar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        if (m == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            InfoRow("Tipo", m.tipo)
                            InfoRow("Categoría", m.categoria)
                            InfoRow("Fecha", m.fecha.formatEpochDay())
                            InfoRow("Taller", m.taller)
                            InfoRow("Kilometraje", "${m.kilometraje.toLong()} km")
                            InfoRow("Costo", "₡${m.costo.toInt()}")
                            Text(
                                "Descripción",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            Text(m.descripcion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (images.isNotEmpty()) {
                    item {
                        Text("Evidencia fotográfica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(images) { img ->
                                UriImage(
                                    uriString = img.uri,
                                    contentDescription = "Foto",
                                    modifier = Modifier.size(140.dp, 110.dp),
                                    emptyLabel = ""
                                )
                            }
                        }
                    }
                }
                if (isManagement) {
                    item {
                        Button(
                            onClick = { confirmDelete = true },
                            Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(painterResource(R.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Eliminar ${" "}registro", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar mantenimiento") },
            text = { Text("Esta acción no se puede deshacer. ¿Desea eliminar este registro?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    maintenanceVm.deleteMaintenance(maintenanceId)
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}