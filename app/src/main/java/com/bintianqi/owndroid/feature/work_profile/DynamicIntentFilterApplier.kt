package com.bintianqi.owndroid.feature.work_profile

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.IntentFilter

object DynamicIntentFilterApplier {
    fun apply(
        dpm: DevicePolicyManager,
        admin: ComponentName,
        entries: List<DynamicIntentFilterEntry>
    ) {
        val pending = mutableListOf<Pair<IntentFilter, Int>>()
        entries.filter { it.enabled }.forEach { entry ->
            val options = entry.options.normalized()
            require(options.actions.isNotEmpty()) { "A filter must contain at least one action" }
            require(options.direction in 1..3) { "Invalid intent filter direction" }
            if (options.mimeTypes.isEmpty()) {
                pending += createFilter(options, includeMimeTypes = false) to options.direction
            } else {
                pending += createFilter(options, includeMimeTypes = true) to options.direction
                if (options.includeNoMime) {
                    pending += createFilter(options, includeMimeTypes = false) to options.direction
                }
            }
        }
        dpm.clearCrossProfileIntentFilters(admin)
        pending.forEach { (filter, direction) ->
            dpm.addCrossProfileIntentFilter(admin, filter, direction)
        }
    }

    private fun createFilter(
        options: DynamicIntentFilterOptions,
        includeMimeTypes: Boolean
    ): IntentFilter {
        val filter = IntentFilter()
        options.actions.forEach(filter::addAction)
        options.categories.forEach(filter::addCategory)
        if (includeMimeTypes) {
            options.mimeTypes.forEach { addMimeType(filter, it) }
        }
        return filter
    }

    private fun addMimeType(filter: IntentFilter, mimeType: String) {
        try {
            filter.addDataType(mimeType)
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw IllegalArgumentException("Invalid MIME type: $mimeType", e)
        }
    }
}
