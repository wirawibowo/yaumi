package com.yaumi.app.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaumi.app.ui.components.ScreenIntroCard

@Composable
fun SettingsRoute(contentPadding: PaddingValues) {
    val vm: SettingsViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val audioOptions = listOf(
        "azan_madinah.mp3" to "Azan Madinah",
        "azan_mekah.mp3" to "Azan Mekah",
        "azan_subuh.mp3" to "Azan Subuh"
    )

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
                title = "Pengaturan",
                subtitle = "Atur perilaku fitur inti agar pengalaman aplikasi tetap konsisten."
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Azan Global", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    RowSwitch("Notifikasi Azan", state.azanSettings.notificationEnabled, vm::setNotificationEnabled)
                    RowSwitch("Service latar belakang", state.azanSettings.backgroundServiceEnabled, vm::setBackgroundServiceEnabled)
                    RowSwitch("Reminder sebelum Azan", state.azanSettings.reminderEnabled, vm::setReminderEnabled)
                    RowSwitch("Audio saat Azan", state.azanSettings.audioEnabled, vm::setAudioEnabled)

                    Text("Volume Audio Azan: ${state.azanSettings.audioVolumePercent}%")
                    Slider(
                        value = state.azanSettings.audioVolumePercent.toFloat(),
                        onValueChange = { vm.setAudioVolumePercent(it.toInt()) },
                        valueRange = 0f..100f
                    )

                    Button(
                        onClick = vm::stopAzanAudio,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stop Audio Azan")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lokasi Jadwal Sholat", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    RowSwitch(
                        "Gunakan lokasi manual",
                        state.azanSettings.useManualLocation,
                        vm::setUseManualLocation
                    )

                    LocationDropdown(
                        label = "Provinsi",
                        options = state.availableProvinces,
                        selected = state.azanSettings.selectedProvince,
                        enabled = state.azanSettings.useManualLocation
                    ) { vm.setSelectedProvince(it) }

                    LocationDropdown(
                        label = "Kab/Kota",
                        options = state.availableKabkota,
                        selected = state.azanSettings.selectedKabkota,
                        enabled = state.azanSettings.useManualLocation && state.azanSettings.selectedProvince.isNotBlank()
                    ) { vm.setSelectedKabkota(it) }

                    if (state.locationLoading) {
                        Text("Memuat data lokasi...", style = MaterialTheme.typography.bodySmall)
                    }
                    state.locationError?.let { err ->
                        Text(err, color = MaterialTheme.colorScheme.error)
                    }
                    if (!state.azanSettings.useManualLocation) {
                        Text("Saat ini memakai lokasi perangkat.")
                    }
                    if (state.azanSettings.useManualLocation && state.azanSettings.selectedKabkota.isNotBlank()) {
                        Text("Lokasi manual aktif: ${state.azanSettings.selectedKabkota}, ${state.azanSettings.selectedProvince}")
                    }
                }
            }
        }

        item {
            AudioPickerCard(
                title = "Suara Azan",
                subtitle = "Dipakai untuk Dzuhur, Ashar, Maghrib, Isya",
                options = audioOptions,
                selected = state.azanSettings.selectedAudio,
                onSelect = vm::setSelectedAudio,
                onPreview = vm::previewAudio,
                onStopPreview = vm::stopAzanAudio,
                isPlaying = state.isAzanAudioPlaying
            )
        }

        item {
            Button(onClick = vm::resetDefaults, modifier = Modifier.fillMaxWidth()) {
                Text("Reset Pengaturan Azan ke Default")
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tadabur Harian", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    RowSwitch(
                        "Notifikasi Tadabur",
                        state.tadaburSettings.notificationEnabled,
                        vm::setTadaburNotificationEnabled
                    )
                    Text("Offset setelah Subuh: ${state.tadaburSettings.offsetMinutesAfterFajr} menit")
                    Slider(
                        value = state.tadaburSettings.offsetMinutesAfterFajr.toFloat(),
                        onValueChange = { vm.setTadaburOffsetMinutes(it.toInt()) },
                        valueRange = 5f..180f
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioPickerCard(
    title: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onPreview: (String) -> Unit,
    onStopPreview: () -> Unit,
    isPlaying: Boolean
) {
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = com.yaumi.app.ui.theme.Ink500
            )
            HorizontalDivider()
            options.forEach { (file, label) ->
                val isSelected = selected == file
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(file) }
                        )
                        Text(label)
                    }
                    androidx.compose.material3.TextButton(
                        onClick = {
                            if (isPlaying) onStopPreview() else onPreview(file)
                        }
                    ) {
                        Text(if (isPlaying && isSelected) "Stop" else "Putar")
                    }
                }
            }
        }
    }
}

@Composable
private fun RowSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun LocationDropdown(
    label: String,
    options: List<String>,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selected.isBlank()) "Pilih $label" else selected,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { if (enabled) expanded = true }, enabled = enabled) {
                Text("Pilih")
            }
        }
        if (enabled && options.isEmpty()) {
            Text("Data belum tersedia", style = MaterialTheme.typography.bodySmall)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 4.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}
