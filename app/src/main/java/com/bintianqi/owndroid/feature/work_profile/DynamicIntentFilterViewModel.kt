package com.bintianqi.owndroid.feature.work_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DynamicIntentFilterViewModel(
    private val ph: PrivilegeHelper,
    private val repo: DynamicIntentFilterRepository,
    private val classicRepo: CrossProfileIntentFilterRepository,
    private val toastChannel: ToastChannel,
    private val sr: SettingsRepository
) : ViewModel() {
    val filterListState = MutableStateFlow(emptyList<DynamicIntentFilterEntry>())
    val filtersChangedState = MutableStateFlow(false)
    val filterListLoadedState = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                filterListState.value = repo.getAllFilters()
                filtersChangedState.value = sr.data.cpifChanged
            } finally {
                filterListLoadedState.value = true
            }
        }
    }

    fun addFilter(options: DynamicIntentFilterOptions) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.addFilter(options)
            if (id > 0) {
                val saved = options.normalized()
                filterListState.update {
                    listOf(DynamicIntentFilterEntry(id.toInt(), saved, System.currentTimeMillis())) + it
                }
                setFilterChanged(true)
            } else {
                toastChannel.sendStatus(false)
            }
        }
    }

    fun updateFilter(id: Int, options: DynamicIntentFilterOptions) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateFilter(id, options)
            val saved = options.normalized()
            filterListState.update { list ->
                list.map {
                    if (it.id == id) DynamicIntentFilterEntry(
                        id, saved, System.currentTimeMillis(), it.enabled
                    ) else it
                }.sortedByDescending { it.time }
            }
            setFilterChanged(true)
        }
    }

    fun setEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setEnabled(id, enabled)
            filterListState.update { list ->
                list.map { if (it.id == id) DynamicIntentFilterEntry(
                    id, it.options, it.time, enabled
                ) else it }
            }
            setFilterChanged(true)
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteById(id)
            filterListState.update { list -> list.filterNot { it.id == id } }
            setFilterChanged(true)
        }
    }

    fun applyFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var applied = false
                ph.safeDpmCall {
                    CrossProfileIntentFilterApplier.apply(
                        dpm, dar, classicRepo.getAllFilters(), repo.getAllFilters()
                    )
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

    private fun setFilterChanged(changed: Boolean) {
        filtersChangedState.value = changed
        sr.update { it.cpifChanged = changed }
    }
}
