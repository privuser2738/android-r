cmake_minimum_required(VERSION 3.15)
project(AndroidScript VERSION 1.0.0 LANGUAGES CXX C)

# C++ Standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)

# Build type
if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

# Options
option(BUILD_TESTS "Build test suite" ON)
option(BUILD_EXAMPLES "Build example programs" ON)
option(WITH_OPENCV "Build with OpenCV support" ON)
option(WITH_TESSERACT "Build with Tesseract OCR support" ON)

# Output directories
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

# Find dependencies
if(WITH_OPENCV)
    find_package(OpenCV QUIET)
    if(OpenCV_FOUND)
        message(STATUS "OpenCV found: ${OpenCV_VERSION}")
        add_definitions(-DWITH_OPENCV)
    else()
        message(WARNING "OpenCV not found - image recognition features will be disabled")
    endif()
endif()

if(WITH_TESSERACT)
    find_package(Tesseract QUIET)
    if(Tesseract_FOUND)
        message(STATUS "Tesseract found")
        add_definitions(-DWITH_TESSERACT)
    else()
        message(WARNING "Tesseract not found - OCR features will be disabled")
    endif()
endif()

# Include directories
include_directories(
    ${CMAKE_SOURCE_DIR}/core/include
    ${CMAKE_SOURCE_DIR}/bridge/include
    ${CMAKE_SOURCE_DIR}/host-runtime/include
)

# Subdirectories
add_subdirectory(core)
add_subdirectory(bridge)
add_subdirectory(host-runtime)

if(BUILD_TESTS)
    enable_testing()
    # add_subdirectory(tests)
endif()

# Installation
install(TARGETS androidscript
    RUNTIME DESTINATION bin
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
)

install(DIRECTORY core/include/
    DESTINATION include/androidscript
    FILES_MATCHING PATTERN "*.h" PATTERN "*.hpp"
)
