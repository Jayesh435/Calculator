package com.example.esp32controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esp32controller.ui.theme.ESP32ControllerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(this, "Location permission is required for WiFi scanning", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkAndRequestPermissions()
        
        setContent {
            ESP32ControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ESP32ControllerApp()
                }
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ESP32ControllerApp(viewModel: ESP32ViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var esp32Ip by remember { mutableStateOf("192.168.4.1") }
    var esp32Port by remember { mutableStateOf("80") }
    var customCommand by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "ESP32 Controller",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Connection Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Connection Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                OutlinedTextField(
                    value = esp32Ip,
                    onValueChange = { esp32Ip = it },
                    label = { Text("ESP32 IP Address") },
                    placeholder = { Text("192.168.4.1") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = esp32Port,
                    onValueChange = { esp32Port = it },
                    label = { Text("Port") },
                    placeholder = { Text("80") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setESP32Address(esp32Ip, esp32Port.toIntOrNull() ?: 80)
                            scope.launch {
                                viewModel.testConnection()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                size = 16.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Test Connection")
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.getWiFiInfo(context)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WiFi Info")
                    }
                }
            }
        }
        
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isConnected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (uiState.isConnected) "Connected" else "Disconnected",
                    color = if (uiState.isConnected) Color(0xFF1B5E20) else Color(0xFFD32F2F)
                )
            }
        }
        
        // Control Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Controls",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Predefined control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.sendCommand("LED_ON")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.isConnected && !uiState.isLoading
                    ) {
                        Text("LED ON")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.sendCommand("LED_OFF")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.isConnected && !uiState.isLoading
                    ) {
                        Text("LED OFF")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.sendCommand("STATUS")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.isConnected && !uiState.isLoading
                    ) {
                        Text("Get Status")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.sendCommand("SENSOR")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.isConnected && !uiState.isLoading
                    ) {
                        Text("Read Sensor")
                    }
                }
                
                // Custom command
                OutlinedTextField(
                    value = customCommand,
                    onValueChange = { customCommand = it },
                    label = { Text("Custom Command") },
                    placeholder = { Text("Enter custom command") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Button(
                    onClick = {
                        if (customCommand.isNotBlank()) {
                            scope.launch {
                                viewModel.sendCommand(customCommand)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isConnected && !uiState.isLoading && customCommand.isNotBlank()
                ) {
                    Text("Send Custom Command")
                }
            }
        }
        
        // Response Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Response Log",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    TextButton(
                        onClick = { viewModel.clearLog() }
                    ) {
                        Text("Clear")
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (uiState.responseLog.isEmpty()) {
                            Text(
                                text = "No responses yet...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.responseLog.forEach { response ->
                                Text(
                                    text = response,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}