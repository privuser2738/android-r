# Core script engine library
add_library(androidscript-core STATIC
    src/lexer.cpp
    src/parser.cpp
    src/ast.cpp
    src/interpreter.cpp
    src/value.cpp
    src/environment.cpp
    src/builtins.cpp
    src/memory.cpp
)

target_include_directories(androidscript-core
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
    PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/src
        ${CMAKE_SOURCE_DIR}/bridge/include
)

target_link_libraries(androidscript-core
    PRIVATE
        androidscript-bridge
)

target_compile_options(androidscript-core PRIVATE
    $<$<CXX_COMPILER_ID:MSVC>:/W4>
    $<$<NOT:$<CXX_COMPILER_ID:MSVC>>:-Wall -Wextra -Wpedantic>
)

# Platform-specific settings
if(WIN32)
    target_compile_definitions(androidscript-core PRIVATE PLATFORM_WINDOWS)
elseif(UNIX)
    target_compile_definitions(androidscript-core PRIVATE PLATFORM_UNIX)
endif()
