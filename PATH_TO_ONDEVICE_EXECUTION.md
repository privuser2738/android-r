# Path to On-Device Script Execution

**Goal:** Enable AndroidScript execution directly on Android devices (not just from PC via ADB)

**Current Progress:** ~60% Complete
**Target:** Priority 10 - Script Execution Service

---

## 🎯 Current Status

### ✅ Phase 1: Foundation (100% Complete)
- [x] Language specification
- [x] Project structure
- [x] Build system (CMake + shell scripts)
- [x] Documentation

### ✅ Phase 2: Core Runtime (98% Complete)
- [x] Lexer implementation
- [x] Parser implementation (for loops just added!)
- [x] AST-based interpreter
- [x] Value system (9 data types)
- [x] Environment/scoping system
- [x] 35+ built-in functions
- [x] Control flow (if/while/for/break/continue)
- [ ] ++ and -- operators (nice to have)
- [ ] Function declarations (not critical)

### ✅ Phase 3: Communication Layer (100% Complete)
- [x] ADB client implementation
- [x] Device discovery and management
- [x] Shell command execution
- [x] File push/pull
- [x] All automation functions connected to ADB

---

## 🚧 What's Needed for On-Device Execution

### Phase 4: Android Agent

To reach on-device script execution, we need to complete **5 priorities**:

#### **Priority 6: Android App Foundation** (Required)
**Status:** Not Started
**Effort:** 2-3 days
**Goal:** Create installable Android app

**Tasks:**
1. Create Android project structure
   - `android-agent/app/build.gradle`
   - `android-agent/app/src/main/AndroidManifest.xml`

2. Implement MainActivity
   - Script selection UI
   - Execution controls (run/stop/pause)
   - Status display
   - Settings screen

3. Add app resources
   - Theme (Material Design)
   - Layouts (XML)
   - Strings, colors, styles
   - App icon

4. Implement permissions handling
   - Request runtime permissions
   - Accessibility service prompt
   - Storage access

**Files to Create:**
```
android-agent/app/src/main/
├── AndroidManifest.xml
├── java/com/androidscript/agent/
│   ├── MainActivity.kt
│   ├── ui/
│   │   ├── ScriptListFragment.kt
│   │   ├── ExecutionFragment.kt
│   │   └── SettingsFragment.kt
│   └── models/
│       └── Script.kt
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── fragment_script_list.xml
    │   └── fragment_execution.xml
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   └── themes.xml
    └── drawable/
        └── ic_launcher.xml
```

**Deliverable:** Installable APK with basic UI

---

#### **Priority 7: Accessibility Service** (Required)
**Status:** Not Started
**Effort:** 4-5 days
**Goal:** UI automation via AccessibilityService

**Tasks:**
1. Create AccessibilityService
   - Service configuration XML
   - Service class implementation
   - Node traversal

2. Implement element finding
   - By resource ID
   - By text
   - By content description
   - By class name

3. Implement UI actions
   - Click/Tap
   - Long press
   - Swipe/Scroll
   - Text input
   - Global actions (Back, Home, Recents)

4. Create gesture system
   - Simple gestures (tap, swipe)
   - Complex gestures (pinch, zoom)
   - Gesture paths

5. Add event monitoring
   - Window state changes
   - UI updates
   - Notification events

**Files to Create:**
```
android-agent/app/src/main/
├── java/com/androidscript/agent/
│   ├── service/
│   │   └── AutomationAccessibilityService.kt
│   └── automation/
│       ├── UIAutomator.kt
│       ├── ElementFinder.kt
│       ├── GestureController.kt
│       └── AccessibilityNodeHelper.kt
└── res/xml/
    └── accessibility_service_config.xml
```

**Deliverable:** Can find and interact with UI elements on device

---

#### **Priority 8: Image Recognition & OCR** (Optional for basic execution)
**Status:** Not Started
**Effort:** 5-6 days
**Goal:** Computer vision capabilities

**Tasks:**
1. Integrate OpenCV
   - Add OpenCV Android SDK
   - Template matching
   - Image preprocessing
   - Confidence scoring
   - Multi-scale detection

2. Integrate Tesseract OCR
   - Add Tesseract Android library
   - Text extraction
   - Language support (English + others)
   - Region-based OCR

3. Screenshot utilities
   - Capture screen
   - Save to file
   - Load from file
   - Crop regions

4. Image comparison
   - Find image on screen
   - Wait for image
   - Image similarity

5. Performance optimization
   - Caching
   - Async processing
   - Native code (JNI if needed)

**Files to Create:**
```
android-agent/app/src/main/
├── java/com/androidscript/agent/
│   └── vision/
│       ├── ImageRecognition.kt
│       ├── OCREngine.kt
│       ├── ScreenCapture.kt
│       └── ImageMatcher.kt
└── cpp/ (if using JNI)
    └── opencv_wrapper.cpp
```

**Dependencies:**
```gradle
implementation 'org.opencv:opencv:4.8.0'
implementation 'com.rmtheis:tess-two:9.1.0'  // Tesseract
```

**Note:** Can skip this for initial on-device execution. Add later for advanced features.

---

#### **Priority 9: App Management APIs** (Partially Required)
**Status:** Not Started
**Effort:** 3-4 days
**Goal:** Control apps and device settings

**Tasks:**
1. App operations
   - Launch app (intent-based)
   - Stop app (requires root or special permissions)
   - Install APK (requires permissions)
   - Uninstall app (user interaction required)
   - Clear app data (requires permissions)

2. Permission management
   - Grant permission (ADB or root)
   - Revoke permission
   - Check permission status

3. Device settings
   - WiFi control
   - Bluetooth control
   - Brightness
   - Volume
   - Airplane mode
   - Screen unlock (fingerprint/pattern/PIN)

**Files to Create:**
```
android-agent/app/src/main/java/com/androidscript/agent/
├── automation/
│   ├── AppManager.kt
│   ├── DeviceController.kt
│   └── PermissionManager.kt
└── utils/
    ├── ShellExecutor.kt  (for root commands if needed)
    └── IntentHelper.kt
```

**Note:** Many features require root or special permissions. Focus on intent-based launching first.

---

#### **Priority 10: Script Execution Service** ⭐ (GOAL)
**Status:** Not Started
**Effort:** 6-8 days
**Goal:** Execute AndroidScript on device

**This is the main objective!**

**Tasks:**
1. Embed interpreter in Android
   - **Option A:** Port C++ interpreter to Android via JNI
   - **Option B:** Rewrite interpreter in Kotlin (simpler, slower)
   - **Recommended:** Start with Option B for simplicity

2. Create script execution engine
   - Load script from file/string
   - Parse script
   - Execute script
   - Handle errors
   - Report results

3. Implement native bridge
   - Call Android APIs from scripts
   - UIAutomator → Tap(), Swipe(), Input()
   - AppManager → LaunchApp(), StopApp()
   - DeviceController → WiFi(), Bluetooth(), Volume()
   - Handle callbacks and async operations

4. Add execution control
   - Start execution
   - Stop execution
   - Pause/Resume
   - Progress reporting
   - Status updates

5. Implement logging and debugging
   - Script logs
   - Error messages
   - Stack traces
   - Performance metrics

**Files to Create:**
```
android-agent/app/src/main/java/com/androidscript/agent/
├── runtime/
│   ├── ScriptEngine.kt           # Main engine
│   ├── Lexer.kt                  # Kotlin port
│   ├── Parser.kt                 # Kotlin port
│   ├── Interpreter.kt            # Kotlin port
│   ├── Value.kt                  # Kotlin port
│   ├── Environment.kt            # Kotlin port
│   └── NativeBridge.kt           # Bridge to Android APIs
├── service/
│   └── ScriptExecutionService.kt # Background service
└── builtins/
    ├── UIBuiltins.kt             # Tap, Swipe, Input, etc.
    ├── AppBuiltins.kt            # LaunchApp, StopApp, etc.
    ├── DeviceBuiltins.kt         # WiFi, Bluetooth, etc.
    ├── FileBuiltins.kt           # ReadFile, WriteFile, etc.
    └── UtilityBuiltins.kt        # Sleep, Print, Log, etc.
```

**Native Bridge Architecture:**
```kotlin
// Script calls: Tap(500, 1000)
// ↓
// Interpreter calls: NativeBridge.executeTap(500, 1000)
// ↓
// NativeBridge calls: UIAutomator.tap(500, 1000)
// ↓
// UIAutomator uses: AccessibilityService to perform tap
```

**Deliverable:** Can run AndroidScript files on device without PC/ADB!

---

## 📊 Effort Breakdown

| Priority | Component | Effort | Critical? |
|----------|-----------|--------|-----------|
| 6 | Android App Foundation | 2-3 days | ✅ Yes |
| 7 | Accessibility Service | 4-5 days | ✅ Yes |
| 8 | Image Recognition & OCR | 5-6 days | ⚠️ Optional |
| 9 | App Management APIs | 3-4 days | ⚠️ Partial |
| 10 | **Script Execution Service** | **6-8 days** | **✅ GOAL** |

**Total Critical Path:** ~15-20 days (without image recognition)
**Total with All Features:** ~20-26 days

---

## 🛤️ Recommended Development Path

### Phase A: Minimal Viable On-Device Execution (1-2 weeks)

**Goal:** Get basic scripts running on device

1. **Week 1:**
   - Day 1-3: Priority 6 - Android App Foundation
   - Day 4-5: Priority 7 - Accessibility Service (basic)
   - Weekend: Buffer

2. **Week 2:**
   - Day 1-5: Priority 10 - Script Execution Service (Kotlin interpreter)
   - Focus on: Lexer, Parser, Interpreter, Basic built-ins
   - Deliverable: Can run simple scripts on device!

**Minimum Features:**
```androidscript
# This should work on-device:
Print("Hello from device!")
Tap(500, 1000)
Sleep(1000)
Swipe(100, 500, 900, 500, 300)
LaunchApp("com.android.settings")
```

---

### Phase B: Enhanced Features (1-2 weeks)

**Goal:** Add more functionality

1. **Week 3:**
   - Complete Priority 7 - Full Accessibility Service
   - Add Priority 9 - App Management (basic)
   - More built-in functions

2. **Week 4 (Optional):**
   - Priority 8 - Image Recognition & OCR
   - Advanced features

**Enhanced Features:**
```androidscript
# Phase B adds:
for ($i = 1; $i <= 5; $i = $i + 1) {
    Tap(500, $i * 200)
    Sleep(500)
}

if (WaitForImage("button.png", 5000)) {
    Tap(500, 1000)
}

$text = OCR(100, 100, 300, 200)
Print("Found text: " + $text)
```

---

## 🎯 Implementation Strategy

### Approach 1: Kotlin Interpreter (Recommended for MVP)

**Pros:**
- No JNI complexity
- Easier debugging
- Faster development
- Native Android integration

**Cons:**
- Slower execution
- Code duplication with C++ version

**Effort:** 6-8 days

**Files to Port:**
```
C++ → Kotlin
core/src/lexer.cpp → Lexer.kt
core/src/parser.cpp → Parser.kt
core/src/interpreter.cpp → Interpreter.kt
core/src/value.cpp → Value.kt
core/src/environment.cpp → Environment.kt
core/src/builtins.cpp → NativeBridge.kt + *Builtins.kt
```

---

### Approach 2: JNI Wrapper (For Later Optimization)

**Pros:**
- Reuse existing C++ code
- Faster execution
- Single codebase

**Cons:**
- JNI complexity
- Harder debugging
- Platform-specific build

**Effort:** 10-12 days (including JNI setup)

**Use When:** After Kotlin version works, optimize critical paths

---

## 📋 Detailed Task List

### Priority 10 Breakdown (Script Execution Service)

#### Task 1: Port Lexer (1 day)
```kotlin
class Lexer(private val source: String) {
    private var current = 0
    private var line = 1
    private var column = 0

    fun tokenize(): List<Token> {
        // Port from lexer.cpp
    }
}
```

#### Task 2: Port Parser (1-2 days)
```kotlin
class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<Statement> {
        // Port from parser.cpp
    }
}
```

#### Task 3: Port Value System (1 day)
```kotlin
sealed class Value {
    object Nil : Value()
    data class Int(val value: Long) : Value()
    data class Float(val value: Double) : Value()
    data class String(val value: kotlin.String) : Value()
    data class Boolean(val value: kotlin.Boolean) : Value()
    data class Array(val elements: MutableList<Value>) : Value()
    // ...
}
```

#### Task 4: Port Interpreter (2 days)
```kotlin
class Interpreter {
    private var environment = Environment()

    fun execute(statements: List<Statement>) {
        // Port from interpreter.cpp
    }

    fun evaluate(expr: Expression): Value {
        // Port expression evaluation
    }
}
```

#### Task 5: Implement Native Bridge (2 days)
```kotlin
object NativeBridge {
    lateinit var uiAutomator: UIAutomator
    lateinit var appManager: AppManager

    fun executeTap(x: Int, y: Int) {
        uiAutomator.tap(x, y)
    }

    fun executeLaunchApp(package: String) {
        appManager.launchApp(package)
    }

    // Map all built-in functions
}
```

#### Task 6: Create Execution Service (1 day)
```kotlin
class ScriptExecutionService : Service() {
    private val engine = ScriptEngine()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val script = intent.getStringExtra("script")
        GlobalScope.launch {
            engine.execute(script!!)
        }
        return START_NOT_STICKY
    }
}
```

---

## 🧪 Testing Strategy

### Unit Tests:
```kotlin
class LexerTest {
    @Test
    fun testTokenization() {
        val lexer = Lexer("$x = 10")
        val tokens = lexer.tokenize()
        // Assert tokens correct
    }
}

class InterpreterTest {
    @Test
    fun testBasicExecution() {
        val interpreter = Interpreter()
        val result = interpreter.execute("Print('Hello')")
        // Assert output correct
    }
}
```

### Integration Tests:
```kotlin
class ScriptEngineTest {
    @Test
    fun testTapExecution() {
        val engine = ScriptEngine()
        engine.execute("Tap(500, 1000)")
        // Verify tap was performed
    }
}
```

### End-to-End Tests:
```androidscript
# test_ondevice.as
Print("Starting on-device test")
Tap(500, 1000)
Sleep(1000)
LaunchApp("com.android.settings")
Sleep(2000)
Tap(200, 300)
Print("Test complete!")
```

---

## 📦 Deliverables

### Deliverable 1: Android App (Priority 6)
- ✅ Installable APK
- ✅ Script selection UI
- ✅ Execution controls
- ✅ Status display

### Deliverable 2: Accessibility Service (Priority 7)
- ✅ UI automation working
- ✅ Element finding
- ✅ Gesture execution

### Deliverable 3: Script Execution (Priority 10)
- ✅ Kotlin interpreter
- ✅ Native bridge
- ✅ Built-in functions
- ✅ **Can run scripts on device!**

---

## 🎉 Success Criteria

### Minimum Success (MVP):
```androidscript
# This script runs entirely on device:
Print("Hello from Android device!")

for ($i = 1; $i <= 3; $i = $i + 1) {
    Tap(500, 500)
    Sleep(500)
}

LaunchApp("com.android.settings")
Sleep(2000)
Tap(500, 1000)
Print("Complete!")
```

### Full Success:
```androidscript
# Advanced on-device script:
$dev = GetCurrentDevice()
Print("Running on: " + $dev.model)

# UI Automation
Tap(500, 1000)
Swipe(100, 1000, 900, 1000, 300)
Input("test@example.com")

# App Management
LaunchApp("com.android.chrome")
Sleep(3000)

# Image Recognition (if Priority 8 done)
if (WaitForImage("search_button.png", 5000)) {
    Tap(500, 800)
}

# OCR
$text = OCR(100, 100, 500, 200)
Print("Found: " + $text)

# Device Control
SetBrightness(50)
SetVolume(10)

Print("All features working!")
```

---

## 🚀 Next Steps

### Immediate (This Week):
1. Start Priority 6 - Android App Foundation
2. Set up Android Studio project
3. Create basic MainActivity UI
4. Implement script loading mechanism

### Next Week:
1. Start Priority 7 - Accessibility Service
2. Basic tap/swipe working
3. Begin Priority 10 - Script Execution Service
4. Port Lexer and Parser to Kotlin

### Week After:
1. Complete Kotlin interpreter
2. Implement native bridge
3. Test on-device execution
4. First on-device script working! 🎉

---

## 📚 Resources

### Android Development:
- [AccessibilityService Documentation](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService)
- [Kotlin Language Guide](https://kotlinlang.org/docs/home.html)
- [Android App Architecture](https://developer.android.com/topic/architecture)

### OpenCV & Tesseract:
- [OpenCV Android](https://opencv.org/android/)
- [Tesseract Android](https://github.com/rmtheis/tess-two)

### Testing:
- [Android Testing Guide](https://developer.android.com/training/testing)
- [Kotlin Testing](https://kotlinlang.org/docs/jvm-test-using-junit.html)

---

**Target Date:** 2-4 weeks from now
**Status:** Ready to begin Phase 4
**Next Priority:** Priority 6 - Android App Foundation

---

*This document outlines the complete path from current state (host-based ADB automation) to on-device script execution. Once Priority 10 is complete, AndroidScript can run entirely on the device without requiring a PC!*
