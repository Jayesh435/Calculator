# ESP32 Android WiFi Controller

A complete solution for controlling an ESP32 microcontroller via WiFi using an Android application. The ESP32 creates a WiFi access point that the Android app connects to for real-time communication.

## 📋 Overview

This project consists of two main components:
1. **Android Application**: A modern Android app built with Jetpack Compose that provides a user-friendly interface for controlling the ESP32
2. **ESP32 Firmware**: Arduino code that creates a WiFi access point and web server to handle commands from the Android app

## 🚀 Features

### Android App Features
- Modern Material 3 UI design
- WiFi connection management
- Real-time command sending
- Response logging
- Connection status monitoring
- Predefined controls (LED, sensors)
- Custom command support

### ESP32 Features
- WiFi Access Point mode
- Web server with JSON API
- LED control
- Sensor data simulation
- Status monitoring
- CORS support for web access
- Built-in web interface

## 📁 Project Structure

```
├── AndroidApp/
│   ├── app/
│   │   ├── build.gradle
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/esp32controller/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ESP32ViewModel.kt
│   │   │   │   ├── ESP32NetworkService.kt
│   │   │   │   └── ui/theme/
│   │   │   └── res/
│   │   └── ...
│   ├── build.gradle
│   └── settings.gradle
├── ESP32Code/
│   └── ESP32_WiFi_Server.ino
└── README.md
```

## 🛠️ Setup Instructions

### ESP32 Setup

1. **Hardware Requirements:**
   - ESP32 development board
   - LED (optional, built-in LED on pin 2 works)
   - Breadboard and jumper wires (optional)

2. **Software Requirements:**
   - Arduino IDE
   - ESP32 board package
   - Required libraries:
     - WiFi (included with ESP32 package)
     - WebServer (included with ESP32 package)
     - ArduinoJson (install via Library Manager)

3. **Installation Steps:**
   ```bash
   # Install Arduino IDE
   # Add ESP32 board manager URL: 
   # https://dl.espressif.com/dl/package_esp32_index.json
   
   # Install ArduinoJson library
   # Sketch -> Include Library -> Manage Libraries
   # Search for "ArduinoJson" and install
   ```

4. **Upload Code:**
   - Open `ESP32Code/ESP32_WiFi_Server.ino` in Arduino IDE
   - Select your ESP32 board and port
   - Upload the code

5. **ESP32 Configuration:**
   ```cpp
   // Default WiFi credentials in the code
   const char* ssid = "ESP32-Controller";
   const char* password = "12345678";
   ```

### Android App Setup

1. **Requirements:**
   - Android Studio (latest version)
   - Minimum SDK: API 24 (Android 7.0)
   - Target SDK: API 34 (Android 14)

2. **Installation Steps:**
   ```bash
   # Open Android Studio
   # File -> Open -> Select AndroidApp folder
   # Let Gradle sync complete
   # Run the app on device or emulator
   ```

3. **Permissions:**
   The app requires the following permissions:
   - `INTERNET`
   - `ACCESS_NETWORK_STATE`
   - `ACCESS_WIFI_STATE`
   - `CHANGE_WIFI_STATE`
   - `ACCESS_FINE_LOCATION`
   - `ACCESS_COARSE_LOCATION`

## 🔗 How to Connect

### Step 1: Start ESP32
1. Power on your ESP32 with the uploaded code
2. Monitor Serial output (115200 baud) to see:
   ```
   ESP32 WiFi Controller Started
   AP IP address: 192.168.4.1
   SSID: ESP32-Controller
   Password: 12345678
   HTTP server started
   ```

### Step 2: Connect Android Device
1. Go to WiFi settings on your Android device
2. Connect to network: `ESP32-Controller`
3. Password: `12345678`

### Step 3: Use the App
1. Open the ESP32 Controller app
2. The default IP `192.168.4.1` should work
3. Tap "Test Connection" to verify communication
4. Use the control buttons to interact with ESP32

## 📡 API Endpoints

The ESP32 provides several HTTP endpoints:

### GET Endpoints
- `/` - Web interface
- `/status` - Get device status
- `/sensor` - Read sensor data
- `/led/on` - Turn LED on
- `/led/off` - Turn LED off
- `/led/toggle` - Toggle LED state
- `/wifi` - Get WiFi information

### POST Endpoints
- `/command` - Send JSON commands

### Command Examples
```json
// LED Control
{"command": "LED_ON"}
{"command": "LED_OFF"}
{"command": "LED_TOGGLE"}

// Status and Sensor
{"command": "STATUS"}
{"command": "SENSOR"}

// System
{"command": "RESTART"}
```

## 🎮 Usage Examples

### Android App Interface

1. **Connection Section:**
   - Enter ESP32 IP address (default: 192.168.4.1)
   - Test connection
   - View WiFi information

2. **Control Section:**
   - LED ON/OFF buttons
   - Get Status button
   - Read Sensor button
   - Custom command input

3. **Response Log:**
   - View all command responses
   - Timestamps for each action
   - Clear log functionality

### Web Interface

You can also control the ESP32 directly through a web browser:
1. Connect to ESP32 WiFi
2. Open browser and go to `192.168.4.1`
3. Use the web interface buttons

## 🔧 Customization

### Adding New Commands

**ESP32 Side:**
```cpp
// In processCommand() function
else if (command == "YOUR_COMMAND") {
    // Your code here
    response = "Command executed";
}
```

**Android Side:**
```kotlin
// Add button in MainActivity.kt
Button(
    onClick = {
        scope.launch {
            viewModel.sendCommand("YOUR_COMMAND")
        }
    }
) {
    Text("Your Button")
}
```

### Adding Sensors

**ESP32 Side:**
```cpp
// In readSensorData() function
int sensorValue = analogRead(yourSensorPin);
float yourSensorValue = convertSensorValue(sensorValue);
```

## 🐛 Troubleshooting

### Common Issues

1. **Can't connect to ESP32 WiFi:**
   - Check if ESP32 is powered and running
   - Verify WiFi credentials
   - Check Serial Monitor for ESP32 status

2. **App can't communicate:**
   - Ensure phone is connected to ESP32 WiFi
   - Check IP address (default: 192.168.4.1)
   - Verify ESP32 web server is running

3. **Commands not working:**
   - Check response log for error messages
   - Verify JSON format in custom commands
   - Monitor ESP32 Serial output

### Debug Steps

1. **ESP32 Debugging:**
   ```cpp
   // Enable more verbose output
   Serial.println("Debug message");
   ```

2. **Android Debugging:**
   - Use Android Studio Logcat
   - Check network logs
   - Verify permissions granted

## 📱 Screenshots

*Note: Add screenshots of your Android app interface here*

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is open source and available under the MIT License.

## 🆘 Support

If you encounter issues:
1. Check the troubleshooting section
2. Review the Serial Monitor output
3. Verify all connections and configurations
4. Create an issue with detailed information

## 🔮 Future Enhancements

- [ ] Support for multiple ESP32 devices
- [ ] Data logging and charts
- [ ] Voice commands
- [ ] Bluetooth communication option
- [ ] Over-the-air (OTA) updates
- [ ] Custom themes
- [ ] Widget support

---

**Happy Coding!** 🎉