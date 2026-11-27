package com.bintianqi.owndroid.feature.work_profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class LegacyIntentFilterOptions(
    val action: String,
    val category: String = "",
    val mimeType: String = "",
    val direction: Int = 3
)

class DynamicIntentFilterViewModel(
    private val application: MyApplication,
    private val ph: PrivilegeHelper,
    private val repo: DynamicIntentFilterRepository,
    private val toastChannel: ToastChannel,
    private val sr: SettingsRepository
) : ViewModel() {
    val filterListState = MutableStateFlow(emptyList<DynamicIntentFilterEntry>())
    val filtersChangedState = MutableStateFlow(false)
    val filterListLoadedState = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val migrated = repo.consumeLegacyMigrationFlag()
                if (migrated) sr.update { it.cpifChanged = true }
                filterListState.value = repo.getAllFilters()
                filtersChangedState.value = sr.data.cpifChanged || migrated
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            } finally {
                filterListLoadedState.value = true
            }
        }
    }

    fun addFilter(options: DynamicIntentFilterOptions) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = repo.addFilter(options)
                check(id > 0) { "Unable to save intent filter" }
                refresh()
                setFilterChanged(true)
                toastChannel.sendStatus(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    fun updateFilter(id: Int, options: DynamicIntentFilterOptions) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.updateFilter(id, options)
                refresh()
                setFilterChanged(true)
                toastChannel.sendStatus(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    fun setEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.setEnabled(id, enabled)
                refresh()
                setFilterChanged(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.deleteById(id)
                refresh()
                setFilterChanged(true)
                toastChannel.sendStatus(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    fun deleteAllFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.deleteAll()
                refresh()
                setFilterChanged(true)
                toastChannel.sendStatus(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    fun addPreset(preset: DynamicIntentFilterPreset, direction: Int) {
        addFilter(preset.options.copy(direction = direction))
    }

    fun applyFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var applied = false
                ph.safeDpmCall {
                    DynamicIntentFilterApplier.apply(dpm, dar, repo.getAllFilters())
                    applied = true
                }
                if (applied) setFilterChanged(false)
                toastChannel.sendStatus(applied)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    fun importFilters(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = application.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().decodeToString()
                } ?: error("Unable to open import file")
                val imported = try {
                    val backup = Json.decodeFromString<DynamicIntentFilterBackup>(json)
                    require(backup.version == 1) { "Unsupported backup version" }
                    backup.filters.map { it.options to it.enabled }
                } catch (_: Exception) {
                    try {
                        Json.decodeFromString<List<DynamicIntentFilterOptions>>(json).map { it to true }
                    } catch (_: Exception) {
                        Json.decodeFromString<List<LegacyIntentFilterOptions>>(json).map {
                            DynamicIntentFilterOptions(
                                listOf(it.action), listOf(it.category), listOf(it.mimeType), it.direction
                            ) to true
                        }
                    }
                }
                val valid = imported.map { it.first.normalized() to it.second }
                    .filter { it.first.actions.isNotEmpty() }
                require(valid.isNotEmpty()) { "No valid intent filters found" }
                valid.forEach { (options, enabled) -> repo.addFilter(options, enabled) }
                refresh()
                setFilterChanged(true)
                toastChannel.sendStatus(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    fun exportFilters(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val backup = DynamicIntentFilterBackup(
                    filters = repo.getAllFilters().map {
                        DynamicIntentFilterBackupEntry(it.options, it.enabled)
                    }
                )
                val bytes = Json.encodeToString(backup).encodeToByteArray()
                application.contentResolver.openOutputStream(uri)?.use {
                    it.write(bytes)
                } ?: error("Unable to open export file")
                toastChannel.sendStatus(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
        }
    }

    private fun refresh() {
        filterListState.value = repo.getAllFilters()
    }

    private fun setFilterChanged(changed: Boolean) {
        filtersChangedState.value = changed
        sr.update { it.cpifChanged = changed }
    }
}
