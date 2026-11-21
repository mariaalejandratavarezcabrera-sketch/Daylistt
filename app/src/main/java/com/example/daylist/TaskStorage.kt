
package com.example.daylist

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "daylist_tasks")

class TaskStorage(private val context: Context) {

    private val TASKS_KEY = stringPreferencesKey("tasks")

    val tasks: Flow<List<Task>> = context.dataStore.data.map { preferences ->
        val json = preferences[TASKS_KEY] ?: "[]"
        val array = JSONArray(json)

        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            Task(
                id = item.getString("id"),
                title = item.getString("title"),
                subject = item.getString("subject"),
                description = item.getString("description"),
                completed = item.getBoolean("completed")
            )
        }
    }

    suspend fun saveTasks(tasks: List<Task>) {
        context.dataStore.edit { preferences ->
            val array = JSONArray()

            tasks.forEach { task ->
                val item = JSONObject()
                item.put("id", task.id)
                item.put("title", task.title)
                item.put("subject", task.subject)
                item.put("description", task.description)
                item.put("completed", task.completed)
                array.put(item)
            }

            preferences[TASKS_KEY] = array.toString()
        }
    }
}
