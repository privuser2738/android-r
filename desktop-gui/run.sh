#!/bin/bash
# AndroidScript Desktop GUI - Run Script

echo "==========================================="
echo "  AndroidScript Desktop GUI"
echo "==========================================="
echo ""

# Check Java version
if ! command -v java &> /dev/null; then
    echo "Error: Java not found"
    echo "Please install JDK 17 or higher"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher required"
    echo "Current version: $JAVA_VERSION"
    exit 1
fi

echo "✓ Java $JAVA_VERSION found"
echo ""

# Check if host controller is running
if curl -s http://localhost:8080/health > /dev/null 2>&1; then
    echo "✓ Host controller is running"
else
    echo "⚠ Warning: Host controller not detected at localhost:8080"
    echo "  Please start it with:"
    echo "    cd ../host-controller && ./gradlew run --args=\"server\""
    echo ""
fi

echo "Starting Desktop GUI..."
echo ""

./gradlew run
