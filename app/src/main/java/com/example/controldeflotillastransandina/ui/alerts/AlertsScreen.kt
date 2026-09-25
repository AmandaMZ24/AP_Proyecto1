package com.example.controldeflotillastransandina.ui.alerts

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.data.AlertType
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.AlertEntity
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.components.ScreenHeader
import com.example.controldeflotillastransandina.ui.viewmodel.AlertsViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory

@Composable
fun AlertsScreen(
    user: UserEntity,
    modifier: Modifier = Modifier,
    onOpenVehicle: (Long) -> Unit
) {
    val alertsVm: AlertsViewModel = viewModel(factory = AppViewModelFactory)
    val role = UserRole.valueOf(user.rol)
    val isManagement = role == UserRole.ENCARGADO_FLOTA || role == UserRole.ADMINISTRADOR

    LaunchedEffect(user.id, role) {
        if (isManagement) alertsVm.loadForFleet() else alertsVm.loadForUser(user.id)
    }

    LazyColumn(
        modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ScreenHeader("Centro de alertas", "Notificaciones de la flotilla.")
        }
        if (!isManagement) {
            item {
                TextButton(onClick = { alertsVm.markAllAttended() }) { Text("Marcar todas como atendidas") }
            }
        }

        if (alertsVm.loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (alertsVm.alerts.isEmpty()) {
            item { EmptyState("No hay alertas pendientes. Todo en orden.") }
        } else {
            items(alertsVm.alerts) { alert ->
                AlertCard(
                    alert = alert,
                    management = isManagement,
                    onOpen = {
                        alert.vehicleId?.let { onOpenVehicle(it) }
                    },
                    onAttend = { alertsVm.markAttended(alert.id) }
                )
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: AlertEntity,
    management: Boolean,
    onOpen: () -> Unit,
    onAttend: () -> Unit
) {
    val type = runCatching { AlertType.valueOf(alert.tipo) }.getOrDefault(AlertType.MANTENIMIENTO_PROXIMO)
    val color = when (type) {
        AlertType.MANTENIMIENTO_ATRASADO,
        AlertType.DOCUMENTO_VENCIDO -> Color(0xFFC62828)

        AlertType.MANTENIMIENTO_REGISTRADO,
        AlertType.VEHICULO_REASIGNADO -> Color(0xFF2E7D32)

        AlertType.MANTENIMIENTO_PROXIMO,
        AlertType.DOCUMENTO_PROXIMO -> Color(0xFFF9A825)
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(10.dp).background(color, CircleShape))
                Text(type.displayName, fontWeight = FontWeight.Bold)
            }
            Text(alert.mensaje, style = MaterialTheme.typography.bodyMedium)
            Text(
                java.time.Instant.ofEpochMilli(alert.fecha).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (management && alert.vehicleId != null) {
                    TextButton(onClick = onOpen) { Text("Ver vehículo") }
                }
                TextButton(onClick = onAttend) { Text("Marcar atendida") }
            }
        }
    }
}