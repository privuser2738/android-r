# Host runtime executable
add_executable(androidscript
    src/main.cpp
    src/runtime.cpp
    src/device_orchestrator.cpp
    src/command_executor.cpp
)

target_include_directories(androidscript
    PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${CMAKE_CURRENT_SOURCE_DIR}/src
)

target_link_libraries(androidscript
    PRIVATE
        androidscript-core
        androidscript-bridge
)

# Optional dependencies
if(OpenCV_FOUND)
    target_link_libraries(androidscript PRIVATE ${OpenCV_LIBS})
endif()

if(Tesseract_FOUND)
    target_link_libraries(androidscript PRIVATE tesseract)
endif()

# Platform-specific settings
if(WIN32)
    target_compile_definitions(androidscript PRIVATE PLATFORM_WINDOWS)
    # Add resource file for Windows version info if needed
elseif(UNIX AND NOT APPLE)
    target_link_libraries(androidscript PRIVATE pthread dl)
elseif(APPLE)
    target_link_libraries(androidscript PRIVATE pthread)
endif()

target_compile_options(androidscript PRIVATE
    $<$<CXX_COMPILER_ID:MSVC>:/W4>
    $<$<NOT:$<CXX_COMPILER_ID:MSVC>>:-Wall -Wextra -Wpedantic>
)

# Set output name
set_target_properties(androidscript PROPERTIES
    OUTPUT_NAME androidscript
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
)
