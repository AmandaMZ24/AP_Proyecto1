package com.example.controldeflotillastransandina.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.controldeflotillastransandina.data.entity.MaintenanceImageEntity

@Dao
interface MaintenanceImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<MaintenanceImageEntity>)

    @Query("SELECT * FROM maintenance_images WHERE maintenanceId = :maintenanceId")
    suspend fun getByMaintenance(maintenanceId: Long): List<MaintenanceImageEntity>

    @Query("SELECT * FROM maintenance_images WHERE maintenanceId = :maintenanceId")
    fun flowByMaintenance(maintenanceId: Long): kotlinx.coroutines.flow.Flow<List<MaintenanceImageEntity>>

    @Query("DELETE FROM maintenance_images WHERE maintenanceId = :maintenanceId")
    suspend fun deleteByMaintenance(maintenanceId: Long)
}