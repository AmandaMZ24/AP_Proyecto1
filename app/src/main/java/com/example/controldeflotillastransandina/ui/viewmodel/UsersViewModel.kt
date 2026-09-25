package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldeflotillastransandina.data.AppRepository
import com.example.controldeflotillastransandina.data.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UsersViewModel(private val repo: AppRepository) : ViewModel() {

    var users by mutableStateOf<List<UserEntity>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set

    fun loadAll() {
        viewModelScope.launch {
            loading = true
            repo.getAllUsers().collect { users = it }
            loading = false
        }
    }

    fun setActive(id: Long, activo: Boolean) {
        viewModelScope.launch {
            repo.setUserActive(id, activo)
            users = users.map { if (it.id == id) it.copy(activo = activo) else it }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            users = repo.getAllUsers().first()
        }
    }
}