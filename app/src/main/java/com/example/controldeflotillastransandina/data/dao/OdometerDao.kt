package com.example.controldeflotillastransandina.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.controldeflotillastransandina.data.entity.OdometerRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OdometerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: OdometerRecordEntity): Long

    @Query("SELECT * FROM odometer_records WHERE vehicleId = :vehicleId ORDER BY fecha ASC, id ASC")
    fun getByVehicle(vehicleId: Long): Flow<List<OdometerRecordEntity>>

    @Query("SELECT * FROM odometer_records WHERE vehicleId = :vehicleId ORDER BY fecha DESC, id DESC LIMIT 1")
    suspend fun getLast(vehicleId: Long): OdometerRecordEntity?

    @Query("SELECT MAX(kilometraje) FROM odometer_records WHERE vehicleId = :vehicleId")
    suspend fun getMaxMileage(vehicleId: Long): Double?

    @Query("SELECT COUNT(*) FROM odometer_records WHERE vehicleId = :vehicleId")
    suspend fun countForVehicle(vehicleId: Long): Int
}