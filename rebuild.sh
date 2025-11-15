#!/bin/bash
# AndroidScript Rebuild Script
# Cleans and rebuilds the project from scratch

set -e  # Exit on error

echo "========================================"
echo "  AndroidScript Rebuild Script"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Remove build directory if it exists
if [ -d "build" ]; then
    echo -e "${YELLOW}Cleaning build directory...${NC}"
    rm -rf build
    echo -e "${GREEN}✓ Build directory cleaned${NC}"
    echo ""
fi

# Create fresh build directory
echo "Creating fresh build directory..."
mkdir build

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
    echo "  ✓ Rebuild Successful!"
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
    echo "  ✗ Rebuild Failed"
    echo "========================================${NC}"
    exit 1
fi
