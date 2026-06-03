package com.yaumi.app.azan.data

import android.annotation.SuppressLint
import android.content.Context
import com.yaumi.app.azan.domain.model.AzanUiData
import com.yaumi.app.azan.domain.model.PrayerTiming
import com.yaumi.app.core.location.DeviceLocationResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AzanRepository(private val appContext: Context) {
    private val api = AzanApiService()
    private val locationResolver = DeviceLocationResolver(appContext)
    private val mapper = IndonesiaLocationMapper()

    suspend fun fetchProvinces(): List<String> = withContext(Dispatchers.IO) {
        val raw = api.getProvinces()
        parseStringList(JSONObject(raw).optJSONArray("data"))
    }

    suspend fun fetchKabkota(province: String): List<String> = withContext(Dispatchers.IO) {
        val raw = api.getKabkota(province)
        parseStringList(JSONObject(raw).optJSONArray("data"))
    }

    @SuppressLint("MissingPermission")
    suspend fun loadAzanData(): AzanUiData = withContext(Dispatchers.IO) {
        val settings = AzanSettingsStore(appContext).load()
        val resolved = locationResolver.resolve(defaultLat = -6.2088, defaultLon = 106.8456)
        val (city, province) = if (settings.useManualLocation &&
            settings.selectedProvince.isNotBlank() &&
            settings.selectedKabkota.isNotBlank()
        ) {
            settings.selectedKabkota to settings.selectedProvince
        } else {
            resolved.cityName to resolved.provinceName
        }
        val today = LocalDate.now()
        val rawProvinces = runCatching { api.getProvinces() }
            .getOrElse { return@withContext fallbackData(city, "Asia/Jakarta", "Network/API error") }

        val provinces = parseStringList(JSONObject(rawProvinces).optJSONArray("data"))
        if (provinces.isEmpty()) {
            return@withContext fallbackData(city, "Asia/Jakarta", "API returned empty provinces")
        }

        val matchedProvince = mapper.matchProvince(province, provinces)
            ?: return@withContext fallbackData(city, "Asia/Jakarta", "Provinsi tidak ditemukan")

        val rawKabkota = runCatching { api.getKabkota(matchedProvince) }
            .getOrElse { return@withContext fallbackData(city, "Asia/Jakarta", "Network/API error") }

        val kabkotaList = parseStringList(JSONObject(rawKabkota).optJSONArray("data"))
        if (kabkotaList.isEmpty()) {
            return@withContext fallbackData(matchedProvince, "Asia/Jakarta", "API returned empty kab/kota")
        }

        val matchedKabkota = mapper.matchKabkota(city, kabkotaList)
            ?: return@withContext fallbackData(city, "Asia/Jakarta", "Kab/Kota tidak ditemukan")

        val rawSchedule = runCatching {
            api.getMonthlySchedule(matchedProvince, matchedKabkota, today.monthValue, today.year)
        }.getOrElse { return@withContext fallbackData(matchedKabkota, "Asia/Jakarta", "Network/API error") }

        val root = JSONObject(rawSchedule)
        val dataObj = root.optJSONObject("data") ?: JSONObject()
        val schedule = dataObj.optJSONArray("jadwal")
            ?: return@withContext fallbackData(matchedKabkota, "Asia/Jakarta", "API returned empty schedule")

        val todayItem = (0 until schedule.length())
            .mapNotNull { index -> schedule.optJSONObject(index) }
            .firstOrNull { it.optString("tanggal_lengkap") == today.toString() }
            ?: return@withContext fallbackData(matchedKabkota, "Asia/Jakarta", "Jadwal hari ini tidak ditemukan")

        val timings = listOf(
            PrayerTiming("Imsak", todayItem.optString("imsak")),
            PrayerTiming("Fajr", todayItem.optString("subuh")),
            PrayerTiming("Sunrise", todayItem.optString("terbit")),
            PrayerTiming("Dhuhr", todayItem.optString("dzuhur")),
            PrayerTiming("Asr", todayItem.optString("ashar")),
            PrayerTiming("Maghrib", todayItem.optString("maghrib")),
            PrayerTiming("Isha", todayItem.optString("isya"))
        )

        AzanUiData(
            cityName = matchedKabkota,
            timezone = "Asia/Jakarta",
            dateReadable = today.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())),
            hijriReadable = computeLocalHijri(),
            timings = timings,
            provinceName = matchedProvince,
            isFallback = false,
            fallbackReason = null
        )
    }

    private fun computeLocalHijri(): String = runCatching {
        val cal = android.icu.util.IslamicCalendar()
        cal.time = Date()
        val day = cal.get(android.icu.util.Calendar.DATE)
        val monthNumber = cal.get(android.icu.util.Calendar.MONTH) + 1
        val year = cal.get(android.icu.util.Calendar.YEAR)
        val monthName = indonesianHijriMonth(monthNumber)
        "$day $monthName $year"
    }.getOrDefault("-")

    private fun indonesianHijriMonth(number: Int): String = when (number) {
        1 -> "Muharam"
        2 -> "Safar"
        3 -> "Rabiulawal"
        4 -> "Rabiulakhir"
        5 -> "Jumadilawal"
        6 -> "Jumadilakhir"
        7 -> "Rajab"
        8 -> "Syakban"
        9 -> "Ramadan"
        10 -> "Syawal"
        11 -> "Zulkaidah"
        12 -> "Zulhijah"
        else -> ""
    }

    private fun fallbackData(city: String, timezone: String, reason: String): AzanUiData {
        val nowDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        return AzanUiData(
            cityName = city,
            timezone = timezone,
            dateReadable = nowDate,
            hijriReadable = computeLocalHijri(),
            timings = listOf(
                PrayerTiming("Imsak", "04:20"),
                PrayerTiming("Fajr", "04:30"),
                PrayerTiming("Sunrise", "05:45"),
                PrayerTiming("Dhuhr", "12:00"),
                PrayerTiming("Asr", "15:20"),
                PrayerTiming("Maghrib", "18:00"),
                PrayerTiming("Isha", "19:15")
            ),
            provinceName = "",
            isFallback = true,
            fallbackReason = reason
        )
    }

    private fun parseStringList(array: org.json.JSONArray?): List<String> {
        if (array == null) return emptyList()
        return (0 until array.length())
            .mapNotNull { index -> array.optString(index).takeIf { it.isNotBlank() } }
    }

}
