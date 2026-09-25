package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldeflotillastransandina.core.SessionManager
import com.example.controldeflotillastransandina.data.AppRepository
import com.example.controldeflotillastransandina.data.AppResult
import com.example.controldeflotillastransandina.data.entity.UserEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AppRepository,
    private val session: SessionManager
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = session.currentUserId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repo.getUser(id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var success by mutableStateOf<String?>(null)
        private set
    var ready by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repo.seedDefaultsIfEmpty()
            session.currentUserId.first()
            ready = true
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loading = true
            error = null
            when (val result = repo.login(email, password)) {
                is AppResult.Success -> session.setCurrentUser(result.data.id)
                is AppResult.Error -> error = result.message
            }
            loading = false
        }
    }

    fun register(
        nombre: String,
        cedula: String,
        email: String,
        telefono: String,
        licencia: String?,
        rol: String,
        password: String
    ) {
        viewModelScope.launch {
            loading = true
            error = null
            when (val result = repo.registerUser(nombre, cedula, email, telefono, licencia, rol, password)) {
                is AppResult.Success -> {
                    session.setCurrentUser(result.data.id)
                    success = "Cuenta creada correctamente."
                }
                is AppResult.Error -> error = result.message
            }
            loading = false
        }
    }

    fun resetPassword(email: String, cedula: String, newPassword: String) {
        viewModelScope.launch {
            loading = true
            error = null
            when (val result = repo.resetPassword(email, cedula, newPassword)) {
                is AppResult.Success -> success = "Contraseña actualizada. Ya puede iniciar sesión."
                is AppResult.Error -> error = result.message
            }
            loading = false
        }
    }

    fun updateProfile(user: UserEntity) {
        viewModelScope.launch {
            error = null
            when (val result = repo.updateProfile(user)) {
                is AppResult.Success -> success = "Datos actualizados."
                is AppResult.Error -> error = result.message
            }
        }
    }

    fun changePassword(current: String, newPassword: String) {
        currentUser.value?.let { user ->
            viewModelScope.launch {
                error = null
                when (val result = repo.changePassword(user.id, current, newPassword)) {
                    is AppResult.Success -> success = "Contraseña cambiada."
                    is AppResult.Error -> error = result.message
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { session.setCurrentUser(null) }
    }

    fun clearMessages() {
        error = null
        success = null
    }
}