#!/bin/bash

# iOS Agent Build Script
# Builds iOSAgent for simulator or device

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}  iOSAgent Build Script${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo

# Check if Xcode is installed
if ! command -v xcodebuild &> /dev/null; then
    echo -e "${RED}✗ Error: Xcode is not installed${NC}"
    echo "  Please install Xcode from the Mac App Store"
    exit 1
fi

echo -e "${GREEN}✓ Xcode found${NC}"
xcodebuild -version | head -n 1

# Parse arguments
TARGET="${1:-simulator}"
CONFIGURATION="${2:-Debug}"

echo
echo -e "${YELLOW}Configuration:${NC}"
echo "  Target: $TARGET"
echo "  Build Configuration: $CONFIGURATION"
echo

cd "$(dirname "$0")"

if [ "$TARGET" == "simulator" ]; then
    echo -e "${GREEN}Building for iOS Simulator...${NC}"

    # List available simulators
    echo
    echo "Available simulators:"
    xcrun simctl list devices | grep -i "iphone" | grep -v "unavailable" | head -n 5
    echo

    # Build for simulator
    xcodebuild \
        -project iOSAgent.xcodeproj \
        -scheme iOSAgent \
        -configuration "$CONFIGURATION" \
        -sdk iphonesimulator \
        -destination 'platform=iOS Simulator,name=iPhone 15,OS=latest' \
        clean build

    BUILD_PATH="build/$CONFIGURATION-iphonesimulator/iOSAgent.app"

    echo
    echo -e "${GREEN}✓ Build successful!${NC}"
    echo
    echo "App built at: $BUILD_PATH"
    echo
    echo "To install on booted simulator:"
    echo "  xcrun simctl install booted $BUILD_PATH"
    echo
    echo "To launch:"
    echo "  xcrun simctl launch booted com.androidscript.iosagent"

elif [ "$TARGET" == "device" ]; then
    echo -e "${GREEN}Building for iOS Device...${NC}"

    # Check for development team
    if [ -z "$DEVELOPMENT_TEAM" ]; then
        echo -e "${YELLOW}⚠ Warning: DEVELOPMENT_TEAM not set${NC}"
        echo "  Set your team ID with: export DEVELOPMENT_TEAM=XXXXXXXXXX"
        echo "  Or configure in Xcode project settings"
        echo
    fi

    # Build for device
    xcodebuild \
        -project iOSAgent.xcodeproj \
        -scheme iOSAgent \
        -configuration "$CONFIGURATION" \
        -sdk iphoneos \
        CODE_SIGN_IDENTITY="iPhone Developer" \
        ${DEVELOPMENT_TEAM:+DEVELOPMENT_TEAM="$DEVELOPMENT_TEAM"} \
        clean build

    BUILD_PATH="build/$CONFIGURATION-iphoneos/iOSAgent.app"

    echo
    echo -e "${GREEN}✓ Build successful!${NC}"
    echo
    echo "App built at: $BUILD_PATH"
    echo
    echo "To install on connected device:"
    echo "  1. Open Xcode"
    echo "  2. Go to Window > Devices and Simulators"
    echo "  3. Drag and drop the .app file"

elif [ "$TARGET" == "archive" ]; then
    echo -e "${GREEN}Creating Archive for Distribution...${NC}"

    # Archive the project
    xcodebuild \
        -project iOSAgent.xcodeproj \
        -scheme iOSAgent \
        -configuration Release \
        -archivePath build/iOSAgent.xcarchive \
        archive

    echo
    echo -e "${GREEN}✓ Archive created!${NC}"
    echo
    echo "Archive at: build/iOSAgent.xcarchive"
    echo
    echo "To export IPA:"
    echo "  1. Open Xcode"
    echo "  2. Go to Window > Organizer"
    echo "  3. Select the archive and click 'Distribute App'"

else
    echo -e "${RED}✗ Unknown target: $TARGET${NC}"
    echo
    echo "Usage: $0 [target] [configuration]"
    echo
    echo "Targets:"
    echo "  simulator  - Build for iOS Simulator (default)"
    echo "  device     - Build for physical iOS device"
    echo "  archive    - Create archive for distribution"
    echo
    echo "Configurations:"
    echo "  Debug      - Debug build (default)"
    echo "  Release    - Release build"
    echo
    echo "Examples:"
    echo "  $0 simulator Debug"
    echo "  $0 device Release"
    echo "  $0 archive"
    exit 1
fi

echo
echo -e "${GREEN}═══════════════════════════════════════${NC}"
echo -e "${GREEN}  Build Complete${NC}"
echo -e "${GREEN}═══════════════════════════════════════${NC}"
