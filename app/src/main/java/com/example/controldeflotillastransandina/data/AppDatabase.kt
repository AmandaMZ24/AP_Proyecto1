package com.example.controldeflotillastransandina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.controldeflotillastransandina.data.dao.AlertDao
import com.example.controldeflotillastransandina.data.dao.MaintenanceDao
import com.example.controldeflotillastransandina.data.dao.MaintenanceImageDao
import com.example.controldeflotillastransandina.data.dao.OdometerDao
import com.example.controldeflotillastransandina.data.dao.UserDao
import com.example.controldeflotillastransandina.data.dao.VehicleDao
import com.example.controldeflotillastransandina.data.entity.AlertEntity
import com.example.controldeflotillastransandina.data.entity.MaintenanceEntity
import com.example.controldeflotillastransandina.data.entity.MaintenanceImageEntity
import com.example.controldeflotillastransandina.data.entity.OdometerRecordEntity
import com.example.controldeflotillastransandina.data.entity.UserEntity
import com.example.controldeflotillastransandina.data.entity.VehicleEntity

@Database(
    entities = [
        UserEntity::class,
        VehicleEntity::class,
        MaintenanceEntity::class,
        MaintenanceImageEntity::class,
        OdometerRecordEntity::class,
        AlertEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun imageDao(): MaintenanceImageDao
    abstract fun odometerDao(): OdometerDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transandina.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}