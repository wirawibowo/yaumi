package com.yaumi.app.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume

data class ResolvedLocation(
    val lat: Double,
    val lon: Double,
    val cityName: String,
    val provinceName: String
)

class DeviceLocationResolver(private val appContext: Context) {
    @SuppressLint("MissingPermission")
    suspend fun resolve(defaultLat: Double, defaultLon: Double): ResolvedLocation {
        val fused = LocationServices.getFusedLocationProviderClient(appContext)
        val location = suspendCancellableCoroutine { cont ->
            fused.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

        val lat = location?.latitude ?: defaultLat
        val lon = location?.longitude ?: defaultLon
        val place = resolvePlaceName(lat, lon)
        return ResolvedLocation(lat = lat, lon = lon, cityName = place.first, provinceName = place.second)
    }

    private fun resolvePlaceName(lat: Double, lon: Double): Pair<String, String> {
        return runCatching {
            val geocoder = Geocoder(appContext, Locale.getDefault())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                val result = CompletableFuture<Pair<String, String>>()
                geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val place = addresses.firstOrNull()
                        val city = place?.subAdminArea ?: place?.locality ?: "Lokasi Terdeteksi"
                        val province = place?.adminArea ?: ""
                        result.complete(city to province)
                    }

                    override fun onError(errorMessage: String?) {
                        result.complete("Lokasi Terdeteksi" to "")
                    }
                })
                result.get()
            } else {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocation(lat, lon, 1)
                val place = list?.firstOrNull()
                val city = place?.subAdminArea ?: place?.locality ?: "Lokasi Terdeteksi"
                val province = place?.adminArea ?: ""
                city to province
            }
        }.getOrDefault("Lokasi Terdeteksi" to "")
    }
}
