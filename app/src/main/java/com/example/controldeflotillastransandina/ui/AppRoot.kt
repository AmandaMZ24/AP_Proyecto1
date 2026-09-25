@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.alerts.AlertsScreen
import com.example.controldeflotillastransandina.ui.auth.LoginScreen
import com.example.controldeflotillastransandina.ui.auth.RecoverPasswordScreen
import com.example.controldeflotillastransandina.ui.auth.RegisterScreen
import com.example.controldeflotillastransandina.ui.components.CenteredLoading
import com.example.controldeflotillastransandina.ui.dashboard.DashboardScreen
import com.example.controldeflotillastransandina.ui.fleet.FleetScreen
import com.example.controldeflotillastransandina.ui.reports.ReportsScreen
import com.example.controldeflotillastransandina.ui.maintenance.MaintenanceDetailScreen
import com.example.controldeflotillastransandina.ui.maintenance.MaintenanceFormScreen
import com.example.controldeflotillastransandina.ui.maintenance.MaintenanceScreen
import com.example.controldeflotillastransandina.ui.mileage.MileageScreen
import com.example.controldeflotillastransandina.ui.profile.ProfileScreen
import com.example.controldeflotillastransandina.ui.users.UsersScreen
import com.example.controldeflotillastransandina.ui.vehicles.VehicleDetailScreen
import com.example.controldeflotillastransandina.ui.vehicles.VehicleFormScreen
import com.example.controldeflotillastransandina.ui.vehicles.VehiclesScreen
import com.example.controldeflotillastransandina.ui.viewmodel.AlertsViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.AuthViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.FleetViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.MaintenanceViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.MileageViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.UsersViewModel
import com.example.controldeflotillastransandina.ui.viewmodel.VehicleViewModel

enum class AppDestination(val label: String, val icon: Int) {
    INICIO("Inicio", R.drawable.ic_home),
    VEHICULOS("Vehículos", R.drawable.ic_car),
    MANTENIMIENTOS("Mantenimientos", R.drawable.ic_wrench),
    ALERTAS("Alertas", R.drawable.ic_bell),
    PERFIL("Perfil", R.drawable.ic_account_box)
}

sealed class MainRoute {
    val key: Any get() = when (this) {
        Tabs -> "tabs"
        is VehicleDetail -> "vehicledetail:$vehicleId"
        is VehicleForm -> "vehicleform:$vehicleId"
        is MaintenanceDetail -> "maintenancedetail:$maintenanceId"
        is MaintenanceForm -> "maintenanceform:$vehicleId:$maintenanceId"
        is Mileage -> "mileage:$vehicleId"
        Fleet -> "fleet"
        Users -> "users"
        Reports -> "reports"
    }

    data object Tabs : MainRoute()
    data class VehicleDetail(val vehicleId: Long) : MainRoute()
    data class VehicleForm(val vehicleId: Long?) : MainRoute()
    data class MaintenanceDetail(val maintenanceId: Long) : MainRoute()
    data class MaintenanceForm(val vehicleId: Long?, val maintenanceId: Long?) : MainRoute()
    data class Mileage(val vehicleId: Long) : MainRoute()
    data object Fleet : MainRoute()
    data object Users : MainRoute()
    data object Reports : MainRoute()
}

@Composable
fun TransAndinaApp() {
    val authVm: AuthViewModel = viewModel(factory = AppViewModelFactory)
    val user by authVm.currentUser.collectAsState()

    when {
        !authVm.ready -> CenteredLoading(Modifier.fillMaxSize())
        user == null -> AuthFlow(authVm)
        else -> MainNavigation(user = user!!, authVm = authVm)
    }
}

private enum class AuthScreen { LOGIN, REGISTER, RECOVER }

@Composable
private fun AuthFlow(authVm: AuthViewModel) {
    var screen by rememberSaveable { mutableStateOf(AuthScreen.LOGIN) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = screen != AuthScreen.LOGIN) {
        screen = AuthScreen.LOGIN
        authVm.clearMessages()
    }

    when (screen) {
        AuthScreen.LOGIN -> LoginScreen(
            email = email,
            password = password,
            errorMessage = authVm.error,
            loading = authVm.loading,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onLogin = {
                authVm.clearMessages()
                authVm.login(email, password)
            },
            onRegister = { authVm.clearMessages(); screen = AuthScreen.REGISTER },
            onRecover = { authVm.clearMessages(); screen = AuthScreen.RECOVER }
        )

        AuthScreen.REGISTER -> RegisterScreen(
            errorMessage = authVm.error,
            loading = authVm.loading,
            onRegister = { n, c, e, t, l, r, p ->
                authVm.clearMessages()
                authVm.register(n, c, e, t, l, r, p)
            },
            onBack = { authVm.clearMessages(); screen = AuthScreen.LOGIN }
        )

        AuthScreen.RECOVER -> RecoverPasswordScreen(
            errorMessage = authVm.error,
            successMessage = authVm.success,
            loading = authVm.loading,
            onReset = { e, c, p ->
                authVm.clearMessages()
                authVm.resetPassword(e, c, p)
            },
            onBack = { authVm.clearMessages(); screen = AuthScreen.LOGIN }
        )
    }
}

@Composable
private fun MainNavigation(user: UserEntity, authVm: AuthViewModel) {
    val vehicleVm: VehicleViewModel = viewModel(factory = AppViewModelFactory)
    val maintenanceVm: MaintenanceViewModel = viewModel(factory = AppViewModelFactory)
    val mileageVm: MileageViewModel = viewModel(factory = AppViewModelFactory)
    val alertsVm: AlertsViewModel = viewModel(factory = AppViewModelFactory)
    val fleetVm: FleetViewModel = viewModel(factory = AppViewModelFactory)
    val usersVm: UsersViewModel = viewModel(factory = AppViewModelFactory)

    var stack by remember { mutableStateOf(listOf<MainRoute>(MainRoute.Tabs)) }
    val current = stack.last()
    val saveableStateHolder = rememberSaveableStateHolder()

    BackHandler(enabled = stack.size > 1) {
        stack = stack.dropLast(1)
    }

    fun push(route: MainRoute) {
        stack = stack + route
    }

    when (val route = current) {
        MainRoute.Tabs -> saveableStateHolder.SaveableStateProvider(MainRoute.Tabs.key) {
            Shell(
                user = user,
                authVm = authVm,
                onNavigate = ::push
            )
        }

        is MainRoute.VehicleDetail -> saveableStateHolder.SaveableStateProvider(route.key) {
            VehicleDetailScreen(
                vehicleId = route.vehicleId,
                vehicleVm = vehicleVm,
                maintenanceVm = maintenanceVm,
                mileageVm = mileageVm,
                currentUser = user,
                onBack = { stack = stack.dropLast(1) },
                onEdit = { push(MainRoute.VehicleForm(route.vehicleId)) },
                onNewMaintenance = { push(MainRoute.MaintenanceForm(route.vehicleId, null)) },
                onRegisterMileage = { push(MainRoute.Mileage(route.vehicleId)) },
                onOpenMaintenance = { push(MainRoute.MaintenanceDetail(it)) }
            )
        }

        is MainRoute.VehicleForm -> saveableStateHolder.SaveableStateProvider(route.key) {
            VehicleFormScreen(
                vehicleId = route.vehicleId,
                vehicleVm = vehicleVm,
                onBack = { stack = stack.dropLast(1) }
            )
        }

        is MainRoute.MaintenanceDetail -> saveableStateHolder.SaveableStateProvider(route.key) {
            MaintenanceDetailScreen(
                maintenanceId = route.maintenanceId,
                maintenanceVm = maintenanceVm,
                currentUser = user,
                onBack = { stack = stack.dropLast(1) },
                onEdit = { push(MainRoute.MaintenanceForm(null, route.maintenanceId)) },
                onDeleted = { stack = stack.dropLast(1) }
            )
        }

        is MainRoute.MaintenanceForm -> saveableStateHolder.SaveableStateProvider(route.key) {
            MaintenanceFormScreen(
                vehicleId = route.vehicleId,
                maintenanceId = route.maintenanceId,
                vehicleVm = vehicleVm,
                maintenanceVm = maintenanceVm,
                currentUser = user,
                onBack = { stack = stack.dropLast(1) }
            )
        }

        is MainRoute.Mileage -> saveableStateHolder.SaveableStateProvider(route.key) {
            MileageScreen(
                vehicleId = route.vehicleId,
                vehicleVm = vehicleVm,
                mileageVm = mileageVm,
                onBack = { stack = stack.dropLast(1) }
            )
        }

        MainRoute.Fleet -> saveableStateHolder.SaveableStateProvider(MainRoute.Fleet.key) {
            FleetScreen(
                onBack = { stack = stack.dropLast(1) },
                onOpenVehicle = { push(MainRoute.VehicleDetail(it)) }
            )
        }

        MainRoute.Users -> saveableStateHolder.SaveableStateProvider(MainRoute.Users.key) {
            UsersScreen(
                currentUserId = user.id,
                onBack = { stack = stack.dropLast(1) }
            )
        }

        MainRoute.Reports -> saveableStateHolder.SaveableStateProvider(MainRoute.Reports.key) {
            ReportsScreen(
                onBack = { stack = stack.dropLast(1) },
                onOpenDetail = { push(MainRoute.MaintenanceDetail(it)) }
            )
        }
    }
}

@Composable
private fun Shell(
    user: UserEntity,
    authVm: AuthViewModel,
    onNavigate: (MainRoute) -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.INICIO) }
    val destinations = destinationsFor(user.rol)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            destinations.forEach { destination ->
                item(
                    icon = { Icon(painterResource(destination.icon), destination.label, Modifier.size(22.dp)) },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Scaffold { paddingValues ->
            val contentModifier = Modifier.padding(paddingValues)
            when (currentDestination) {
AppDestination.INICIO -> DashboardScreen(
                            user = user,
                            modifier = contentModifier,
                            onOpenVehicle = { onNavigate(MainRoute.VehicleDetail(it)) },
                            onOpenFleet = { onNavigate(MainRoute.Fleet) },
                            onNewMaintenance = { vehicleId -> onNavigate(MainRoute.MaintenanceForm(vehicleId, null)) }
                        )

                        AppDestination.VEHICULOS -> VehiclesScreen(
                            user = user,
                            modifier = contentModifier,
                            onOpenVehicle = { onNavigate(MainRoute.VehicleDetail(it)) },
                            onNewVehicle = { onNavigate(MainRoute.VehicleForm(null)) },
                            onNewMaintenance = { onNavigate(MainRoute.MaintenanceForm(it, null)) },
                            onRegisterMileage = { onNavigate(MainRoute.Mileage(it)) }
                        )

                        AppDestination.MANTENIMIENTOS -> MaintenanceScreen(
                            user = user,
                            modifier = contentModifier,
                            onOpenDetail = { onNavigate(MainRoute.MaintenanceDetail(it)) },
                            onNewMaintenance = { onNavigate(MainRoute.MaintenanceForm(null, null)) }
                        )

                AppDestination.ALERTAS -> AlertsScreen(
                    user = user,
                    modifier = contentModifier,
                    onOpenVehicle = { onNavigate(MainRoute.VehicleDetail(it)) }
                )

                AppDestination.PERFIL -> ProfileScreen(
                    user = user,
                    authVm = authVm,
                    modifier = contentModifier,
                    onUsers = { onNavigate(MainRoute.Users) },
                    onFleet = { onNavigate(MainRoute.Fleet) },
                    onReports = { onNavigate(MainRoute.Reports) }
                )
            }
        }
    }
}

private fun destinationsFor(roleName: String): List<AppDestination> = when (
    runCatching { UserRole.valueOf(roleName) }.getOrDefault(UserRole.CONDUCTOR)
) {
    UserRole.ADMINISTRADOR, UserRole.ENCARGADO_FLOTA, UserRole.CONDUCTOR ->
        AppDestination.entries.toList()

    UserRole.MECANICO ->
        listOf(
            AppDestination.INICIO,
            AppDestination.VEHICULOS,
            AppDestination.MANTENIMIENTOS,
            AppDestination.PERFIL
        )
}