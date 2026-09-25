package com.example.controldeflotillastransandina.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenances",
    indices = [Index(value = ["vehicleId"]), Index(value = ["userId"])]
)
data class MaintenanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val userId: Long,
    val tipo: String,
    val categoria: String,
    val fecha: Long,
    val taller: String,
    val descripcion: String,
    val kilometraje: Double,
    val costo: Double
)