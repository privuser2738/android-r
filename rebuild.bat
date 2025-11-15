@echo off
REM AndroidScript Rebuild Script for Windows
REM Cleans and rebuilds the project from scratch

echo ========================================
echo   AndroidScript Rebuild Script
echo ========================================
echo.

REM Remove build directory if it exists
if exist "build" (
    echo Cleaning build directory...
    rmdir /s /q build
    echo Build directory cleaned
    echo.
)

REM Create fresh build directory
echo Creating fresh build directory...
mkdir build

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
    echo   X Rebuild Failed
    echo ========================================
    exit /b 1
)

echo.
echo ========================================
echo   Rebuild Successful!
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
