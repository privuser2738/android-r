# Development Guide

## Quick Start for Developers

### Prerequisites Setup

#### Linux/Mac
```bash
# Install build tools
sudo apt-get install build-essential cmake git

# Install OpenCV (optional, for image recognition)
sudo apt-get install libopencv-dev

# Install Tesseract (optional, for OCR)
sudo apt-get install tesseract-ocr libtesseract-dev

# Install Android SDK
# Download from: https://developer.android.com/studio
```

#### Windows
```powershell
# Install Visual Studio 2022 with C++ tools
# Install CMake
# Install Android Studio

# OpenCV and Tesseract - download pre-built binaries
```

---

## Building the Project

### Core C++ Components

```bash
# From project root
mkdir build
cd build

# Configure
cmake .. -DCMAKE_BUILD_TYPE=Release

# Build
cmake --build . --config Release

# Run tests (when implemented)
ctest --output-on-failure
```

### With Optional Features

```bash
# Build without OpenCV
cmake .. -DWITH_OPENCV=OFF

# Build without Tesseract
cmake .. -DWITH_TESSERACT=OFF

# Build with debug symbols
cmake .. -DCMAKE_BUILD_TYPE=Debug
```

### Android Agent

```bash
cd android-agent

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

---

## Project Structure Explained

```
android-r/
│
├── core/                          # Script engine (C++)
│   ├── include/                   # Public headers
│   │   ├── token.h               # Token definitions
│   │   ├── lexer.h               # Lexical analyzer
│   │   ├── parser.h              # Syntax parser
│   │   ├── ast.h                 # Abstract syntax tree
│   │   ├── interpreter.h         # Script executor
│   │   ├── value.h               # Runtime values
│   │   └── builtins.h            # Built-in functions
│   └── src/                       # Implementation files
│
├── bridge/                        # ADB communication (C++)
│   ├── include/
│   │   ├── adb_client.h          # ADB protocol
│   │   ├── device_manager.h      # Device discovery
│   │   ├── protocol.h            # Message protocol
│   │   └── transport.h           # Network transport
│   └── src/
│
├── host-runtime/                  # Host CLI tool (C++)
│   ├── include/
│   │   ├── runtime.h             # Runtime engine
│   │   ├── device_orchestrator.h # Multi-device manager
│   │   └── command_executor.h    # Command dispatcher
│   └── src/
│       └── main.cpp              # Entry point
│
├── android-agent/                 # Android app (Kotlin/Java)
│   └── app/src/main/
│       ├── java/com/androidscript/agent/
│       │   ├── MainActivity.kt
│       │   ├── service/
│       │   │   ├── AutomationAccessibilityService.kt
│       │   │   ├── ScriptExecutionService.kt
│       │   │   └── NetworkServerService.kt
│       │   ├── automation/
│       │   │   ├── UIAutomator.kt
│       │   │   ├── GestureController.kt
│       │   │   ├── AppManager.kt
│       │   │   └── DeviceController.kt
│       │   ├── vision/
│       │   │   ├── ImageRecognition.kt
│       │   │   └── OCREngine.kt
│       │   └── runtime/
│       │       ├── ScriptEngine.kt
│       │       └── NativeBridge.kt
│       └── res/                   # Android resources
│
├── stdlib/                        # Standard library
│   ├── device.as                 # Device functions
│   ├── ui.as                     # UI helpers
│   └── utils.as                  # Utility functions
│
├── examples/                      # Example scripts
│   ├── simple_login.as
│   ├── multi_device_test.as
│   ├── image_recognition_test.as
│   ├── app_management.as
│   └── stress_test.as
│
├── tests/                         # Test suites
│   ├── unit/                     # Unit tests
│   ├── integration/              # Integration tests
│   └── e2e/                      # End-to-end tests
│
├── docs/                          # Documentation
│
├── LANGUAGE_SPEC.md              # Language specification
├── ROADMAP.md                    # Development roadmap
├── README.md                     # Project overview
└── DEVELOPMENT.md                # This file
```

---

## Development Workflow

### 1. Working on Core Engine (C++)

```bash
# Make changes to core/src/*.cpp

# Rebuild
cd build
cmake --build . --target androidscript-core

# Test changes
./bin/androidscript ../examples/simple_login.as
```

### 2. Working on Android Agent

```bash
# Make changes to android-agent/app/src/main/java/...

# Hot reload (if using Android Studio)
# Or rebuild and reinstall:
./gradlew installDebug

# View logs
adb logcat | grep AndroidScript
```

### 3. Adding New Built-in Function

**Step 1:** Define in `core/include/builtins.h`
```cpp
Value builtin_MyFunction(const std::vector<Value>& args);
```

**Step 2:** Implement in `core/src/builtins.cpp`
```cpp
Value builtin_MyFunction(const std::vector<Value>& args) {
    // Implementation
    return Value::makeNull();
}
```

**Step 3:** Register in interpreter
```cpp
env.define("MyFunction", Value::makeBuiltin(builtin_MyFunction));
```

**Step 4:** Document in LANGUAGE_SPEC.md

### 4. Adding New Automation Command

**Step 1:** Define in protocol (`bridge/include/protocol.h`)
```cpp
enum class CommandType {
    // ...
    NEW_COMMAND
};
```

**Step 2:** Implement in Android agent
```kotlin
// android-agent/.../automation/UIAutomator.kt
fun executeNewCommand(params: JSONObject): Result {
    // Implementation
}
```

**Step 3:** Add to command executor
```cpp
// host-runtime/src/command_executor.cpp
case CommandType::NEW_COMMAND:
    return executeNewCommand(params);
```

**Step 4:** Create script built-in
```cpp
// core/src/builtins.cpp
Value builtin_NewCommand(const std::vector<Value>& args) {
    // Send command to device
}
```

---

## Testing Strategy

### Unit Tests
Test individual components in isolation.

```cpp
// tests/unit/lexer_test.cpp
TEST(LexerTest, TokenizesNumbers) {
    Lexer lexer("123");
    auto tokens = lexer.tokenize();
    ASSERT_EQ(tokens[0].type, TokenType::INTEGER);
    ASSERT_EQ(tokens[0].int_value, 123);
}
```

### Integration Tests
Test component interactions.

```cpp
// tests/integration/parser_test.cpp
TEST(ParserTest, ParsesIfStatement) {
    Lexer lexer("if (x > 5) { print(x) }");
    Parser parser(lexer.tokenize());
    auto ast = parser.parse();
    ASSERT_TRUE(ast != nullptr);
}
```

### End-to-End Tests
Test complete workflows.

```bash
# tests/e2e/login_test.sh
androidscript run examples/simple_login.as --device emulator-5554
# Verify output
```

---

## Debugging

### Debugging C++ Code

```bash
# Build with debug symbols
cmake .. -DCMAKE_BUILD_TYPE=Debug
cmake --build .

# Run with GDB
gdb --args ./bin/androidscript ../examples/simple_login.as

# Or use LLDB on Mac
lldb ./bin/androidscript -- ../examples/simple_login.as
```

### Debugging Android Agent

```bash
# Enable ADB debugging
adb shell setprop log.tag.AndroidScript DEBUG

# View logs
adb logcat -s AndroidScript:D

# Or use Android Studio debugger
# Attach to process: com.androidscript.agent
```

### Script Debugging

Add debug output to scripts:
```androidscript
Print("Debug: Variable value = " + $myVar)
Log("Checkpoint reached")
Screenshot("debug_state.png")
```

---

## Code Style Guidelines

### C++ Style
- Follow Google C++ Style Guide
- Use snake_case for functions and variables
- Use PascalCase for classes
- Always use RAII
- Prefer smart pointers over raw pointers
- Use const where possible

```cpp
// Good
std::unique_ptr<Expression> parse_expression() {
    const auto token = current_token();
    // ...
}

// Bad
Expression* parseExpression() {
    Token token = currentToken();
    // ...
}
```

### Kotlin/Java Style
- Follow Android/Kotlin style guide
- Use camelCase for functions and variables
- Use PascalCase for classes
- Use nullable types appropriately
- Prefer coroutines over threads

```kotlin
// Good
suspend fun executeScript(scriptPath: String): Result {
    val script = File(scriptPath).readText()
    // ...
}

// Bad
fun ExecuteScript(script_path: String): Result {
    // ...
}
```

### Script Style
- Use descriptive variable names
- Add comments for complex logic
- Group related actions
- Use functions for reusable code

```androidscript
// Good
function LoginUser($username, $password) {
    Tap(500, 800)
    Input($username)
    Tap(500, 1000)
    Input($password)
    TapText("Login")
}

// Bad
Tap(500, 800)
Input($u)
Tap(500, 1000)
Input($p)
TapText("Login")
```

---

## Contributing

### Before Submitting PR

1. **Build successfully**
   ```bash
   cmake --build build --target all
   cd android-agent && ./gradlew build
   ```

2. **Run tests**
   ```bash
   cd build && ctest
   cd android-agent && ./gradlew test
   ```

3. **Format code**
   ```bash
   # C++: Use clang-format
   clang-format -i core/src/*.cpp

   # Kotlin: Use ktlint
   cd android-agent && ./gradlew ktlintFormat
   ```

4. **Update documentation**
   - Update LANGUAGE_SPEC.md for language changes
   - Update README.md for user-facing changes
   - Add example if adding new feature

5. **Write tests**
   - Add unit tests for new functions
   - Add integration tests for new features

---

## Common Issues & Solutions

### Issue: CMake can't find OpenCV
```bash
# Specify OpenCV path
cmake .. -DOpenCV_DIR=/path/to/opencv/build

# Or disable OpenCV
cmake .. -DWITH_OPENCV=OFF
```

### Issue: Android build fails
```bash
# Clean build
cd android-agent
./gradlew clean

# Update Gradle wrapper
./gradlew wrapper --gradle-version=8.2

# Sync dependencies
./gradlew --refresh-dependencies
```

### Issue: ADB not found
```bash
# Add to PATH
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Or specify directly
export ADB_PATH=/path/to/adb
```

### Issue: Script execution fails
```bash
# Enable verbose logging
androidscript run script.as --verbose

# Check syntax
androidscript validate script.as

# Run in debug mode
androidscript run script.as --debug
```

---

## Performance Optimization

### Script Performance
- Minimize unnecessary waits
- Use element IDs instead of image recognition when possible
- Cache image templates
- Batch commands when possible

### Multi-Device Performance
- Use parallel execution
- Set reasonable timeouts
- Handle failures gracefully
- Monitor device resources

### Memory Management
- Profile with valgrind (C++)
- Use Android Profiler (Android)
- Watch for memory leaks
- Limit screenshot retention

---

## Continuous Integration

### GitHub Actions Example

```yaml
# .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  build-cpp:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install dependencies
        run: sudo apt-get install cmake build-essential
      - name: Build
        run: |
          cmake -B build
          cmake --build build
      - name: Test
        run: cd build && ctest

  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Gradle
        run: cd android-agent && ./gradlew build
```

---

## Resources

### Documentation
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [ADB Protocol](https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/master/OVERVIEW.TXT)
- [OpenCV Docs](https://docs.opencv.org/)
- [Tesseract OCR](https://github.com/tesseract-ocr/tesseract)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [scrcpy](https://github.com/Genymobile/scrcpy) - Screen mirroring
- [Vysor](https://www.vysor.io/) - Device control
- [ADB Wireless](https://developer.android.com/studio/command-line/adb#wireless)

---

## Getting Help

- Check [ROADMAP.md](ROADMAP.md) for project status
- Read [LANGUAGE_SPEC.md](LANGUAGE_SPEC.md) for syntax
- Review [examples/](examples/) for usage patterns
- Open issue on GitHub for bugs
- Start discussion for feature requests

---

**Happy Coding!** 🚀
