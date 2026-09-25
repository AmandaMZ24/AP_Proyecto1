package com.example.controldeflotillastransandina.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.controldeflotillastransandina.data.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Delete
    suspend fun delete(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles ORDER BY placa")
    fun getAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles")
    suspend fun getAllOnce(): List<VehicleEntity>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun getById(id: Long): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getByIdOnce(id: Long): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE conductorId = :driverId AND activo = 1")
    fun getByDriver(driverId: Long): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE conductorId = :driverId AND activo = 1")
    suspend fun getByDriverOnce(driverId: Long): List<VehicleEntity>

    @Query("SELECT * FROM vehicles WHERE placa = :plate LIMIT 1")
    suspend fun getByPlaca(plate: String): VehicleEntity?

    @Query("UPDATE vehicles SET activo = :activo WHERE id = :id")
    suspend fun setActive(id: Long, activo: Boolean)

    @Query("UPDATE vehicles SET kilometrajeActual = :mileage WHERE id = :id")
    suspend fun setMileage(id: Long, mileage: Double)

    @Query("UPDATE vehicles SET conductorId = :driverId WHERE id = :id")
    suspend fun assignDriver(id: Long, driverId: Long?)

    @Query("SELECT COUNT(*) FROM vehicles WHERE activo = 1")
    suspend fun countActive(): Int
}