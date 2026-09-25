package com.example.controldeflotillastransandina.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.controldeflotillastransandina.data.entity.MaintenanceEntity
import kotlinx.coroutines.flow.Flow

data class MaintenanceWithInfo(
    @Embedded val maintenance: MaintenanceEntity,
    @ColumnInfo(name = "placa") val placa: String,
    @ColumnInfo(name = "modelo") val modelo: String,
    @ColumnInfo(name = "userName") val userName: String
)

data class CostSummary(
    @ColumnInfo(name = "vehicleId") val vehicleId: Long,
    @ColumnInfo(name = "total") val total: Double,
    @ColumnInfo(name = "count") val count: Int
)

@Dao
interface MaintenanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maintenance: MaintenanceEntity): Long

    @Update
    suspend fun update(maintenance: MaintenanceEntity)

    @Query("SELECT * FROM maintenances WHERE id = :id")
    suspend fun getByIdOnce(id: Long): MaintenanceEntity?

    @Query(
        """SELECT m.*, v.placa AS placa, v.modelo AS modelo, u.nombre AS userName
           FROM maintenances m
           INNER JOIN vehicles v ON v.id = m.vehicleId
           INNER JOIN users u ON u.id = m.userId
           ORDER BY m.fecha DESC"""
    )
    fun getAllWithInfo(): Flow<List<MaintenanceWithInfo>>

    @Query(
        """SELECT m.*, v.placa AS placa, v.modelo AS modelo, u.nombre AS userName
           FROM maintenances m
           INNER JOIN vehicles v ON v.id = m.vehicleId
           INNER JOIN users u ON u.id = m.userId
           WHERE m.vehicleId = :vehicleId
           ORDER BY m.fecha DESC"""
    )
    fun getByVehicle(vehicleId: Long): Flow<List<MaintenanceWithInfo>>

    @Query(
        """SELECT m.*, v.placa AS placa, v.modelo AS modelo, u.nombre AS userName
           FROM maintenances m
           INNER JOIN vehicles v ON v.id = m.vehicleId
           INNER JOIN users u ON u.id = m.userId
           WHERE (:vehicleId IS NULL OR m.vehicleId = :vehicleId)
             AND (:tipo IS NULL OR m.tipo = :tipo)
             AND (:fromDate IS NULL OR m.fecha >= :fromDate)
             AND (:toDate IS NULL OR m.fecha <= :toDate)
           ORDER BY m.fecha DESC"""
    )
    suspend fun getFiltered(
        vehicleId: Long?,
        tipo: String?,
        fromDate: Long?,
        toDate: Long?
    ): List<MaintenanceWithInfo>

    @Query(
        """SELECT m.vehicleId AS vehicleId, SUM(m.costo) AS total, COUNT(*) AS count
           FROM maintenances m
           WHERE (:vehicleId IS NULL OR m.vehicleId = :vehicleId)
             AND (:tipo IS NULL OR m.tipo = :tipo)
             AND (:fromDate IS NULL OR m.fecha >= :fromDate)
             AND (:toDate IS NULL OR m.fecha <= :toDate)
           GROUP BY m.vehicleId
           ORDER BY total DESC"""
    )
    suspend fun getCostSummary(
        vehicleId: Long?,
        tipo: String?,
        fromDate: Long?,
        toDate: Long?
    ): List<CostSummary>

    @Query(
        """SELECT * FROM maintenances
           WHERE vehicleId = :vehicleId AND tipo = 'PREVENTIVO'
           ORDER BY fecha DESC, id DESC LIMIT 1"""
    )
    suspend fun getLastPreventive(vehicleId: Long): MaintenanceEntity?

    @Query("SELECT COUNT(*) FROM maintenances WHERE vehicleId = :vehicleId")
    suspend fun countForVehicle(vehicleId: Long): Int

    @Query("DELETE FROM maintenances WHERE id = :id")
    suspend fun deleteById(id: Long)
}