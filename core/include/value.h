#ifndef ANDROIDSCRIPT_VALUE_H
#define ANDROIDSCRIPT_VALUE_H

#include <string>
#include <vector>
#include <map>
#include <memory>
#include <functional>
#include <ostream>

namespace androidscript {

// Forward declarations
class Value;
class Environment;

// Type aliases
using NativeFunction = std::function<Value(const std::vector<Value>&)>;
using ValueArray = std::vector<Value>;
using ValueMap = std::map<std::string, Value>;

// Value types
enum class ValueType {
    NIL,
    BOOLEAN,
    INTEGER,
    FLOAT,
    STRING,
    ARRAY,
    OBJECT,
    FUNCTION,
    NATIVE_FUNCTION,
    DEVICE
};

// Device reference (for multi-device support)
struct DeviceRef {
    std::string serial;
    std::string model;
    int screen_width;
    int screen_height;
    std::string android_version;
    void* native_handle;  // Platform-specific handle

    DeviceRef() : screen_width(0), screen_height(0), native_handle(nullptr) {}
    DeviceRef(const std::string& ser) : serial(ser), screen_width(0), screen_height(0), native_handle(nullptr) {}
};

// Function object (user-defined functions)
struct FunctionObject {
    std::vector<std::string> parameters;
    std::shared_ptr<class Statement> body;  // AST node for function body
    std::shared_ptr<Environment> closure;   // Captured environment

    FunctionObject() = default;
    FunctionObject(const std::vector<std::string>& params,
                   std::shared_ptr<class Statement> b,
                   std::shared_ptr<Environment> env)
        : parameters(params), body(b), closure(env) {}
};

// Main Value class
class Value {
public:
    // Constructors
    Value();  // nil
    Value(bool b);
    Value(int64_t i);
    Value(int i);  // Convenience for int literals
    Value(double d);
    Value(const std::string& s);
    Value(const char* s);  // Convenience for string literals
    Value(const ValueArray& arr);
    Value(const ValueMap& obj);
    Value(const DeviceRef& dev);
    Value(const FunctionObject& func);
    Value(NativeFunction func);

    // Copy and move
    Value(const Value& other);
    Value(Value&& other) noexcept;
    Value& operator=(const Value& other);
    Value& operator=(Value&& other) noexcept;
    ~Value();

    // Type checking
    ValueType type() const { return type_; }
    bool isNil() const { return type_ == ValueType::NIL; }
    bool isBool() const { return type_ == ValueType::BOOLEAN; }
    bool isInt() const { return type_ == ValueType::INTEGER; }
    bool isFloat() const { return type_ == ValueType::FLOAT; }
    bool isNumber() const { return isInt() || isFloat(); }
    bool isString() const { return type_ == ValueType::STRING; }
    bool isArray() const { return type_ == ValueType::ARRAY; }
    bool isObject() const { return type_ == ValueType::OBJECT; }
    bool isFunction() const { return type_ == ValueType::FUNCTION; }
    bool isNativeFunction() const { return type_ == ValueType::NATIVE_FUNCTION; }
    bool isDevice() const { return type_ == ValueType::DEVICE; }
    bool isCallable() const { return isFunction() || isNativeFunction(); }

    // Type conversions
    bool asBool() const;
    int64_t asInt() const;
    double asFloat() const;
    std::string asString() const;
    ValueArray& asArray();
    const ValueArray& asArray() const;
    ValueMap& asObject();
    const ValueMap& asObject() const;
    DeviceRef& asDevice();
    const DeviceRef& asDevice() const;
    FunctionObject& asFunction();
    const FunctionObject& asFunction() const;
    NativeFunction& asNativeFunction();
    const NativeFunction& asNativeFunction() const;

    // Factory methods
    static Value makeNil();
    static Value makeBool(bool b);
    static Value makeInt(int64_t i);
    static Value makeFloat(double d);
    static Value makeString(const std::string& s);
    static Value makeArray(const ValueArray& arr = ValueArray());
    static Value makeObject(const ValueMap& obj = ValueMap());
    static Value makeDevice(const DeviceRef& dev);
    static Value makeFunction(const FunctionObject& func);
    static Value makeNativeFunction(NativeFunction func);

    // Operators
    Value operator+(const Value& other) const;
    Value operator-(const Value& other) const;
    Value operator*(const Value& other) const;
    Value operator/(const Value& other) const;
    Value operator%(const Value& other) const;
    bool operator==(const Value& other) const;
    bool operator!=(const Value& other) const;
    bool operator<(const Value& other) const;
    bool operator<=(const Value& other) const;
    bool operator>(const Value& other) const;
    bool operator>=(const Value& other) const;

    // Unary operators
    Value operator-() const;  // Negation
    Value operator!() const;  // Logical NOT

    // Array/Object access
    Value& operator[](size_t index);
    const Value& operator[](size_t index) const;
    Value& operator[](const std::string& key);
    const Value& operator[](const std::string& key) const;

    // String representation
    std::string toString() const;
    std::string typeString() const;

    // Truthiness (for conditionals)
    bool isTruthy() const;

    // Array operations
    void push(const Value& val);
    Value pop();
    size_t length() const;

    // Object operations
    bool hasKey(const std::string& key) const;
    void set(const std::string& key, const Value& val);
    Value get(const std::string& key) const;
    std::vector<std::string> keys() const;

private:
    ValueType type_;

    // Value storage (union-like)
    union {
        bool bool_val;
        int64_t int_val;
        double float_val;
    };

    // Heap-allocated types
    std::shared_ptr<std::string> string_val;
    std::shared_ptr<ValueArray> array_val;
    std::shared_ptr<ValueMap> object_val;
    std::shared_ptr<DeviceRef> device_val;
    std::shared_ptr<FunctionObject> function_val;
    std::shared_ptr<NativeFunction> native_function_val;

    // Helper methods
    void cleanup();
    void copyFrom(const Value& other);
};

// Output operator
std::ostream& operator<<(std::ostream& os, const Value& val);

// Type checking helpers
inline bool isNil(const Value& val) { return val.isNil(); }
inline bool isBool(const Value& val) { return val.isBool(); }
inline bool isInt(const Value& val) { return val.isInt(); }
inline bool isFloat(const Value& val) { return val.isFloat(); }
inline bool isNumber(const Value& val) { return val.isNumber(); }
inline bool isString(const Value& val) { return val.isString(); }
inline bool isArray(const Value& val) { return val.isArray(); }
inline bool isObject(const Value& val) { return val.isObject(); }

} // namespace androidscript

#endif // ANDROIDSCRIPT_VALUE_H
