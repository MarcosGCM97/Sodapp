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
import kotlinx.coroutines.flow.map

// ...

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SCHEDULED_VISIT_DAYS = stringSetPreferencesKey("scheduled_visit_days")
        // --- NUEVA CLAVE ---
        // Almacenará un conjunto de "entregas completadas" con el formato "clienteId-Dia"
        val COMPLETED_DELIVERIES = stringSetPreferencesKey("completed_deliveries")
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