#!/bin/bash
# Build script for AndroidScript Agent

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk

echo "Building AndroidScript Agent..."
./gradlew assembleDebug "$@"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 APK location: app/build/outputs/apk/debug/app-debug.apk"
    ls -lh app/build/outputs/apk/debug/app-debug.apk
    echo ""
    echo "To install on device:"
    echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ Build failed!"
    exit 1
fi
