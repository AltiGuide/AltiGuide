package com.example.altiguide_mobile.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthDataStore(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val EMAIL_KEY = stringPreferencesKey("auth_email")
        val USER_PROFILE_KEY = stringPreferencesKey("user_profile_json")
    }

    val authTokenFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY] ?: ""
    }

    val authEmailFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY] ?: ""
    }

    val userProfileFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_PROFILE_KEY] ?: ""
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
        }
    }

    suspend fun saveUserProfile(userJson: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_PROFILE_KEY] = userJson
        }
    }

    suspend fun saveAuthData(token: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[EMAIL_KEY] = email
        }
    }

    suspend fun saveAuthDataWithProfile(token: String, email: String, profileJson: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[EMAIL_KEY] = email
            preferences[USER_PROFILE_KEY] = profileJson
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(USER_PROFILE_KEY)
        }
    }
}

