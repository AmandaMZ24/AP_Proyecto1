package com.example.controldeflotillastransandina.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    private val keyUserId = longPreferencesKey("current_user_id")

    val currentUserId: Flow<Long?> =
        context.dataStore.data.map { prefs -> prefs[keyUserId] }

    suspend fun setCurrentUser(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id == null) prefs.remove(keyUserId) else prefs[keyUserId] = id
        }
    }
}