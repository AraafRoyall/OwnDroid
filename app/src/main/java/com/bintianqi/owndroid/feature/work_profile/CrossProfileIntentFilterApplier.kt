package com.bintianqi.owndroid.feature.work_profile

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.IntentFilter

object CrossProfileIntentFilterApplier {
    fun apply(
        dpm: DevicePolicyManager,
        admin: ComponentName,
        classicEntries: List<IntentFilterEntry>,
        dynamicEntries: List<DynamicIntentFilterEntry>
    ) {
        val pending = mutableListOf<Pair<IntentFilter, Int>>()

        classicEntries.filter { it.enabled }.forEach { entry ->
            val options = entry.options
            val filter = IntentFilter(options.action)
            if (options.category.isNotEmpty()) filter.addCategory(options.category)
            if (options.mimeType.isNotEmpty()) addMimeType(filter, options.mimeType)
            require(options.direction in 1..3) { "Invalid intent filter direction" }
            pending += filter to options.direction
        }

        dynamicEntries.filter { it.enabled }.forEach { entry ->
            val options = entry.options.normalized()
            require(options.actions.isNotEmpty()) { "A dynamic filter must contain at least one action" }
            require(options.direction in 1..3) { "Invalid intent filter direction" }
            val filter = IntentFilter()
            options.actions.forEach(filter::addAction)
            options.categories.forEach(filter::addCategory)
            options.mimeTypes.forEach { addMimeType(filter, it) }
            pending += filter to options.direction
        }

        dpm.clearCrossProfileIntentFilters(admin)
        pending.forEach { (filter, direction) ->
            dpm.addCrossProfileIntentFilter(admin, filter, direction)
        }
    }

    private fun addMimeType(filter: IntentFilter, mimeType: String) {
        try {
            filter.addDataType(mimeType)
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw IllegalArgumentException("Invalid MIME type: $mimeType", e)
        }
    }
}
