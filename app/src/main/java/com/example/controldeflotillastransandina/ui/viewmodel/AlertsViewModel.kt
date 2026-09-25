package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldeflotillastransandina.data.AppRepository
import com.example.controldeflotillastransandina.data.entity.AlertEntity
import kotlinx.coroutines.launch

class AlertsViewModel(private val repo: AppRepository) : ViewModel() {

    var alerts by mutableStateOf<List<AlertEntity>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set

    fun loadForFleet() {
        viewModelScope.launch {
            loading = true
            repo.getActiveAlerts().collect { alerts = it }
            loading = false
        }
    }

    fun loadForUser(userId: Long) {
        viewModelScope.launch {
            loading = true
            repo.getAlertsForUser(userId).collect { alerts = it }
            loading = false
        }
    }

    fun markAttended(id: Long) {
        viewModelScope.launch {
            repo.markAlertAttended(id)
            alerts = alerts.filterNot { it.id == id }
        }
    }

    fun markAllAttended() {
        viewModelScope.launch {
            repo.markAllAlertsAttended()
            alerts = emptyList()
        }
    }
}