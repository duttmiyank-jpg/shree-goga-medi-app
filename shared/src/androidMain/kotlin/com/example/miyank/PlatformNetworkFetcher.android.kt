package com.example.miyank

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun fetchUrlContent(urlString: String, timeoutMs: Int): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = timeoutMs
        conn.readTimeout = timeoutMs
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) TempleApp/1.0")
        conn.setRequestProperty("Accept", "application/json")

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            conn.disconnect()
            sb.toString()
        } else {
            conn.disconnect()
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
