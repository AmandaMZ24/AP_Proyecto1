package com.example.controldeflotillastransandina.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.controldeflotillastransandina.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET activo = :activo WHERE id = :userId")
    suspend fun setActive(userId: Long, activo: Boolean)

    @Query("SELECT * FROM users WHERE id = :id")
    fun getById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getByIdOnce(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE cedula = :cedula LIMIT 1")
    suspend fun getByCedula(cedula: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE rol = :rol AND activo = 1 ORDER BY nombre")
    fun getAllByRole(rol: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE rol = :rol AND activo = 1")
    suspend fun getAllByRoleOnce(rol: String): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun countByEmail(email: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE cedula = :cedula")
    suspend fun countByCedula(cedula: String): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countAll(): Int
}