# ADB bridge library
add_library(androidscript-bridge STATIC
    src/adb_client.cpp
    src/device_manager.cpp
    src/protocol.cpp
    src/transport.cpp
)

target_include_directories(androidscript-bridge
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
    PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/src
)

# Platform-specific networking
if(WIN32)
    target_link_libraries(androidscript-bridge PRIVATE ws2_32)
else()
    # Unix/Linux systems use standard socket libraries
endif()

target_compile_options(androidscript-bridge PRIVATE
    $<$<CXX_COMPILER_ID:MSVC>:/W4>
    $<$<NOT:$<CXX_COMPILER_ID:MSVC>>:-Wall -Wextra -Wpedantic>
)
