package com.example.controldeflotillastransandina.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "odometer_records",
    indices = [Index(value = ["vehicleId"])]
)
data class OdometerRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val fecha: Long,
    val kilometraje: Double
)