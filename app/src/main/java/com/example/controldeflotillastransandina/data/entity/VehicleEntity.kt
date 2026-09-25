package com.example.controldeflotillastransandina.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    indices = [Index(value = ["placa"], unique = true)]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val tipo: String,
    val capacidad: String,
    val kilometrajeActual: Double,
    val fechaMarchamo: Long?,
    val fechaRevision: Long?,
    val fechaSeguro: Long?,
    val conductorId: Long?,
    val activo: Boolean = true,
    val frecuenciaKm: Int = 10000,
    val frecuenciaDias: Int = 90
)