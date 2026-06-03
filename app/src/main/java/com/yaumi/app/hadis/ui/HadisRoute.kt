package com.yaumi.app.hadis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaumi.app.hadis.domain.model.HadisCollection
import com.yaumi.app.ui.data.PickerLayoutStore
import com.yaumi.app.ui.components.ScreenIntroCard

private enum class PickerLayout {
    LIST,
    GRID
}

@Composable
fun HadisRoute(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val layoutStore = remember { PickerLayoutStore(context.applicationContext) }
    val vm: HadisViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showKitabSheet by rememberSaveable { mutableStateOf(false) }
    var kitabQuery by rememberSaveable { mutableStateOf("") }
    var kitabLayout by rememberSaveable {
        mutableStateOf(layoutStore.getHadisKitabLayout(PickerLayout.LIST.name))
    }
    var query by rememberSaveable { mutableStateOf("") }

    val filteredKitab = remember(state.collections, kitabQuery) {
        val q = kitabQuery.trim().lowercase()
        if (q.isBlank()) {
            state.collections
        } else {
            state.collections.filter { kitab ->
                kitab.name.lowercase().contains(q) || kitab.slug.lowercase().contains(q)
            }
        }
    }

    val filteredHadis = remember(state.hadisItems, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) {
            state.hadisItems
        } else {
            state.hadisItems.filter { hadis ->
                hadis.number.toString().contains(q) ||
                    hadis.translationId.lowercase().contains(q) ||
                    hadis.arabicText.lowercase().contains(q)
            }
        }
    }

    if (showKitabSheet) {
        KitabPickerSheet(
            query = kitabQuery,
            onQueryChange = { kitabQuery = it },
            kitabList = filteredKitab,
            selectedSlug = state.selectedCollection?.slug,
            isGrid = kitabLayout == PickerLayout.GRID.name,
            onToggleLayout = { isGrid ->
                kitabLayout = if (isGrid) PickerLayout.GRID.name else PickerLayout.LIST.name
                layoutStore.setHadisKitabLayout(kitabLayout)
            },
            onSelectKitab = { slug ->
                showKitabSheet = false
                vm.selectCollection(slug)
            },
            onDismiss = { showKitabSheet = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenIntroCard(
                title = "Hadis",
                subtitle = "Pilih kitab hadis lalu baca teks Arab dan terjemah Indonesia."
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    com.yaumi.app.ui.theme.Lavender200
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val selected = state.selectedCollection
                    Text(
                        text = "Kitab Hadis",
                        style = MaterialTheme.typography.labelMedium,
                        color = com.yaumi.app.ui.theme.Ink500
                    )
                    Text(
                        text = selected?.name ?: "Belum dipilih",
                        style = MaterialTheme.typography.titleLarge,
                        color = com.yaumi.app.ui.theme.Ink900,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    if (selected != null) {
                        Text(
                            text = "${selected.total} hadis",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.yaumi.app.ui.theme.Ink500
                        )
                    }

                    AssistChip(
                        onClick = { showKitabSheet = true },
                        label = { Text("Daftar Kitab") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                        }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text("Cari hadis") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.isLoading) {
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                }
            }
        }

        state.errorMessage?.let { err ->
            item { Text(err, color = MaterialTheme.colorScheme.error) }
        }

        items(filteredHadis.take(state.visibleCount)) { hadis ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hadis ${hadis.number}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(hadis.arabicText, style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider()
                    Text(hadis.translationId, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (state.visibleCount < filteredHadis.size) {
            item {
                Button(onClick = vm::loadMore, modifier = Modifier.fillMaxWidth()) {
                    Text("Muat Lagi")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitabPickerSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    kitabList: List<HadisCollection>,
    selectedSlug: String?,
    isGrid: Boolean,
    onToggleLayout: (Boolean) -> Unit,
    onSelectKitab: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Kitab",
                    style = MaterialTheme.typography.titleMedium
                )
                IconToggleButton(
                    checked = isGrid,
                    onCheckedChange = onToggleLayout
                ) {
                    Icon(
                        imageVector = if (isGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGrid) "Ubah ke list" else "Ubah ke grid"
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                label = { Text("Cari kitab") },
                modifier = Modifier.fillMaxWidth()
            )

            if (isGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(kitabList, key = { it.slug }) { kitab ->
                        val isSelected = kitab.slug == selectedSlug
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectKitab(kitab.slug) },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    kitab.name,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    "Total: ${kitab.total}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(kitabList, key = { it.slug }) { kitab ->
                        val isSelected = kitab.slug == selectedSlug
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectKitab(kitab.slug) },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        kitab.name,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Text(
                                        "Total: ${kitab.total}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
