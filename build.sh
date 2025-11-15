#!/bin/bash
# AndroidScript Build Script
# Builds the project in Release mode

set -e  # Exit on error

echo "========================================"
echo "  AndroidScript Build Script"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Create build directory if it doesn't exist
if [ ! -d "build" ]; then
    echo "Creating build directory..."
    mkdir build
fi

cd build

echo "Running CMake configuration..."
if cmake .. ; then
    echo -e "${GREEN}✓ CMake configuration successful${NC}"
    echo ""
else
    echo -e "${RED}✗ CMake configuration failed${NC}"
    exit 1
fi

echo "Building project..."
if cmake --build . --config Release ; then
    echo ""
    echo -e "${GREEN}========================================"
    echo "  ✓ Build Successful!"
    echo "========================================${NC}"
    echo ""
    echo "Executable location:"
    echo "  ./build/bin/androidscript"
    echo ""
    echo "Run a script:"
    echo "  ./build/bin/androidscript script.as"
    echo ""
    echo "Test basic functionality:"
    echo "  ./build/bin/androidscript single_arg_test.as"
    echo ""
else
    echo ""
    echo -e "${RED}========================================"
    echo "  ✗ Build Failed"
    echo "========================================${NC}"
    exit 1
fi
