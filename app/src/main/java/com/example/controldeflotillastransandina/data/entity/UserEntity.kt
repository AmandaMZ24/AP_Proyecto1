package com.example.controldeflotillastransandina.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["cedula"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val cedula: String,
    val email: String,
    val telefono: String,
    val licencia: String?,
    val rol: String,
    val passwordHash: String,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)