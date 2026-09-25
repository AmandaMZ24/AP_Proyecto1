package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.controldeflotillastransandina.data.AppRepository
import com.example.controldeflotillastransandina.data.AppResult
import com.example.controldeflotillastransandina.data.entity.OdometerRecordEntity
import kotlinx.coroutines.launch

class MileageViewModel(private val repo: AppRepository) : ViewModel() {

    var history by mutableStateOf<List<OdometerRecordEntity>>(emptyList())
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var saved by mutableStateOf(false)

    fun load(vehicleId: Long) {
        viewModelScope.launch {
            repo.getOdometerHistory(vehicleId).collect { history = it }
        }
    }

    fun register(vehicleId: Long, fecha: Long, kilometraje: Double) {
        viewModelScope.launch {
            busy = true
            error = null
            when (val result = repo.registerOdometer(vehicleId, fecha, kilometraje)) {
                is AppResult.Success -> saved = true
                is AppResult.Error -> error = result.message
            }
            busy = false
        }
    }

    fun clearFlags() {
        error = null
        saved = false
    }
}