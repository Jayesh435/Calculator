#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <EEPROM.h>

// WiFi credentials
const char* ssid = "ESP32-Controller";
const char* password = "12345678";

// Create WebServer object on port 80
WebServer server(80);

// LED pin
const int ledPin = 2;

// Sensor pin (example: analog sensor)
const int sensorPin = A0;

// Variables to store sensor data
float temperature = 25.0;
bool ledState = false;
unsigned long lastSensorRead = 0;
const unsigned long sensorInterval = 1000; // Read sensor every second

void setup() {
  Serial.begin(115200);
  
  // Initialize LED pin
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  
  // Initialize EEPROM
  EEPROM.begin(512);
  
  // Set up WiFi Access Point
  WiFi.softAP(ssid, password);
  IPAddress IP = WiFi.softAPIP();
  
  Serial.println("ESP32 WiFi Controller Started");
  Serial.print("AP IP address: ");
  Serial.println(IP);
  Serial.println("SSID: " + String(ssid));
  Serial.println("Password: " + String(password));
  
  // Set up web server routes
  setupServerRoutes();
  
  // Start server
  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient();
  
  // Read sensor data periodically
  if (millis() - lastSensorRead >= sensorInterval) {
    readSensorData();
    lastSensorRead = millis();
  }
  
  delay(10);
}

void setupServerRoutes() {
  // Root route
  server.on("/", HTTP_GET, handleRoot);
  
  // Command route for POST requests
  server.on("/command", HTTP_POST, handleCommand);
  server.on("/command", HTTP_OPTIONS, handleCORS);
  
  // Status route
  server.on("/status", HTTP_GET, handleStatus);
  
  // Sensor route
  server.on("/sensor", HTTP_GET, handleSensor);
  
  // LED control routes
  server.on("/led/on", HTTP_GET, handleLedOn);
  server.on("/led/off", HTTP_GET, handleLedOff);
  server.on("/led/toggle", HTTP_GET, handleLedToggle);
  
  // WiFi info route
  server.on("/wifi", HTTP_GET, handleWiFiInfo);
  
  // Handle not found
  server.onNotFound(handleNotFound);
}

void handleRoot() {
  String html = R"(
    <!DOCTYPE html>
    <html>
    <head>
        <title>ESP32 Controller</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 20px; }
            .container { max-width: 600px; margin: 0 auto; }
            .button { background-color: #4CAF50; color: white; padding: 10px 20px; 
                     text-decoration: none; margin: 5px; display: inline-block; border-radius: 4px; }
            .button:hover { background-color: #45a049; }
            .status { background-color: #f0f0f0; padding: 10px; margin: 10px 0; border-radius: 4px; }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>ESP32 WiFi Controller</h1>
            <p>Device is ready to receive commands from Android app</p>
            
            <div class="status">
                <h3>Current Status:</h3>
                <p>LED: <span id="ledStatus">)" + (ledState ? "ON" : "OFF") + R"(</span></p>
                <p>Temperature: <span id="temp">)" + String(temperature, 1) + R"(°C</span></p>
                <p>Free Heap: )" + String(ESP.getFreeHeap()) + R"( bytes</p>
            </div>
            
            <h3>Quick Controls:</h3>
            <a href="/led/on" class="button">LED ON</a>
            <a href="/led/off" class="button">LED OFF</a>
            <a href="/led/toggle" class="button">Toggle LED</a>
            <a href="/sensor" class="button">Read Sensor</a>
            <a href="/status" class="button">Get Status</a>
        </div>
    </body>
    </html>
  )";
  
  setCORSHeaders();
  server.send(200, "text/html", html);
}

void handleCommand() {
  setCORSHeaders();
  
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    Serial.println("Received command: " + body);
    
    // Parse JSON
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, body);
    
    String command = doc["command"];
    String response = processCommand(command);
    
    // Send JSON response
    DynamicJsonDocument responseDoc(1024);
    responseDoc["status"] = "success";
    responseDoc["command"] = command;
    responseDoc["response"] = response;
    responseDoc["timestamp"] = millis();
    
    String jsonResponse;
    serializeJson(responseDoc, jsonResponse);
    
    server.send(200, "application/json", jsonResponse);
  } else {
    server.send(400, "application/json", "{\"error\":\"No command received\"}");
  }
}

String processCommand(String command) {
  command.toUpperCase();
  String response = "";
  
  if (command == "LED_ON") {
    digitalWrite(ledPin, HIGH);
    ledState = true;
    response = "LED turned ON";
    
  } else if (command == "LED_OFF") {
    digitalWrite(ledPin, LOW);
    ledState = false;
    response = "LED turned OFF";
    
  } else if (command == "LED_TOGGLE") {
    ledState = !ledState;
    digitalWrite(ledPin, ledState ? HIGH : LOW);
    response = "LED toggled to " + String(ledState ? "ON" : "OFF");
    
  } else if (command == "STATUS") {
    response = getStatusString();
    
  } else if (command == "SENSOR") {
    readSensorData();
    response = "Temperature: " + String(temperature, 1) + "°C";
    
  } else if (command == "RESTART") {
    response = "ESP32 restarting...";
    server.send(200, "application/json", "{\"response\":\"" + response + "\"}");
    delay(1000);
    ESP.restart();
    
  } else if (command.startsWith("DELAY_")) {
    int delayTime = command.substring(6).toInt();
    delay(delayTime);
    response = "Delayed for " + String(delayTime) + "ms";
    
  } else {
    response = "Unknown command: " + command;
  }
  
  return response;
}

void handleStatus() {
  setCORSHeaders();
  
  DynamicJsonDocument doc(1024);
  doc["device"] = "ESP32";
  doc["status"] = "online";
  doc["led_state"] = ledState;
  doc["temperature"] = temperature;
  doc["free_heap"] = ESP.getFreeHeap();
  doc["uptime"] = millis();
  doc["wifi_clients"] = WiFi.softAPgetStationNum();
  
  String jsonString;
  serializeJson(doc, jsonString);
  
  server.send(200, "application/json", jsonString);
}

void handleSensor() {
  setCORSHeaders();
  readSensorData();
  
  DynamicJsonDocument doc(512);
  doc["temperature"] = temperature;
  doc["timestamp"] = millis();
  doc["sensor_pin"] = sensorPin;
  
  String jsonString;
  serializeJson(doc, jsonString);
  
  server.send(200, "application/json", jsonString);
}

void handleLedOn() {
  setCORSHeaders();
  digitalWrite(ledPin, HIGH);
  ledState = true;
  server.send(200, "application/json", "{\"status\":\"LED ON\"}");
}

void handleLedOff() {
  setCORSHeaders();
  digitalWrite(ledPin, LOW);
  ledState = false;
  server.send(200, "application/json", "{\"status\":\"LED OFF\"}");
}

void handleLedToggle() {
  setCORSHeaders();
  ledState = !ledState;
  digitalWrite(ledPin, ledState ? HIGH : LOW);
  
  DynamicJsonDocument doc(256);
  doc["status"] = "LED " + String(ledState ? "ON" : "OFF");
  doc["led_state"] = ledState;
  
  String jsonString;
  serializeJson(doc, jsonString);
  
  server.send(200, "application/json", jsonString);
}

void handleWiFiInfo() {
  setCORSHeaders();
  
  DynamicJsonDocument doc(512);
  doc["ssid"] = ssid;
  doc["ip"] = WiFi.softAPIP().toString();
  doc["mac"] = WiFi.macAddress();
  doc["clients"] = WiFi.softAPgetStationNum();
  doc["channel"] = WiFi.channel();
  
  String jsonString;
  serializeJson(doc, jsonString);
  
  server.send(200, "application/json", jsonString);
}

void handleCORS() {
  setCORSHeaders();
  server.send(200, "text/plain", "");
}

void handleNotFound() {
  setCORSHeaders();
  server.send(404, "application/json", "{\"error\":\"Not found\"}");
}

void setCORSHeaders() {
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
}

void readSensorData() {
  // Simulate temperature reading (replace with actual sensor code)
  // For demonstration, we'll create a varying temperature
  static float baseTemp = 25.0;
  static unsigned long lastChange = 0;
  
  if (millis() - lastChange > 5000) { // Change every 5 seconds
    baseTemp += random(-20, 21) / 10.0; // ±2°C variation
    if (baseTemp < 10.0) baseTemp = 10.0;
    if (baseTemp > 40.0) baseTemp = 40.0;
    lastChange = millis();
  }
  
  temperature = baseTemp;
  
  // If you have a real sensor, replace the above with:
  // int sensorValue = analogRead(sensorPin);
  // temperature = (sensorValue * 3.3 / 4095.0) * 100.0; // Example conversion
}

String getStatusString() {
  String status = "ESP32 Status:\n";
  status += "LED: " + String(ledState ? "ON" : "OFF") + "\n";
  status += "Temperature: " + String(temperature, 1) + "°C\n";
  status += "Free Heap: " + String(ESP.getFreeHeap()) + " bytes\n";
  status += "Uptime: " + String(millis() / 1000) + " seconds\n";
  status += "WiFi Clients: " + String(WiFi.softAPgetStationNum());
  
  return status;
}