package com.bintianqi.owndroid.feature.work_profile

import android.content.Intent
import com.bintianqi.owndroid.R
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

@Serializable
data class DynamicIntentFilterBackup(
    val version: Int = 1,
    val filters: List<DynamicIntentFilterBackupEntry>
)

@Serializable
data class DynamicIntentFilterBackupEntry(
    val options: DynamicIntentFilterOptions,
    val enabled: Boolean = true
)

class DynamicIntentFilterPreset(
    val name: Int,
    val options: DynamicIntentFilterOptions
)

val directionTextMap = mapOf(
    1 to R.string.personal_to_work,
    2 to R.string.work_to_personal,
    3 to R.string.both_direction
)

val dynamicIntentFilterPresets = listOf(
    DynamicIntentFilterPreset(R.string.open_file, DynamicIntentFilterOptions(listOf(Intent.ACTION_VIEW), listOf(Intent.CATEGORY_DEFAULT), listOf("*/*"))),
    DynamicIntentFilterPreset(R.string.share, DynamicIntentFilterOptions(listOf(Intent.ACTION_SEND), listOf(Intent.CATEGORY_DEFAULT), listOf("*/*"))),
    DynamicIntentFilterPreset(R.string.share_multiple, DynamicIntentFilterOptions(listOf(Intent.ACTION_SEND_MULTIPLE))),
    DynamicIntentFilterPreset(R.string.edit, DynamicIntentFilterOptions(listOf(Intent.ACTION_EDIT), listOf(Intent.CATEGORY_DEFAULT), listOf("*/*"))),
    DynamicIntentFilterPreset(R.string.get_content, DynamicIntentFilterOptions(listOf(Intent.ACTION_GET_CONTENT), listOf(Intent.CATEGORY_DEFAULT), listOf("*/*"))),
    DynamicIntentFilterPreset(R.string.install_app, DynamicIntentFilterOptions(listOf(Intent.ACTION_INSTALL_PACKAGE))),
    DynamicIntentFilterPreset(R.string.uninstall_app, DynamicIntentFilterOptions(listOf(Intent.ACTION_UNINSTALL_PACKAGE))),
    DynamicIntentFilterPreset(R.string.choose_file, DynamicIntentFilterOptions(listOf(Intent.ACTION_OPEN_DOCUMENT), listOf(Intent.CATEGORY_DEFAULT), listOf("*/*"))),
    DynamicIntentFilterPreset(R.string.choose_folder, DynamicIntentFilterOptions(listOf(Intent.ACTION_OPEN_DOCUMENT_TREE)))
)
