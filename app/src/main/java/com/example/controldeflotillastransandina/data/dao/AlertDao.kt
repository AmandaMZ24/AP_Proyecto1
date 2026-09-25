package com.example.controldeflotillastransandina.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.controldeflotillastransandina.data.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity): Long

    @Query("SELECT * FROM alerts WHERE atendida = 0 ORDER BY prioridad ASC, fecha DESC")
    fun getActive(): Flow<List<AlertEntity>>

    @Query(
        """SELECT * FROM alerts
           WHERE atendida = 0
             AND (userId = :userId OR userId IS NULL)
           ORDER BY prioridad ASC, fecha DESC"""
    )
    fun getActiveForUser(userId: Long): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY fecha DESC")
    fun getAll(): Flow<List<AlertEntity>>

    @Query("UPDATE alerts SET atendida = 1 WHERE id = :id")
    suspend fun markAttended(id: Long)

    @Query("UPDATE alerts SET atendida = 1")
    suspend fun markAllAttended()

    @Query("DELETE FROM alerts WHERE atendida = 1")
    suspend fun clearAttended()

    @Query("DELETE FROM alerts")
    suspend fun clearAll()

    @Query("DELETE FROM alerts WHERE tipo IN (:tipos)")
    suspend fun deleteByTypes(tipos: List<String>)
}