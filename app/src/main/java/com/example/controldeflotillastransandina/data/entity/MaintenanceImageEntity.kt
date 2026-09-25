package com.example.controldeflotillastransandina.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_images",
    indices = [Index(value = ["maintenanceId"])]
)
data class MaintenanceImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val maintenanceId: Long,
    val uri: String
)