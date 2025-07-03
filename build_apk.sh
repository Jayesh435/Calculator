#!/bin/bash

# ESP32 Controller APK Builder Script

echo "🔨 Building ESP32 Controller APK..."
echo "=================================="

# Check if AndroidApp directory exists
if [ ! -d "AndroidApp" ]; then
    echo "❌ Error: AndroidApp directory not found!"
    echo "   Please run this script from the project root directory."
    exit 1
fi

# Navigate to Android project
cd AndroidApp

# Check if gradlew exists
if [ ! -f "gradlew" ]; then
    echo "❌ Error: gradlew not found!"
    echo "   Please ensure this is a valid Android project."
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

echo "🧹 Cleaning previous builds..."
./gradlew clean

echo "📱 Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ APK built successfully!"
    echo "📍 APK Location: AndroidApp/app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # Get APK size
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
        echo "📦 APK Size: $APK_SIZE"
    fi
    
    echo ""
    echo "🎉 Your ESP32 Controller app is ready!"
    echo ""
    echo "Next steps:"
    echo "1. Transfer the APK to your Android device"
    echo "2. Enable 'Unknown sources' in device settings"
    echo "3. Install the APK"
    echo "4. Connect to ESP32 WiFi network and enjoy!"
    
else
    echo ""
    echo "❌ Build failed!"
    echo "   Please check the error messages above."
    exit 1
fi