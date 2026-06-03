package com.yaumi.app.quran.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaumi.app.quran.data.QuranReadingProgressStore
import com.yaumi.app.quran.data.QuranReciter
import com.yaumi.app.ui.data.PickerLayoutStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

private enum class SurahSheetLayout {
    LIST,
    GRID
}

@Composable
fun QuranRoute(contentPadding: PaddingValues) {
    val viewModel: QuranViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuranScreen(
        state = state,
        contentPadding = contentPadding,
        onSelectSurah = viewModel::selectSurah,
        onToggleBookmark = viewModel::toggleBookmark,
        onToggleSurahAudio = viewModel::toggleSurahAudio,
        onToggleAyahAudio = viewModel::toggleAyahAudio,
        onStopAudio = viewModel::stopAudio,
        onSelectReciter = viewModel::setReciter
    )
}

@Composable
private fun QuranScreen(
    state: QuranUiState,
    contentPadding: PaddingValues,
    onSelectSurah: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onToggleSurahAudio: () -> Unit,
    onToggleAyahAudio: (Int) -> Unit,
    onStopAudio: () -> Unit,
    onSelectReciter: (String) -> Unit
) {
    var showReciterSheet by rememberSaveable { mutableStateOf(false) }
    if (showReciterSheet) {
        ReciterPickerSheet(
            reciters = state.availableReciters,
            selectedId = state.reciterId,
            onSelect = { id ->
                onSelectReciter(id)
                showReciterSheet = false
            },
            onDismiss = { showReciterSheet = false }
        )
    }
    val context = LocalContext.current
    val progressStore = remember { QuranReadingProgressStore(context.applicationContext) }
    val layoutStore = remember { PickerLayoutStore(context.applicationContext) }
    val listState = rememberLazyListState()
    var showSurahSheet by rememberSaveable { mutableStateOf(false) }
    var surahQuery by rememberSaveable { mutableStateOf("") }
    var surahSheetLayout by rememberSaveable {
        mutableStateOf(layoutStore.getQuranSurahLayout(SurahSheetLayout.LIST.name))
    }
    val filteredSurah = remember(state.surahList, surahQuery) {
        val query = surahQuery.trim().lowercase()
        if (query.isBlank()) {
            state.surahList
        } else {
            state.surahList.filter { surah ->
                surah.id.toString().contains(query) ||
                    surah.latinName.lowercase().contains(query) ||
                    surah.translation.lowercase().contains(query)
            }
        }
    }
    val selectedSurah = state.selectedSurah
    val selectedIndex = state.surahList.indexOfFirst { it.id == selectedSurah?.id }
    val previousSurahId = if (selectedIndex > 0) {
        state.surahList[selectedIndex - 1].id
    } else {
        null
    }
    val nextSurahId = if (selectedIndex in 0 until state.surahList.lastIndex) {
        state.surahList[selectedIndex + 1].id
    } else {
        null
    }
    val ayahStartIndex = 1 +
        (if (state.isLoading) 1 else 0) +
        (if (state.errorMessage != null) 1 else 0) +
        (if (state.audioErrorMessage != null) 1 else 0)

    LaunchedEffect(selectedSurah?.id, state.ayahLines) {
        val surahId = selectedSurah?.id ?: return@LaunchedEffect
        if (state.ayahLines.isEmpty()) return@LaunchedEffect
        val savedAyah = progressStore.getLastReadAyahNumber(surahId)
        val ayahIndex = savedAyah
            ?.let { ayah -> state.ayahLines.indexOfFirst { it.number == ayah } }
            ?.takeIf { it >= 0 }
            ?: 0
        listState.scrollToItem(ayahStartIndex + ayahIndex)
    }

    LaunchedEffect(listState, selectedSurah?.id, state.ayahLines, ayahStartIndex) {
        val surahId = selectedSurah?.id ?: return@LaunchedEffect
        if (state.ayahLines.isEmpty()) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { itemIndex -> itemIndex - ayahStartIndex }
            .filter { ayahIndex -> ayahIndex in state.ayahLines.indices }
            .distinctUntilChanged()
            .collect { ayahIndex ->
                progressStore.setLastReadAyahNumber(surahId, state.ayahLines[ayahIndex].number)
            }
    }

    if (showSurahSheet) {
        SurahPickerSheet(
            query = surahQuery,
            onQueryChange = { surahQuery = it },
            surahList = filteredSurah,
            selectedSurahId = state.selectedSurah?.id,
            isGrid = surahSheetLayout == SurahSheetLayout.GRID.name,
            onToggleLayout = { isGrid ->
                surahSheetLayout = if (isGrid) {
                    SurahSheetLayout.GRID.name
                } else {
                    SurahSheetLayout.LIST.name
                }
                layoutStore.setQuranSurahLayout(surahSheetLayout)
            },
            onSelectSurah = { id ->
                showSurahSheet = false
                onSelectSurah(id)
            },
            onDismiss = { showSurahSheet = false }
        )
    }

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
    ) {
        // Toolbar: Daftar Surah + Pilih Qari (separated from ayat content)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { showSurahSheet = true },
                label = { Text("Daftar Surah") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.weight(1f)
            )
            val reciter = state.availableReciters.firstOrNull { it.id == state.reciterId }
            AssistChip(
                onClick = { showReciterSheet = true },
                label = { Text(reciter?.displayName ?: "Pilih Qari") },
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Surah terpilih", style = MaterialTheme.typography.titleMedium)
                        val selected = selectedSurah
                        if (selected == null) {
                            Text(text = "Belum ada data", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(text = "${selected.id}. ${selected.latinName} (${selected.arabicName})")
                            Text(text = "${selected.location} - ${selected.ayahCount} ayat")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = onToggleSurahAudio,
                                    enabled = !state.isSurahAudioLoading,
                                    label = {
                                        val isThisSurah = state.audioSurahId == selected.id
                                        val label = when {
                                            state.isSurahAudioLoading && isThisSurah -> "Memuat audio..."
                                            state.isSurahAudioPlaying && isThisSurah -> "Pause audio surah"
                                            else -> "Play audio surah"
                                        }
                                        Text(label)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )

                                if (state.isSurahAudioLoading && state.audioSurahId == selected.id) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }

        if (state.isLoading) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        state.errorMessage?.let { error ->
            item {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }

        state.audioErrorMessage?.let { error ->
            item {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }

        items(state.ayahLines) { ayah ->
            var tafsirExpanded by rememberSaveable(ayah.number) { mutableStateOf(false) }
            val isActiveAyah = state.activeAyahNumber == ayah.number

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActiveAyah) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ayat ${ayah.number}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isActiveAyah) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        val bookmarked = state.bookmarkedAyahNumbers.contains(ayah.number)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isThisAyah = state.activeAyahNumber == ayah.number
                            val isPlayingThisAyah = isThisAyah && state.isSurahAudioPlaying

                            IconButton(onClick = { onToggleAyahAudio(ayah.number) }) {
                                Icon(
                                    imageVector = if (isPlayingThisAyah) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlayingThisAyah) {
                                        "Pause ayat ${ayah.number}"
                                    } else {
                                        "Play ayat ${ayah.number}"
                                    }
                                )
                            }
                            IconButton(
                                onClick = onStopAudio,
                                enabled = state.activeAyahNumber != null
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop audio")
                            }
                            IconButton(onClick = { onToggleBookmark(ayah.number) }) {
                                Icon(
                                    imageVector = if (bookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Toggle bookmark"
                                )
                            }
                        }
                    }
                    Text(text = ayah.arabicText, style = MaterialTheme.typography.headlineSmall)
                    HorizontalDivider()
                    Text(text = ayah.translationId, style = MaterialTheme.typography.bodyMedium)
                    if (!ayah.tafsirText.isNullOrBlank()) {
                        AssistChip(
                            onClick = { tafsirExpanded = !tafsirExpanded },
                            label = {
                                Text(if (tafsirExpanded) "Sembunyikan Tafsir" else "Lihat Tafsir")
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        AnimatedVisibility(visible = tafsirExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Tafsir", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    text = ayah.tafsirText,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedSurah != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { previousSurahId?.let(onSelectSurah) },
                        enabled = previousSurahId != null,
                        label = { Text("Prev") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = null
                            )
                        }
                    )
                    AssistChip(
                        onClick = { nextSurahId?.let(onSelectSurah) },
                        enabled = nextSurahId != null,
                        label = { Text("Next") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReciterPickerSheet(
    reciters: List<QuranReciter>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Pilih Qari",
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reciters, key = { it.id }) { reciter ->
                    val isSelected = reciter.id == selectedId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(reciter.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = reciter.displayName,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahPickerSheet(
    query: String,
    onQueryChange: (String) -> Unit,
    surahList: List<com.yaumi.app.quran.domain.model.SurahSummary>,
    selectedSurahId: Int?,
    isGrid: Boolean,
    onToggleLayout: (Boolean) -> Unit,
    onSelectSurah: (Int) -> Unit,
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
                    text = "Daftar Surah",
                    style = MaterialTheme.typography.titleMedium
                )
                IconToggleButton(
                    checked = isGrid,
                    onCheckedChange = onToggleLayout
                ) {
                    Icon(
                        imageVector = if (isGrid) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGrid) "Ubah ke list" else "Ubah ke grid"
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                label = { Text("Cari surah") },
                modifier = Modifier.fillMaxWidth()
            )

            if (isGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(surahList, key = { it.id }) { surah ->
                        val isSelected = selectedSurahId == surah.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSurah(surah.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("${surah.id}. ${surah.latinName}")
                                Text(
                                    surah.translation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(surahList, key = { it.id }) { surah ->
                        val isSelected = selectedSurahId == surah.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSurah(surah.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("${surah.id}. ${surah.latinName}")
                                Text(
                                    surah.translation,
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
