@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.controldeflotillastransandina.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.controldeflotillastransandina.R
import com.example.controldeflotillastransandina.data.UserRole
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.ui.components.EmptyState
import com.example.controldeflotillastransandina.ui.components.ScreenHeader
import com.example.controldeflotillastransandina.ui.viewmodel.AppViewModelFactory
import com.example.controldeflotillastransandina.ui.viewmodel.UsersViewModel

@Composable
fun UsersScreen(
    currentUserId: Long,
    onBack: () -> Unit
) {
    val usersVm: UsersViewModel = viewModel(factory = AppViewModelFactory)

    LaunchedEffect(Unit) {
        usersVm.loadAll()
    }

    val users = usersVm.users.sortedBy { it.nombre }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de usuarios") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ScreenHeader("Usuarios", "Active o suspenda el acceso de los colaboradores.")
            }
            if (users.isEmpty()) {
                item { EmptyState("No hay usuarios registrados.") }
            } else {
                items(users) { u ->
                    UserRow(
                        user = u,
                        isSelf = u.id == currentUserId,
                        onToggle = { usersVm.setActive(u.id, !u.activo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: UserEntity, isSelf: Boolean, onToggle: () -> Unit) {
    val roleName = runCatching { UserRole.valueOf(user.rol).displayName }.getOrDefault(user.rol)
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(user.nombre, fontWeight = FontWeight.Bold)
                Text(roleName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isSelf) {
                    Text("Este perfil (no se puede suspender)", style = MaterialTheme.typography.bodySmall)
                }
            }
            Switch(
                checked = user.activo,
                onCheckedChange = { if (!isSelf) onToggle() },
                enabled = !isSelf
            )
        }
    }
}