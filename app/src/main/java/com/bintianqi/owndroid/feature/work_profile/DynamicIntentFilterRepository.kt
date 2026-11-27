package com.bintianqi.owndroid.feature.work_profile

import android.content.ContentValues
import com.bintianqi.owndroid.MyDbHelper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DynamicIntentFilterRepository(private val dbHelper: MyDbHelper) {
    fun addFilter(data: DynamicIntentFilterOptions, enabled: Boolean = true): Long {
        val options = data.normalized()
        val values = ContentValues().apply {
            put("actions", Json.encodeToString(options.actions))
            put("categories", Json.encodeToString(options.categories))
            put("mime_types", Json.encodeToString(options.mimeTypes))
            put("direction", options.direction)
            put("created_at", System.currentTimeMillis())
            put("enabled", if (enabled) 1 else 0)
        }
        return dbHelper.writableDatabase.insert("dynamic_cpif", null, values)
    }

    fun updateFilter(id: Int, data: DynamicIntentFilterOptions) {
        val options = data.normalized()
        val values = ContentValues().apply {
            put("actions", Json.encodeToString(options.actions))
            put("categories", Json.encodeToString(options.categories))
            put("mime_types", Json.encodeToString(options.mimeTypes))
            put("direction", options.direction)
            put("created_at", System.currentTimeMillis())
        }
        dbHelper.writableDatabase.update("dynamic_cpif", values, "id = ?", arrayOf(id.toString()))
    }

    fun setEnabled(id: Int, enabled: Boolean) {
        val values = ContentValues().apply { put("enabled", if (enabled) 1 else 0) }
        dbHelper.writableDatabase.update("dynamic_cpif", values, "id = ?", arrayOf(id.toString()))
    }

    fun getAllFilters(): List<DynamicIntentFilterEntry> {
        val result = mutableListOf<DynamicIntentFilterEntry>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT id, actions, categories, mime_types, direction, created_at, enabled FROM dynamic_cpif ORDER BY created_at DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val options = DynamicIntentFilterOptions(
                    Json.decodeFromString<List<String>>(cursor.getString(1)),
                    Json.decodeFromString<List<String>>(cursor.getString(2)),
                    Json.decodeFromString<List<String>>(cursor.getString(3)),
                    cursor.getInt(4)
                )
                result += DynamicIntentFilterEntry(
                    cursor.getInt(0), options, cursor.getLong(5), cursor.getInt(6) == 1
                )
            }
        }
        return result
    }

    fun deleteById(id: Int) {
        dbHelper.writableDatabase.delete("dynamic_cpif", "id = ?", arrayOf(id.toString()))
    }

    fun deleteAll() {
        dbHelper.writableDatabase.delete("dynamic_cpif", null, null)
    }

    fun consumeLegacyMigrationFlag(): Boolean {
        return dbHelper.writableDatabase.delete(
            "app_migrations", "name = ?", arrayOf(MyDbHelper.LEGACY_CPIF_MIGRATION)
        ) > 0
    }
}
