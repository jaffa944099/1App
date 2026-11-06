package io.github.jaffa944099.oneapp

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal val Context.taskPreferences by preferencesDataStore("tasks")

internal val favoriteTasksKey = stringSetPreferencesKey("favorites")

internal fun referenceKey(task: PilotTask) = stringPreferencesKey("reference_${task.id}")

internal suspend fun Context.toggleFavorite(task: PilotTask) {
    taskPreferences.edit { preferences ->
        val favorites = preferences[favoriteTasksKey].orEmpty()
        preferences[favoriteTasksKey] =
            if (task.id in favorites) favorites - task.id else favorites + task.id
    }
}

internal suspend fun Context.saveReference(task: PilotTask, value: String) {
    taskPreferences.edit { preferences ->
        val key = referenceKey(task)
        val reference = value.trim()
        if (reference.isEmpty()) preferences.remove(key) else preferences[key] = reference
    }
}
