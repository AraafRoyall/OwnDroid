package com.bintianqi.owndroid.feature.work_profile

import android.content.IntentFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.NoneText
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.formatDate

@Composable
fun DynamicIntentFilterScreen(
    vm: DynamicIntentFilterViewModel,
    onNavigateUp: () -> Unit,
    navigate: (Destination) -> Unit
) {
    val filters by vm.filterListState.collectAsState()
    val filtersChanged by vm.filtersChangedState.collectAsState()
    MyLazyScaffold(R.string.dynamic_intent_filters, onNavigateUp, {
        IconButton({ navigate(Destination.AddDynamicIntentFilter(-1)) }) {
            Icon(Icons.Default.Add, null)
        }
        if (filtersChanged) FilledIconButton(vm::applyFilters) {
            Icon(Icons.Default.Check, null)
        }
    }) {
        if (filters.isEmpty()) item { NoneText() }
        items(filters, key = { it.id }) { entry ->
            Column(Modifier.animateItem()) {
                Row(
                    Modifier.fillMaxWidth().clickable {
                        navigate(Destination.AddDynamicIntentFilter(entry.id))
                    }.padding(HorizontalPadding, 8.dp, 8.dp, 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1F)) {
                        Text(
                            entry.options.actions.joinToString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (entry.options.categories.isNotEmpty()) {
                            Text(
                                "Categories: " + entry.options.categories.joinToString(),
                                Modifier.alpha(0.7F), style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (entry.options.mimeTypes.isNotEmpty()) {
                            Text(
                                "MIME: " + entry.options.mimeTypes.joinToString(),
                                Modifier.alpha(0.7F), style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            stringResource(directionTextMap[entry.options.direction] ?: R.string.both_direction),
                            Modifier.alpha(0.7F), style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            formatDate(entry.time),
                            Modifier.alpha(0.6F), style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(entry.enabled, { vm.setEnabled(entry.id, it) })
                }
                HorizontalDivider()
            }
        }
        item { Spacer(Modifier.height(BottomPadding)) }
    }
}

@Composable
fun AddDynamicIntentFilterScreen(
    params: Destination.AddDynamicIntentFilter,
    vm: DynamicIntentFilterViewModel,
    navigateUp: () -> Unit
) {
    val filters by vm.filterListState.collectAsState()
    val filtersLoaded by vm.filterListLoadedState.collectAsState()
    val actions = remember { mutableStateListOf("") }
    val categories = remember { mutableStateListOf<String>() }
    val mimeTypes = remember { mutableStateListOf<String>() }
    var direction by rememberSaveable { mutableIntStateOf(3) }
    var entryLoaded by rememberSaveable { mutableStateOf(params.editingId == -1) }

    LaunchedEffect(params.editingId, filtersLoaded) {
        if (params.editingId != -1 && filtersLoaded && !entryLoaded) {
            val options = filters.find { it.id == params.editingId }?.options
            if (options != null) {
                actions.clear()
                actions.addAll(options.actions.ifEmpty { listOf("") })
                categories.clear()
                categories.addAll(options.categories)
                mimeTypes.clear()
                mimeTypes.addAll(options.mimeTypes)
                direction = options.direction
            }
            entryLoaded = true
        }
    }

    val cleanActions = actions.map(String::trim).filter(String::isNotEmpty).distinct()
    val cleanCategories = categories.map(String::trim).filter(String::isNotEmpty).distinct()
    val cleanMimeTypes = mimeTypes.map(String::trim).filter(String::isNotEmpty).distinct()
    val invalidMime = cleanMimeTypes.any { !isValidIntentMimeType(it) }
    val canSave = cleanActions.isNotEmpty() && !invalidMime && entryLoaded

    MyScaffold(
        if (params.editingId == -1) R.string.add_filter else R.string.edit,
        navigateUp
    ) {
        DynamicStringListEditor(R.string.actions, actions, "Action")
        DynamicStringListEditor(R.string.categories, categories, "Category")
        DynamicStringListEditor(R.string.mime_types, mimeTypes, "MIME type")
        DynamicDirectionSelector(direction, { direction = it })

        if (cleanActions.isEmpty()) {
            Text(
                stringResource(R.string.dynamic_filter_action_required),
                Modifier.padding(horizontal = HorizontalPadding),
                color = MaterialTheme.colorScheme.error
            )
        }
        if (invalidMime) {
            Text(
                stringResource(R.string.dynamic_filter_mime_invalid),
                Modifier.padding(horizontal = HorizontalPadding),
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            {
                val options = DynamicIntentFilterOptions(
                    cleanActions, cleanCategories, cleanMimeTypes, direction
                )
                if (params.editingId == -1) vm.addFilter(options)
                else vm.updateFilter(params.editingId, options)
                navigateUp()
            },
            Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding, vertical = 6.dp),
            enabled = canSave
        ) {
            Text(stringResource(if (params.editingId == -1) R.string.add else R.string.update))
        }

        if (params.editingId != -1) {
            TextButton(
                { vm.deleteEntry(params.editingId); navigateUp() },
                Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Delete, null)
                Text(stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun DynamicStringListEditor(
    titleId: Int,
    values: SnapshotStateList<String>,
    fieldLabel: String
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(titleId), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1F))
            TextButton({ values.add("") }) {
                Icon(Icons.Default.Add, null)
                Text(stringResource(R.string.add))
            }
        }
        values.forEachIndexed { index, value ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value, { values[index] = it },
                    Modifier.weight(1F),
                    label = { Text(fieldLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next
                    )
                )
                IconButton({ values.removeAt(index) }) {
                    Icon(Icons.Outlined.Delete, stringResource(R.string.remove))
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DynamicDirectionSelector(direction: Int, onChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded, { expanded = it },
        Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding, vertical = 5.dp)
    ) {
        OutlinedTextField(
            stringResource(directionTextMap[direction] ?: R.string.both_direction),
            {},
            Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            label = { Text(stringResource(R.string.direction)) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            directionTextMap.forEach { (value, label) ->
                DropdownMenuItem(
                    { Text(stringResource(label)) },
                    { onChange(value); expanded = false }
                )
            }
        }
    }
}

private fun isValidIntentMimeType(value: String): Boolean {
    return try {
        IntentFilter().addDataType(value)
        true
    } catch (_: IntentFilter.MalformedMimeTypeException) {
        false
    }
}
