# How to Build APK for ESP32 Controller App

## 📱 Methods to Generate APK

### Method 1: Using Android Studio (Recommended for beginners)

#### Debug APK (for testing):
1. **Open Project**:
   - Open Android Studio
   - File → Open → Select the `AndroidApp` folder
   - Wait for Gradle sync to complete

2. **Build Debug APK**:
   - Go to `Build` menu → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - Or use shortcut: `Ctrl+Shift+A` (Windows/Linux) or `Cmd+Shift+A` (Mac)
   - Type "Build APK" and select it

3. **Find APK**:
   - APK will be generated at: `AndroidApp/app/build/outputs/apk/debug/app-debug.apk`
   - Android Studio will show a notification with "Locate" link

#### Release APK (for distribution):
1. **Generate Signed APK**:
   - Go to `Build` menu → `Generate Signed Bundle / APK`
   - Select `APK` → Click `Next`

2. **Create Keystore** (first time only):
   ```
   Key store path: Choose location (e.g., ~/esp32-keystore.jks)
   Password: Create a strong password
   Key alias: esp32controller
   Key password: Same or different password
   Validity: 25 years
   Certificate info: Fill your details
   ```

3. **Build Release APK**:
   - Select build variant: `release`
   - Signature Versions: Check both V1 and V2
   - Click `Finish`

### Method 2: Using Command Line (Advanced users)

#### Prerequisites:
```bash
# Make sure you have Android SDK and Java installed
# Set environment variables (if not already set):
export ANDROID_HOME=/path/to/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

#### Debug APK:
```bash
# Navigate to project directory
cd AndroidApp

# Build debug APK
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

#### Release APK:
```bash
# Build release APK (unsigned)
./gradlew assembleRelease

# APK location: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Method 3: Using Gradle in Android Studio Terminal

1. **Open Terminal in Android Studio**:
   - View → Tool Windows → Terminal

2. **Run Gradle Commands**:
   ```bash
   # For debug APK
   ./gradlew assembleDebug
   
   # For release APK
   ./gradlew assembleRelease
   
   # For both
   ./gradlew assemble
   ```

## 🔧 APK Locations

After building, APKs will be located at:

```
AndroidApp/
└── app/
    └── build/
        └── outputs/
            └── apk/
                ├── debug/
                │   └── app-debug.apk          # Debug APK
                └── release/
                    └── app-release.apk        # Release APK (signed)
                    └── app-release-unsigned.apk # Release APK (unsigned)
```

## 📋 APK Types Explained

### Debug APK
- **Purpose**: Testing and development
- **Installation**: Can be installed directly
- **Security**: Signed with debug key (not secure for distribution)
- **Size**: Larger (includes debug information)
- **Performance**: Slower (not optimized)

### Release APK
- **Purpose**: Distribution to users
- **Installation**: Requires proper signing
- **Security**: Signed with your private key
- **Size**: Smaller (optimized)
- **Performance**: Faster (optimized)

## 🛠️ Quick Build Commands

Create these batch files for quick building:

### Windows (`build_apk.bat`):
```batch
@echo off
cd AndroidApp
echo Building Debug APK...
gradlew.bat assembleDebug
echo.
echo APK built successfully!
echo Location: app\build\outputs\apk\debug\app-debug.apk
pause
```

### Linux/Mac (`build_apk.sh`):
```bash
#!/bin/bash
cd AndroidApp
echo "Building Debug APK..."
./gradlew assembleDebug
echo ""
echo "APK built successfully!"
echo "Location: app/build/outputs/apk/debug/app-debug.apk"
```

## 📲 Installing the APK

### On Android Device:
1. **Enable Unknown Sources**:
   - Go to Settings → Security → Enable "Unknown sources"
   - Or Settings → Apps & notifications → Special app access → Install unknown apps

2. **Transfer APK**:
   - Copy APK to device via USB, email, or cloud storage
   - Or use `adb install` command

3. **Install APK**:
   - Tap the APK file in file manager
   - Follow installation prompts

### Using ADB (Android Debug Bridge):
```bash
# Connect device via USB (with USB debugging enabled)
adb install app-debug.apk

# Or install and launch
adb install -r app-debug.apk
adb shell am start -n com.example.esp32controller/.MainActivity
```

## 🚀 Automated Build Script

Create this script for automated building:

### `build_and_install.sh`:
```bash
#!/bin/bash

echo "🔨 Building ESP32 Controller APK..."
cd AndroidApp

# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ APK built successfully!"
    echo "📍 Location: app/build/outputs/apk/debug/app-debug.apk"
    
    # Ask if user wants to install
    read -p "📲 Install on connected device? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        echo "📱 App installed successfully!"
    fi
else
    echo "❌ Build failed!"
fi
```

## 🐛 Troubleshooting

### Common Issues:

1. **Gradle sync failed**:
   ```bash
   # Clear Gradle cache
   ./gradlew clean
   # Or delete .gradle folder and sync again
   ```

2. **Build failed**:
   ```bash
   # Check Java version (should be 8 or 11)
   java -version
   
   # Update Gradle wrapper
   ./gradlew wrapper --gradle-version=8.0
   ```

3. **APK not installing**:
   - Check if "Unknown sources" is enabled
   - Verify APK is not corrupted
   - Check device storage space

4. **Keystore issues**:
   - Remember keystore password (cannot be recovered)
   - Keep keystore file safe (needed for app updates)

## 📊 Build Optimization

### Reduce APK Size:
```gradle
// In app/build.gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### Multiple APKs (for different architectures):
```gradle
android {
    splits {
        abi {
            enable true
            reset()
            include 'x86', 'x86_64', 'arm64-v8a', 'armeabi-v7a'
            universalApk false
        }
    }
}
```

---

## 📱 Ready to Use!

After following any of these methods, you'll have an APK file that you can:
- Install on any Android device
- Share with others
- Upload to app stores (for signed release APK)
- Use for testing the ESP32 controller functionality

Choose the method that best fits your experience level and requirements!