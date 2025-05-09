package com.example.coursework.data.storage

import android.content.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.*

private val Context.dataStore by preferencesDataStore(name = "token_store")

class TokenDataStore(private val context: Context) {

    companion object {
        val TOKENS_KEY = stringSetPreferencesKey("tokens")
        val SELECTED_TOKEN_KEY = stringPreferencesKey("selected_token")
    }

    val allTokens: Flow<Set<String>> = context.dataStore.data.map {
        it[TOKENS_KEY] ?: emptySet()
    }

    val selectedToken: Flow<String?> = context.dataStore.data.map {
        it[SELECTED_TOKEN_KEY]
    }

    suspend fun addToken(token: String) {
        context.dataStore.edit {
            val updated = it[TOKENS_KEY]?.toMutableSet() ?: mutableSetOf()
            updated.add(token)
            it[TOKENS_KEY] = updated
            it[SELECTED_TOKEN_KEY] = token
        }
    }

    suspend fun selectToken(token: String) {
        context.dataStore.edit {
            it[SELECTED_TOKEN_KEY] = token
        }
    }

    fun getTokenName(token: String): Flow<String?> {
        return context.dataStore.data.map { it[stringPreferencesKey("name_$token")] }
    }

    suspend fun saveTokenName(token: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey("name_$token")] = name
        }
    }
}