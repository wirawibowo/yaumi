package com.yaumi.app.doa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaumi.app.ui.components.ScreenIntroCard

@Composable
fun DoaRoute(contentPadding: PaddingValues) {
    val vm: DoaViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val filteredDoa = remember(state.doaItems, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) {
            state.doaItems
        } else {
            state.doaItems.filter {
                it.title.lowercase().contains(q) ||
                    it.translation.lowercase().contains(q) ||
                    it.latinText.lowercase().contains(q)
            }
        }
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
                title = "Doa Harian",
                subtitle = "Kumpulan doa dengan teks Arab, latin, dan terjemahan."
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                label = { Text("Cari doa") },
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

        items(filteredDoa) { doa ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(doa.title, style = MaterialTheme.typography.titleMedium)
                    Text(doa.arabicText, style = MaterialTheme.typography.bodyLarge)
                    Text(doa.latinText, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider()
                    Text(doa.translation, style = MaterialTheme.typography.bodyMedium)
                    if (doa.source.isNotBlank()) {
                        Text(doa.source, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
