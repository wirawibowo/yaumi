package com.yaumi.app.azan.ui

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yaumi.app.ui.components.ScreenIntroCard

@Composable
fun AzanRoute(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val vm: AzanViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    var locationPermissionDenied by remember { mutableStateOf(false) }
    var notificationPermissionDenied by remember { mutableStateOf(false) }

    val finePermission = Manifest.permission.ACCESS_FINE_LOCATION
    val coarsePermission = Manifest.permission.ACCESS_COARSE_LOCATION
    val notifPermission = Manifest.permission.POST_NOTIFICATIONS

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[finePermission] == true || result[coarsePermission] == true
        locationPermissionDenied = !granted
        vm.refresh()
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionDenied = !granted
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, finePermission) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, coarsePermission) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            locationLauncher.launch(arrayOf(finePermission, coarsePermission))
        } else {
            locationPermissionDenied = false
        }

        if (Build.VERSION.SDK_INT >= 33) {
            val notifGranted = ContextCompat.checkSelfPermission(context, notifPermission) == PackageManager.PERMISSION_GRANTED
            if (!notifGranted) {
                notifLauncher.launch(notifPermission)
            } else {
                notificationPermissionDenied = false
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
                title = "Azan & Jadwal Sholat",
                subtitle = "Lihat jadwal harian. Pengaturan azan ada di menu Pengaturan."
            )
        }

        item {
            Button(onClick = { vm.refresh() }) {
                Text("Refresh Jadwal")
            }
        }

        if (locationPermissionDenied) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Izin lokasi ditolak", color = MaterialTheme.colorScheme.error)
                        Text("Jadwal tetap tersedia memakai lokasi fallback (Jakarta).")
                    }
                }
            }
        }

        if (notificationPermissionDenied) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Izin notifikasi ditolak", color = MaterialTheme.colorScheme.error)
                        Text("Jadwal tetap tampil, namun notifikasi azan tidak bisa dikirim.")
                    }
                }
            }
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

        state.data?.let { data ->
            if (data.isFallback) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Mode fallback aktif", color = MaterialTheme.colorScheme.error)
                            Text(data.fallbackReason ?: "Alasan tidak tersedia")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(data.cityName, style = MaterialTheme.typography.titleMedium)
                        Text(data.dateReadable)
                        if (data.hijriReadable.isNotBlank()) Text(data.hijriReadable)
                        if (data.timezone.isNotBlank()) Text(data.timezone)
                    }
                }
            }

            items(data.timings) { timing ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(timing.name, style = MaterialTheme.typography.titleMedium)
                        Text(timing.time, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
