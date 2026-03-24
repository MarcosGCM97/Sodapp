package com.example.sodappcomposse
// En UserPreferencesRepository.kt
import android.content.Context
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey // Importar para Set<String>
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ...

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val SCHEDULED_VISIT_DAYS = stringSetPreferencesKey("scheduled_visit_days")
        // --- NUEVA CLAVE ---
        // Almacenará un conjunto de "entregas completadas" con el formato "clienteId-Dia"
        val COMPLETED_DELIVERIES = stringSetPreferencesKey("completed_deliveries")
        //Para los datos del usuario que ingreso
        val USER_ID = stringSetPreferencesKey("user_id") // O intPreferencesKey
        val USER_NAME = stringSetPreferencesKey("user_name")
        val IS_LOGGED_IN = androidx.datastore.preferences.core.booleanPreferencesKey("is_logged_in")

    }
    // Función para guardar los datos al hacer Login
    suspend fun saveUserData(id: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = setOf(id)
            preferences[PreferencesKeys.USER_NAME] = setOf(name)
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
        }
    }

    // Flujo para saber si el usuario está logueado
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }

    // Función para borrar los datos al cerrar sesión (Logout)
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }


    // Flujo para leer el conjunto de entregas completadas
    val completedDeliveries: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.COMPLETED_DELIVERIES] ?: emptySet()
        }

    // Función para añadir una entrega completada
    suspend fun addCompletedDelivery(deliveryId: String) {
        context.dataStore.edit { preferences ->
            val currentDeliveries = preferences[PreferencesKeys.COMPLETED_DELIVERIES] ?: emptySet()
            preferences[PreferencesKeys.COMPLETED_DELIVERIES] = currentDeliveries + deliveryId
        }
    }

    // Función para quitar una entrega (desmarcar el checkbox)
    suspend fun removeCompletedDelivery(deliveryId: String) {
        context.dataStore.edit { preferences ->
            val currentDeliveries = preferences[PreferencesKeys.COMPLETED_DELIVERIES] ?: emptySet()
            preferences[PreferencesKeys.COMPLETED_DELIVERIES] = currentDeliveries - deliveryId
        }
    }

    // Función para guardar un conjunto limpio de entregas (para la limpieza de datos antiguos)
    suspend fun saveCleanedDeliveries(cleanedDeliveries: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPLETED_DELIVERIES] = cleanedDeliveries
        }
    }
}