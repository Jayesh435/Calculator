@echo off
title ESP32 Controller APK Builder

echo 🔨 Building ESP32 Controller APK...
echo ==================================

:: Check if AndroidApp directory exists
if not exist "AndroidApp" (
    echo ❌ Error: AndroidApp directory not found!
    echo    Please run this script from the project root directory.
    pause
    exit /b 1
)

:: Navigate to Android project
cd AndroidApp

:: Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo ❌ Error: gradlew.bat not found!
    echo    Please ensure this is a valid Android project.
    pause
    exit /b 1
)

echo 🧹 Cleaning previous builds...
gradlew.bat clean

echo 📱 Building debug APK...
gradlew.bat assembleDebug

:: Check if build was successful
if %ERRORLEVEL% equ 0 (
    echo.
    echo ✅ APK built successfully!
    echo 📍 APK Location: AndroidApp\app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    :: Check if APK file exists and show size
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        for %%I in (app\build\outputs\apk\debug\app-debug.apk) do (
            echo 📦 APK Size: %%~zI bytes
        )
    )
    
    echo.
    echo 🎉 Your ESP32 Controller app is ready!
    echo.
    echo Next steps:
    echo 1. Transfer the APK to your Android device
    echo 2. Enable 'Unknown sources' in device settings
    echo 3. Install the APK
    echo 4. Connect to ESP32 WiFi network and enjoy!
    echo.
    pause
    
) else (
    echo.
    echo ❌ Build failed!
    echo    Please check the error messages above.
    echo.
    pause
    exit /b 1
)