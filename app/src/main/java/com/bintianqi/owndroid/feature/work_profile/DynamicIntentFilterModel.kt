package com.bintianqi.owndroid.feature.work_profile

import kotlinx.serialization.Serializable

@Serializable
data class DynamicIntentFilterOptions(
    val actions: List<String>,
    val categories: List<String> = emptyList(),
    val mimeTypes: List<String> = emptyList(),
    val direction: Int = 3
) {
    fun normalized() = copy(
        actions = actions.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
        categories = categories.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
        mimeTypes = mimeTypes.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    )
}

class DynamicIntentFilterEntry(
    val id: Int,
    val options: DynamicIntentFilterOptions,
    val time: Long,
    val enabled: Boolean = true
)
