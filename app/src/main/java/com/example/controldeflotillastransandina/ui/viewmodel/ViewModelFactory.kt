package com.example.controldeflotillastransandina.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.controldeflotillastransandina.core.AppContainer

object AppViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = AppContainer.repository
        val session = AppContainer.session
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(repository, session) as T
            modelClass.isAssignableFrom(VehicleViewModel::class.java) ->
                VehicleViewModel(repository) as T
            modelClass.isAssignableFrom(MaintenanceViewModel::class.java) ->
                MaintenanceViewModel(repository) as T
            modelClass.isAssignableFrom(MileageViewModel::class.java) ->
                MileageViewModel(repository) as T
            modelClass.isAssignableFrom(AlertsViewModel::class.java) ->
                AlertsViewModel(repository) as T
            modelClass.isAssignableFrom(FleetViewModel::class.java) ->
                FleetViewModel(repository) as T
            modelClass.isAssignableFrom(UsersViewModel::class.java) ->
                UsersViewModel(repository) as T
            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}