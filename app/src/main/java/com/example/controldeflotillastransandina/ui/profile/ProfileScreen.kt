package com.example.controldeflotillastransandina.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.AppErrorMessage
import com.example.controldeflotillastransandina.ui.components.AppSuccessMessage
import com.example.controldeflotillastransandina.ui.components.InfoRow
import com.example.controldeflotillastransandina.ui.components.PasswordField
import com.example.controldeflotillastransandina.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    user: UserEntity,
    authVm: AuthViewModel,
    modifier: Modifier = Modifier,
    onUsers: () -> Unit,
    onFleet: () -> Unit,
    onReports: () -> Unit
) {
    var editMode by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }

    val role = UserRole.valueOf(user.rol)
    val isManagement = role == UserRole.ENCARGADO_FLOTA || role == UserRole.ADMINISTRADOR

    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        authVm.error?.let { AppErrorMessage(it) }
        authVm.success?.let { AppSuccessMessage(it) }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (editMode) {
                    EditProfileFields(authVm, user) {
                        editMode = false
                    }
                } else {
                    Text(user.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(role.displayName, color = MaterialTheme.colorScheme.primary)
                    InfoRow("Cédula", user.cedula)
                    InfoRow("Correo", user.email)
                    InfoRow("Teléfono", user.telefono)
                    user.licencia?.let { InfoRow("Licencia", it) }
                    TextButton(onClick = { editMode = true }) { Text("Editar datos") }
                }
            }
        }

        Button(onClick = { showPasswordDialog = true }, Modifier.fillMaxWidth()) {
            Text("Cambiar contraseña")
        }

        if (isManagement) {
            Text("Panel de administración", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = onUsers, Modifier.fillMaxWidth()) {
                Text("Gestión de usuarios")
            }
            Button(onClick = onFleet, Modifier.fillMaxWidth()) {
                Text("Flotilla y estado")
            }
            Button(onClick = onReports, Modifier.fillMaxWidth()) {
                Text("Reportes de costos")
            }
        }

        Button(
            onClick = { authVm.signOut() },
            Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) { Text("Cerrar sesión") }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(authVm) { showPasswordDialog = false }
    }
}

@Composable
private fun EditProfileFields(authVm: AuthViewModel, user: UserEntity, onDone: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf(user.nombre) }
    var telefono by rememberSaveable { mutableStateOf(user.telefono) }
    var localError by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        localError?.let { AppErrorMessage(it) }
        OutlinedTextField(nombre, { nombre = it }, Modifier.fillMaxWidth(), label = { Text("Nombre completo") })
        OutlinedTextField(telefono, { telefono = it }, Modifier.fillMaxWidth(), label = { Text("Teléfono") })
        Button(
            onClick = {
                localError = when {
                    nombre.isBlank() -> "El nombre no puede estar vacío."
                    telefono.isBlank() -> "El teléfono no puede estar vacío."
                    else -> null
                }
                if (localError == null) {
                    authVm.updateProfile(user.copy(nombre = nombre.trim(), telefono = telefono.trim()))
                    onDone()
                }
            },
            Modifier.fillMaxWidth()
        ) { Text("Guardar cambios") }
        TextButton(onClick = onDone, Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}

@Composable
private fun ChangePasswordDialog(authVm: AuthViewModel, onDismiss: () -> Unit) {
    var current by rememberSaveable { mutableStateOf("") }
    var newPass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                localError?.let { AppErrorMessage(it) }
                authVm.error?.let { AppErrorMessage(it) }
                PasswordField(current, { current = it }, "Contraseña actual")
                PasswordField(newPass, { newPass = it }, "Nueva contraseña")
                PasswordField(confirm, { confirm = it }, "Confirmar nueva contraseña")
            }
        },
        confirmButton = {
            TextButton(onClick = {
                localError = when {
                    current.isBlank() -> "Ingrese la contraseña actual."
                    newPass.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                    newPass != confirm -> "Las contraseñas nuevas no coinciden."
                    else -> null
                }
                if (localError == null) {
                    authVm.changePassword(current, newPass)
                    onDismiss()
                }
            }) { Text("Cambiar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}