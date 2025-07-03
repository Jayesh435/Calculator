package com.example.esp32controller

import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ESP32UiState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val responseLog: List<String> = emptyList(),
    val lastResponse: String = "",
    val errorMessage: String? = null
)

class ESP32ViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ESP32UiState())
    val uiState: StateFlow<ESP32UiState> = _uiState.asStateFlow()
    
    private var esp32Service: ESP32NetworkService? = null
    private var esp32Ip: String = "192.168.4.1"
    private var esp32Port: Int = 80
    
    init {
        esp32Service = ESP32NetworkService()
    }
    
    fun setESP32Address(ip: String, port: Int) {
        esp32Ip = ip
        esp32Port = port
        esp32Service?.updateBaseUrl("http://$ip:$port")
    }
    
    suspend fun testConnection() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        try {
            val response = esp32Service?.testConnection() ?: "No response"
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] Connection test: $response"
            
            _uiState.value = _uiState.value.copy(
                isConnected = response.contains("ESP32") || response.contains("OK"),
                isLoading = false,
                responseLog = _uiState.value.responseLog + logEntry,
                lastResponse = response
            )
        } catch (e: Exception) {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] Connection failed: ${e.message}"
            
            _uiState.value = _uiState.value.copy(
                isConnected = false,
                isLoading = false,
                responseLog = _uiState.value.responseLog + logEntry,
                errorMessage = e.message
            )
        }
    }
    
    suspend fun sendCommand(command: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        try {
            val response = esp32Service?.sendCommand(command) ?: "No response"
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] Command '$command': $response"
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                responseLog = _uiState.value.responseLog + logEntry,
                lastResponse = response
            )
        } catch (e: Exception) {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] Command '$command' failed: ${e.message}"
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                responseLog = _uiState.value.responseLog + logEntry,
                errorMessage = e.message
            )
        }
    }
    
    suspend fun getWiFiInfo(context: Context) {
        viewModelScope.launch {
            try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val ssid = wifiInfo.ssid?.replace("\"", "") ?: "Unknown"
                val ip = android.text.format.Formatter.formatIpAddress(wifiInfo.ipAddress)
                
                val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val logEntry = "[$timestamp] WiFi Info - SSID: $ssid, IP: $ip"
                
                _uiState.value = _uiState.value.copy(
                    responseLog = _uiState.value.responseLog + logEntry
                )
            } catch (e: Exception) {
                val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val logEntry = "[$timestamp] WiFi Info failed: ${e.message}"
                
                _uiState.value = _uiState.value.copy(
                    responseLog = _uiState.value.responseLog + logEntry
                )
            }
        }
    }
    
    fun clearLog() {
        _uiState.value = _uiState.value.copy(responseLog = emptyList())
    }
}