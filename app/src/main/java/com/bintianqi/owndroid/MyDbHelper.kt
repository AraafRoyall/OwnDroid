package com.bintianqi.owndroid

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.bintianqi.owndroid.feature.applications.AppGroupRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 18) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DHIZUKU_CLIENTS_TABLE)
        db.execSQL(SECURITY_LOGS_TABLE)
        db.execSQL(NETWORK_LOGS_TABLE)
        db.execSQL(APP_GROUPS_TABLE_V3)
        db.execSQL(APP_GROUP_APPS_TABLE)
        db.execSQL(DYNAMIC_CPIF_TABLE)
        db.execSQL(APP_MIGRATIONS_TABLE)
        db.execSQL(TIME_BLOCK_RULES_TABLE)
        db.execSQL(TIME_BLOCK_SUSPENDED_TABLE)
        db.execSQL(TIME_BLOCK_USAGE_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL(SECURITY_LOGS_TABLE)
        if (oldVersion < 3) db.execSQL(NETWORK_LOGS_TABLE)
        if (oldVersion < 4) db.execSQL(APP_GROUPS_TABLE)
        if (oldVersion < 8) db.execSQL(CPIF_TABLE)
        if (oldVersion < 9) {
            db.execSQL(DELETE_CPIF)
            db.execSQL(CPIF2_TABLE_OLD)
        }
        if (oldVersion < 10) db.execSQL(CPIF2_ADD_STATUS)
        if (oldVersion < 11) {
            // Recreate because databases created at version 10 may lack the enabled column.
            db.execSQL("DROP TABLE cpif2")
            db.execSQL(CPIF2_TABLE)
        }
        if (oldVersion < 12) {
            db.execSQL(TIME_BLOCK_RULES_TABLE)
            db.execSQL(TIME_BLOCK_SUSPENDED_TABLE)
        }
        if (oldVersion < 13) db.execSQL(TIME_BLOCK_USAGE_TABLE)
        if (oldVersion < 14) {
            val groups = AppGroupRepository.getAppGroupsV1(db)
            db.execSQL(APP_GROUP_APPS_TABLE)
            groups.forEach { AppGroupRepository.setGroupApps(db, it.id.toLong(), it.apps) }
        }
        if (oldVersion < 15) db.execSQL("ALTER TABLE app_groups ADD COLUMN position INTEGER")
        if (oldVersion < 16) db.execSQL(DYNAMIC_CPIF_TABLE)
        if (oldVersion < 17) {
            db.execSQL(DYNAMIC_CPIF_TABLE)
            db.execSQL(APP_MIGRATIONS_TABLE)
            val legacyExists = db.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name='cpif2'", null
            ).use { it.moveToFirst() }
            if (legacyExists) {
                var migrated = 0
                db.rawQuery(
                    "SELECT action_str, category, mime_type, direction, created_at, enabled FROM cpif2",
                    null
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        val action = cursor.getString(0)?.trim().orEmpty()
                        if (action.isEmpty()) continue
                        val actions: List<String> = listOf(action)
                        val category = cursor.getString(1)?.trim().orEmpty()
                        val mimeType = cursor.getString(2)?.trim().orEmpty()
                        val values = ContentValues().apply {
                            put("actions", Json.encodeToString(actions))
                            put("categories", Json.encodeToString(if (category.isEmpty()) emptyList() else listOf(category)))
                            put("mime_types", Json.encodeToString(if (mimeType.isEmpty()) emptyList() else listOf(mimeType)))
                            put("direction", cursor.getInt(3))
                            put("created_at", cursor.getLong(4))
                            put("enabled", if (cursor.getInt(5) == 1) 1 else 0)
                        }
                        if (db.insert("dynamic_cpif", null, values) != -1L) migrated++
                    }
                }
                if (migrated > 0) {
                    val marker = ContentValues().apply {
                        put("name", LEGACY_CPIF_MIGRATION)
                        put("created_at", System.currentTimeMillis())
                    }
                    db.insertWithOnConflict("app_migrations", null, marker, SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.execSQL("DROP TABLE cpif2")
            }
        }
        if (oldVersion < 18) {
            val hasIncludeNoMime = db.rawQuery("PRAGMA table_info(dynamic_cpif)", null).use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                var found = false
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == "include_no_mime") {
                        found = true
                        break
                    }
                }
                found
            }
            if (!hasIncludeNoMime) {
                db.execSQL("ALTER TABLE dynamic_cpif ADD COLUMN include_no_mime INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Debug builds are often installed over newer versions; reset the DB on downgrade.
        Log.w("MyDbHelper", "DB downgrade $oldVersion -> $newVersion, resetting all tables")
        val tables = mutableListOf<String>()
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
            null
        ).use { c -> while (c.moveToNext()) tables += c.getString(0) }
        for (table in tables) db.execSQL("DROP TABLE IF EXISTS `$table`")
        onCreate(db)
    }

    companion object {
        const val LEGACY_CPIF_MIGRATION = "legacy_cpif_migrated"
        const val DHIZUKU_CLIENTS_TABLE = "CREATE TABLE dhizuku_clients (uid INTEGER PRIMARY KEY, signature TEXT, permissions TEXT)"
        const val SECURITY_LOGS_TABLE = "CREATE TABLE security_logs (id INTEGER, tag INTEGER, level INTEGER, time INTEGER, data TEXT)"
        const val NETWORK_LOGS_TABLE = "CREATE TABLE network_logs (id INTEGER, package INTEGER, time INTEGER, type TEXT, host TEXT, count INTEGER, addresses TEXT, address TEXT, port INTEGER)"
        const val APP_GROUPS_TABLE = "CREATE TABLE app_groups(id INTEGER PRIMARY KEY, name TEXT, apps TEXT)"
        const val APP_GROUPS_TABLE_V3 = "CREATE TABLE app_groups(id INTEGER PRIMARY KEY, name TEXT NOT NULL, position INTEGER)"
        const val APP_GROUP_APPS_TABLE = "CREATE TABLE app_group_apps(group_id INTEGER REFERENCES app_groups (id) ON DELETE CASCADE, package_name TEXT NOT NULL, UNIQUE(group_id, package_name) ON CONFLICT IGNORE)"
        const val CPIF_TABLE = "CREATE TABLE cpif (action_str TEXT, category TEXT, mime_type TEXT, direction INTEGER, time INTEGER)"
        const val DELETE_CPIF = "DROP TABLE cpif"
        const val CPIF2_TABLE_OLD = "CREATE TABLE cpif2 (id INTEGER PRIMARY KEY, action_str TEXT, category TEXT, mime_type TEXT, direction INTEGER, created_at INTEGER)"
        const val CPIF2_ADD_STATUS = "ALTER TABLE cpif2 ADD COLUMN enabled INTEGER DEFAULT TRUE"
        const val CPIF2_TABLE = "CREATE TABLE cpif2 (id INTEGER PRIMARY KEY, action_str TEXT, category TEXT, mime_type TEXT, direction INTEGER, created_at INTEGER, enabled INTEGER DEFAULT TRUE)"
        const val DYNAMIC_CPIF_TABLE = "CREATE TABLE IF NOT EXISTS dynamic_cpif (id INTEGER PRIMARY KEY, actions TEXT NOT NULL, categories TEXT NOT NULL, mime_types TEXT NOT NULL, direction INTEGER NOT NULL, include_no_mime INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL, enabled INTEGER DEFAULT TRUE)"
        const val APP_MIGRATIONS_TABLE = "CREATE TABLE IF NOT EXISTS app_migrations (name TEXT PRIMARY KEY, created_at INTEGER NOT NULL)"
        const val TIME_BLOCK_RULES_TABLE = "CREATE TABLE time_block_rules (id INTEGER PRIMARY KEY, package_name TEXT, daily_limit_minutes INTEGER, blocked_windows TEXT, allowed_windows TEXT, enabled INTEGER)"
        const val TIME_BLOCK_SUSPENDED_TABLE = "CREATE TABLE time_block_suspended (package_name TEXT PRIMARY KEY)"
        const val TIME_BLOCK_USAGE_TABLE = "CREATE TABLE time_block_usage (package_name TEXT PRIMARY KEY, used_ms INTEGER, day_epoch INTEGER)"
    }
}
