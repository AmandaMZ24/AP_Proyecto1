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
import com.example.controldeflotillastransandina.ui.components.AppErrorMessage
import com.example.controldeflotillastransandina.ui.components.AppSuccessMessage
import com.example.controldeflotillastransandina.ui.components.PasswordField

@Composable
fun RecoverPasswordScreen(
    errorMessage: String?,
    successMessage: String?,
    loading: Boolean,
    onReset: (email: String, cedula: String, newPassword: String) -> Unit,
    onBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var cedula by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
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
                "Restablecer acceso",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Verificaremos tu identidad con el correo y la cédula de la cuenta para poder asignar una contraseña nueva.",
                style = MaterialTheme.typography.bodyMedium
            )

            localError?.let { AppErrorMessage(it) }
            errorMessage?.let { AppErrorMessage(it) }
            successMessage?.let { AppSuccessMessage(it) }

            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Correo electrónico") }, singleLine = true)
            OutlinedTextField(cedula, { cedula = it.filter { c -> c.isDigit() }.take(12) }, Modifier.fillMaxWidth(), label = { Text("Cédula") }, singleLine = true)
            PasswordField(password, { password = it }, "Contraseña nueva")
            PasswordField(confirm, { confirm = it }, "Confirmar contraseña nueva")

            Button(
                onClick = {
                    localError = when {
                        email.isBlank() -> "Ingrese su correo electrónico."
                        cedula.isBlank() -> "Ingrese su cédula."
                        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                        password != confirm -> "Las contraseñas no coinciden."
                        else -> null
                    }
                    if (localError == null) onReset(email, cedula, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text(if (loading) "Procesando…" else "Restablecer contraseña")
            }
        }
    }
}