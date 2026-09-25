@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.ui.components.AppErrorMessage
import com.example.controldeflotillastransandina.ui.components.ChipSelector
import com.example.controldeflotillastransandina.ui.components.PasswordField

@Composable
fun RegisterScreen(
    errorMessage: String?,
    loading: Boolean,
    onRegister: (nombre: String, cedula: String, email: String, telefono: String, licencia: String?, rol: String, password: String) -> Unit,
    onBack: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var cedula by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var licencia by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf(UserRole.CONDUCTOR) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val roles = UserRole.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            painterResource(com.example.controldeflotillastransandina.R.drawable.ic_arrow_back),
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Registra tus datos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Crea tu cuenta de conductor, mecánico o encargado de flota.",
                style = MaterialTheme.typography.bodyMedium
            )

            localError?.let {
                AppErrorMessage(it)
            }
            errorMessage?.let {
                AppErrorMessage(it)
            }

            OutlinedTextField(nombre, { nombre = it }, Modifier.fillMaxWidth(), label = { Text("Nombre completo") }, singleLine = true)
            OutlinedTextField(cedula, { cedula = it.filter { c -> c.isDigit() }.take(12) }, Modifier.fillMaxWidth(), label = { Text("Cédula") }, singleLine = true)
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Correo electrónico") }, singleLine = true)
            OutlinedTextField(telefono, { telefono = it }, Modifier.fillMaxWidth(), label = { Text("Teléfono") }, singleLine = true)

            Text("Rol", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            ChipSelector(roles, rol, { it.displayName }, { rol = it })

            if (rol == UserRole.CONDUCTOR) {
                OutlinedTextField(licencia, { licencia = it }, Modifier.fillMaxWidth(), label = { Text("Número de licencia de conducir") }, singleLine = true)
            }

            PasswordField(password, { password = it }, "Contraseña")
            PasswordField(confirm, { confirm = it }, "Confirmar contraseña")

            Button(
                onClick = {
                    localError = when {
                        nombre.isBlank() -> "El nombre es obligatorio."
                        cedula.isBlank() || cedula.length < 7 -> "Ingrese una cédula válida (solo dígitos)."
                        email.isBlank() -> "El correo es obligatorio."
                        telefono.isBlank() -> "El teléfono es obligatorio."
                        rol == UserRole.CONDUCTOR && licencia.isBlank() -> "La licencia es obligatoria para conductores."
                        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                        password != confirm -> "Las contraseñas no coinciden."
                        else -> null
                    }
                    if (localError == null) {
                        onRegister(nombre, cedula, email, telefono, licencia.ifBlank { null }, rol.name, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text(if (loading) "Creando cuenta…" else "Registrarme")
            }
        }
    }
}