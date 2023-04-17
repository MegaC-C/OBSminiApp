package com.example.obsmini.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


interface MyDataStore {
    fun getTheme(): Flow<Boolean>
    suspend fun changeTheme()

    fun getLeftRight(): Flow<Boolean>
    suspend fun changeLeftRight()

    fun getPortalUrlFlow(): Flow<String>
    suspend fun setPortalUrl(portalUrl: String)

    fun getPortalTokenFlow(): Flow<String>
    suspend fun setPortalToken(portalUrl: String)

    fun getLeftHandlebarFlow(): Flow<Int>
    suspend fun setLeftHandlebar(leftHandlebar: Int)

    fun getRightHandlebarFlow(): Flow<Int>
    suspend fun setRightHandlebar(rightHandlebar: Int)
}

class MyDataStoreImpl @Inject constructor(private val context: Context) : MyDataStore {

    private companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore("myDataStore")
        val DARKTHEME_KEY = booleanPreferencesKey("theme")
        val LEFT_RIGHT_KEY = booleanPreferencesKey("leftRight")
        val PORTAL_URL_KEY = stringPreferencesKey("url")
        val PORTAL_TOKEN_KEY = stringPreferencesKey("token")
        val LEFT_HANDLEBAR_KEY = intPreferencesKey("leftHandlebar")
        val RIGHT_HANDLEBAR_KEY = intPreferencesKey("rightHandlebar")
    }

    override fun getTheme(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARKTHEME_KEY] ?: true
        }
    override suspend fun changeTheme() {
        context.dataStore.edit { preferences ->
            val currentTheme = preferences[DARKTHEME_KEY] ?: true
            preferences[DARKTHEME_KEY] = !currentTheme
        }
    }

    override fun getLeftRight(): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[LEFT_RIGHT_KEY] ?: true
        }
    override suspend fun changeLeftRight() {
        context.dataStore.edit { preferences ->
            val currentLeftRight = preferences[LEFT_RIGHT_KEY] ?: true
            preferences[LEFT_RIGHT_KEY] = !currentLeftRight
        }
    }

    override fun getPortalUrlFlow(): Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PORTAL_URL_KEY] ?: ""
        }
    override suspend fun setPortalUrl(portalUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[PORTAL_URL_KEY] = portalUrl
        }
    }

    override fun getPortalTokenFlow(): Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PORTAL_TOKEN_KEY] ?: ""
        }
    override suspend fun setPortalToken(portalUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[PORTAL_TOKEN_KEY] = portalUrl
        }
    }

    override fun getLeftHandlebarFlow(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[LEFT_HANDLEBAR_KEY] ?: 0
        }
    override suspend fun setLeftHandlebar(leftHandlebar: Int) {
        context.dataStore.edit { preferences ->
            preferences[LEFT_HANDLEBAR_KEY] = leftHandlebar
        }
    }

    override fun getRightHandlebarFlow(): Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[RIGHT_HANDLEBAR_KEY] ?: 0
        }
    override suspend fun setRightHandlebar(rightHandlebar: Int) {
        context.dataStore.edit { preferences ->
            preferences[RIGHT_HANDLEBAR_KEY] = rightHandlebar
        }
    }
}

