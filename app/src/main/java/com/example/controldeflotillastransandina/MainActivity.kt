package com.example.controldeflotillastransandina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.controldeflotillastransandina.ui.theme.ControlDeFlotillasTransAndinaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlDeFlotillasTransAndinaTheme {
                TransAndinaApp()
            }
        }
    }
}

enum class UserRole(val displayName: String) {
    ADMINISTRADOR("Administrador"),
    ENCARGADO_FLOTA("Encargado de flota"),
    CONDUCTOR("Conductor"),
    MECANICO("Mecánico")
}

enum class AppDestination(val label: String, val icon: Int) {
    INICIO("Inicio", R.drawable.ic_home),
    VEHICULOS("Vehículos", R.drawable.ic_favorite),
    MANTENIMIENTOS("Mantenimientos", R.drawable.ic_favorite),
    ALERTAS("Alertas", R.drawable.ic_favorite),
    PERFIL("Perfil", R.drawable.ic_account_box)
}

data class FleetVehicle(
    val plate: String,
    val description: String,
    val mileage: String,
    val status: String,
)

private val demoVehicles = listOf(
    FleetVehicle("TRA-101", "Isuzu NPR 2022 · Pesado", "128 420 km", "Próximo"),
    FleetVehicle("TRA-205", "Toyota Hilux 2023 · Liviano", "64 180 km", "Al día"),
    FleetVehicle("TRA-310", "Hino 500 2021 · Pesado", "194 650 km", "Atrasado"),
)

@Composable
fun TransAndinaApp() {
    var signedIn by rememberSaveable { mutableStateOf(false) }
    var roleName by rememberSaveable { mutableStateOf(UserRole.ENCARGADO_FLOTA.name) }

    if (!signedIn) {
        LoginScreen(
            onLogin = { selectedRole ->
                roleName = selectedRole.name
                signedIn = true
            },
        )
    } else {
        FleetShell(
            role = UserRole.valueOf(roleName),
            onSignOut = { signedIn = false },
        )
    }
}

@Composable
private fun LoginScreen(onLogin: (UserRole) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf(UserRole.ENCARGADO_FLOTA) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("TRANSANDINA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Gestión de flotilla", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Registra, previene y mantén cada vehículo disponible.", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(28.dp))
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Correo electrónico") }, singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Contraseña") }, singleLine = true)
            Spacer(Modifier.height(18.dp))
            Text("Rol para la demostración", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(UserRole.ADMINISTRADOR, UserRole.ENCARGADO_FLOTA).forEach { role ->
                    FilterChip(selectedRole == role, { selectedRole = role }, label = { Text(role.displayName) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(UserRole.CONDUCTOR, UserRole.MECANICO).forEach { role ->
                    FilterChip(selectedRole == role, { selectedRole = role }, label = { Text(role.displayName) })
                }
            }
            Spacer(Modifier.height(22.dp))
            Button(onClick = { onLogin(selectedRole) }, Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
            TextButton(onClick = {}) { Text("¿Olvidaste tu contraseña?") }
        }
    }
}

@Composable
private fun FleetShell(role: UserRole, onSignOut: () -> Unit) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.INICIO) }
    val destinations = destinationsFor(role)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            destinations.forEach { destination ->
                item(
                    icon = { Icon(painterResource(destination.icon), destination.label, Modifier.size(22.dp)) },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination },
                )
            }
        },
    ) {
        Scaffold { paddingValues ->
            when (currentDestination) {
                AppDestination.INICIO -> DashboardScreen(role, Modifier.padding(paddingValues))
                AppDestination.VEHICULOS -> VehiclesScreen(Modifier.padding(paddingValues))
                AppDestination.MANTENIMIENTOS -> MaintenanceScreen(Modifier.padding(paddingValues))
                AppDestination.ALERTAS -> AlertsScreen(Modifier.padding(paddingValues))
                AppDestination.PERFIL -> ProfileScreen(role, onSignOut, Modifier.padding(paddingValues))
            }
        }
    }
}

private fun destinationsFor(role: UserRole): List<AppDestination> = when (role) {
    UserRole.ADMINISTRADOR -> listOf(AppDestination.INICIO, AppDestination.VEHICULOS, AppDestination.ALERTAS, AppDestination.PERFIL)
    UserRole.ENCARGADO_FLOTA -> AppDestination.entries.toList()
    UserRole.CONDUCTOR -> listOf(AppDestination.INICIO, AppDestination.MANTENIMIENTOS, AppDestination.ALERTAS, AppDestination.PERFIL)
    UserRole.MECANICO -> listOf(AppDestination.INICIO, AppDestination.MANTENIMIENTOS, AppDestination.PERFIL)
}

@Composable
private fun DashboardScreen(role: UserRole, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Buenos días", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Centro de control", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(role.displayName, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Vehículos", "12", Modifier.weight(1f))
                MetricCard("Por atender", "3", Modifier.weight(1f))
            }
        }
        item { Text("Estado de la flotilla", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(demoVehicles.take(if (role == UserRole.CONDUCTOR) 1 else 3)) { vehicle -> VehicleCard(vehicle) }
    }
}

@Composable
private fun VehiclesScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Vehículos", "Consulta la ficha y el estado de cada unidad.") }
        item { OutlinedButton(onClick = {}, Modifier.fillMaxWidth()) { Text("+ Registrar vehículo") } }
        items(demoVehicles) { vehicle -> VehicleCard(vehicle) }
    }
}

@Composable
private fun MaintenanceScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Mantenimientos", "Historial preventivo y correctivo de la flotilla.") }
        item { Button(onClick = {}, Modifier.fillMaxWidth()) { Text("Registrar mantenimiento") } }
        item { MaintenanceRow("TRA-101", "Cambio de aceite", "Preventivo · 12 sep 2026", "₡85 000") }
        item { MaintenanceRow("TRA-310", "Sistema de frenos", "Correctivo · 08 sep 2026", "₡240 000") }
    }
}

@Composable
private fun AlertsScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Centro de alertas", "Atiende primero los eventos más urgentes.") }
        item { AlertRow("Mantenimiento atrasado", "TRA-310 requiere revisión de frenos.") }
        item { AlertRow("Documento próximo a vencer", "El seguro de TRA-101 vence en 12 días.") }
        item { AlertRow("Kilometraje actualizado", "TRA-205 registró 64 180 km.") }
    }
}

@Composable
private fun ProfileScreen(role: UserRole, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenHeader("Mi perfil", "Datos de la cuenta activa.")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Usuario de demostración", fontWeight = FontWeight.Bold)
                Text("usuario@transandina.com")
                Text("Rol: ${role.displayName}", color = MaterialTheme.colorScheme.primary)
            }
        }
        OutlinedButton(onClick = onSignOut, Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(subtitle, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(16.dp)) { Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text(title) } }
}

@Composable
private fun VehicleCard(vehicle: FleetVehicle) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(vehicle.plate, fontWeight = FontWeight.Bold)
                Text(vehicle.status, color = MaterialTheme.colorScheme.primary)
            }
            Text(vehicle.description)
            HorizontalDivider()
            Text("Odómetro: ${vehicle.mileage}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MaintenanceRow(vehicle: String, service: String, detail: String, cost: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(vehicle, fontWeight = FontWeight.Bold); Text(cost) }
            Text(service)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AlertRow(title: String, detail: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(detail) }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    ControlDeFlotillasTransAndinaTheme { LoginScreen(onLogin = {}) }
}