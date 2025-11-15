@echo off
REM AndroidScript Build Script for Windows
REM Builds the project in Release mode

echo ========================================
echo   AndroidScript Build Script
echo ========================================
echo.

REM Create build directory if it doesn't exist
if not exist "build" (
    echo Creating build directory...
    mkdir build
)

cd build

echo Running CMake configuration...
cmake ..
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo   X CMake configuration failed
    echo ========================================
    exit /b 1
)

echo.
echo CMake configuration successful
echo.

echo Building project...
cmake --build . --config Release
if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo   X Build Failed
    echo ========================================
    exit /b 1
)

echo.
echo ========================================
echo   Build Successful!
echo ========================================
echo.
echo Executable location:
echo   build\bin\Release\androidscript.exe
echo.
echo Run a script:
echo   build\bin\Release\androidscript.exe script.as
echo.
echo Test basic functionality:
echo   build\bin\Release\androidscript.exe single_arg_test.as
echo.

cd ..
