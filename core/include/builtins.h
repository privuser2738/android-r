#ifndef ANDROIDSCRIPT_BUILTINS_H
#define ANDROIDSCRIPT_BUILTINS_H

#include "value.h"
#include <vector>

namespace androidscript {

// Forward declaration
class Interpreter;

// Register all built-in functions with the interpreter
void registerBuiltins(Interpreter& interpreter);

// Utility functions
Value builtin_Print(const std::vector<Value>& args);
Value builtin_Log(const std::vector<Value>& args);
Value builtin_LogError(const std::vector<Value>& args);
Value builtin_Sleep(const std::vector<Value>& args);
Value builtin_Assert(const std::vector<Value>& args);

// String functions
Value builtin_Length(const std::vector<Value>& args);
Value builtin_Substring(const std::vector<Value>& args);
Value builtin_ToUpper(const std::vector<Value>& args);
Value builtin_ToLower(const std::vector<Value>& args);
Value builtin_Contains(const std::vector<Value>& args);
Value builtin_Replace(const std::vector<Value>& args);

// Array functions
Value builtin_Count(const std::vector<Value>& args);
Value builtin_Push(const std::vector<Value>& args);
Value builtin_Pop(const std::vector<Value>& args);
Value builtin_Join(const std::vector<Value>& args);

// Type conversion
Value builtin_ToString(const std::vector<Value>& args);
Value builtin_ToInt(const std::vector<Value>& args);
Value builtin_ToFloat(const std::vector<Value>& args);

// Device management (placeholders for now)
Value builtin_Device(const std::vector<Value>& args);
Value builtin_GetAllDevices(const std::vector<Value>& args);

// File operations
Value builtin_FileExists(const std::vector<Value>& args);
Value builtin_ReadFile(const std::vector<Value>& args);
Value builtin_WriteFile(const std::vector<Value>& args);

// UI Automation
Value builtin_Tap(const std::vector<Value>& args);
Value builtin_Swipe(const std::vector<Value>& args);
Value builtin_Input(const std::vector<Value>& args);
Value builtin_KeyEvent(const std::vector<Value>& args);
Value builtin_Screenshot(const std::vector<Value>& args);

// App Management
Value builtin_LaunchApp(const std::vector<Value>& args);
Value builtin_StopApp(const std::vector<Value>& args);
Value builtin_InstallApp(const std::vector<Value>& args);
Value builtin_UninstallApp(const std::vector<Value>& args);
Value builtin_ClearAppData(const std::vector<Value>& args);

// Device File Operations
Value builtin_PushFile(const std::vector<Value>& args);
Value builtin_PullFile(const std::vector<Value>& args);

} // namespace androidscript

#endif // ANDROIDSCRIPT_BUILTINS_H
