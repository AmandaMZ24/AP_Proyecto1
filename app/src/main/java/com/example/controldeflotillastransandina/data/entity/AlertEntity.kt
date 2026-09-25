package com.example.controldeflotillastransandina.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alerts",
    indices = [Index(value = ["vehicleId"]), Index(value = ["userId"])]
)
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long?,
    val userId: Long?,
    val tipo: String,
    val mensaje: String,
    val prioridad: Int,
    val fecha: Long = System.currentTimeMillis(),
    val atendida: Boolean = false
)