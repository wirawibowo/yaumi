package com.yaumi.app.azan.data

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AzanApiService {
    fun getProvinces(): String {
        val url = URL("$BASE_URL/provinsi")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
        }
        connection.inputStream.bufferedReader().use { return it.readText() }
    }

    fun getKabkota(province: String): String {
        val url = URL("$BASE_URL/kabkota")
        return postJson(url, "{\"provinsi\":\"$province\"}")
    }

    fun getMonthlySchedule(province: String, kabkota: String, month: Int, year: Int): String {
        val url = URL(BASE_URL)
        val body = "{\"provinsi\":\"$province\",\"kabkota\":\"$kabkota\",\"bulan\":$month,\"tahun\":$year}"
        return postJson(url, body)
    }

    private fun postJson(url: URL, body: String): String {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(body)
            writer.flush()
        }

        connection.inputStream.bufferedReader().use { return it.readText() }
    }

    private companion object {
        private const val BASE_URL = "https://equran.id/api/v2/shalat"
        private const val TIMEOUT_MS = 8000
    }
}
