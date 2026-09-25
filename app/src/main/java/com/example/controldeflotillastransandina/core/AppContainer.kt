package com.example.controldeflotillastransandina.core

import android.content.Context
import com.example.controldeflotillastransandina.data.AppDatabase
import com.example.controldeflotillastransandina.data.AppRepository

object AppContainer {

    lateinit var database: AppDatabase
        private set
    lateinit var repository: AppRepository
        private set
    lateinit var session: SessionManager
        private set

    fun init(context: Context) {
        if (::database.isInitialized) return
        database = AppDatabase.get(context)
        repository = AppRepository(database)
        session = SessionManager(context)
    }
}