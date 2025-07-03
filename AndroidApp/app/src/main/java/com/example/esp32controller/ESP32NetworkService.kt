package com.example.esp32controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ESP32NetworkService {
    private var baseUrl: String = "http://192.168.4.1:80"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    fun updateBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }
    
    suspend fun testConnection(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/")
            .get()
            .build()
        
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: "Connected to ESP32"
            } else {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            throw IOException("Connection failed: ${e.message}")
        }
    }
    
    suspend fun sendCommand(command: String): String = withContext(Dispatchers.IO) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = """{"command":"$command"}""".toRequestBody(jsonMediaType)
        
        val request = Request.Builder()
            .url("$baseUrl/command")
            .post(requestBody)
            .build()
        
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: "Command sent successfully"
            } else {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            throw IOException("Command failed: ${e.message}")
        }
    }
    
    suspend fun sendGetRequest(endpoint: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/$endpoint")
            .get()
            .build()
        
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: "Request successful"
            } else {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            throw IOException("Request failed: ${e.message}")
        }
    }
    
    suspend fun sendPostData(endpoint: String, data: Map<String, Any>): String = withContext(Dispatchers.IO) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val jsonData = data.map { "\"${it.key}\":\"${it.value}\"" }.joinToString(",", "{", "}")
        val requestBody = jsonData.toRequestBody(jsonMediaType)
        
        val request = Request.Builder()
            .url("$baseUrl/$endpoint")
            .post(requestBody)
            .build()
        
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: "Data sent successfully"
            } else {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            throw IOException("Data send failed: ${e.message}")
        }
    }
}