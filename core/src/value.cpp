#include "value.h"
#include <sstream>
#include <stdexcept>
#include <cmath>

namespace androidscript {

// Constructors
Value::Value() : type_(ValueType::NIL), int_val(0) {}

Value::Value(bool b) : type_(ValueType::BOOLEAN), bool_val(b) {}

Value::Value(int64_t i) : type_(ValueType::INTEGER), int_val(i) {}

Value::Value(int i) : type_(ValueType::INTEGER), int_val(i) {}

Value::Value(double d) : type_(ValueType::FLOAT), float_val(d) {}

Value::Value(const std::string& s)
    : type_(ValueType::STRING), int_val(0),
      string_val(std::make_shared<std::string>(s)) {}

Value::Value(const char* s)
    : type_(ValueType::STRING), int_val(0),
      string_val(std::make_shared<std::string>(s)) {}

Value::Value(const ValueArray& arr)
    : type_(ValueType::ARRAY), int_val(0),
      array_val(std::make_shared<ValueArray>(arr)) {}

Value::Value(const ValueMap& obj)
    : type_(ValueType::OBJECT), int_val(0),
      object_val(std::make_shared<ValueMap>(obj)) {}

Value::Value(const DeviceRef& dev)
    : type_(ValueType::DEVICE), int_val(0),
      device_val(std::make_shared<DeviceRef>(dev)) {}

Value::Value(const FunctionObject& func)
    : type_(ValueType::FUNCTION), int_val(0),
      function_val(std::make_shared<FunctionObject>(func)) {}

Value::Value(NativeFunction func)
    : type_(ValueType::NATIVE_FUNCTION), int_val(0),
      native_function_val(std::make_shared<NativeFunction>(func)) {}

// Copy constructor
Value::Value(const Value& other) : type_(ValueType::NIL), int_val(0) {
    copyFrom(other);
}

// Move constructor
Value::Value(Value&& other) noexcept : type_(other.type_) {
    switch (type_) {
        case ValueType::BOOLEAN: bool_val = other.bool_val; break;
        case ValueType::INTEGER: int_val = other.int_val; break;
        case ValueType::FLOAT: float_val = other.float_val; break;
        case ValueType::STRING: string_val = std::move(other.string_val); break;
        case ValueType::ARRAY: array_val = std::move(other.array_val); break;
        case ValueType::OBJECT: object_val = std::move(other.object_val); break;
        case ValueType::DEVICE: device_val = std::move(other.device_val); break;
        case ValueType::FUNCTION: function_val = std::move(other.function_val); break;
        case ValueType::NATIVE_FUNCTION: native_function_val = std::move(other.native_function_val); break;
        default: break;
    }
    other.type_ = ValueType::NIL;
}

// Assignment operator
Value& Value::operator=(const Value& other) {
    if (this != &other) {
        cleanup();
        copyFrom(other);
    }
    return *this;
}

// Move assignment
Value& Value::operator=(Value&& other) noexcept {
    if (this != &other) {
        cleanup();
        type_ = other.type_;

        switch (type_) {
            case ValueType::BOOLEAN: bool_val = other.bool_val; break;
            case ValueType::INTEGER: int_val = other.int_val; break;
            case ValueType::FLOAT: float_val = other.float_val; break;
            case ValueType::STRING: string_val = std::move(other.string_val); break;
            case ValueType::ARRAY: array_val = std::move(other.array_val); break;
            case ValueType::OBJECT: object_val = std::move(other.object_val); break;
            case ValueType::DEVICE: device_val = std::move(other.device_val); break;
            case ValueType::FUNCTION: function_val = std::move(other.function_val); break;
            case ValueType::NATIVE_FUNCTION: native_function_val = std::move(other.native_function_val); break;
            default: break;
        }

        other.type_ = ValueType::NIL;
    }
    return *this;
}

// Destructor
Value::~Value() {
    cleanup();
}

// Helper methods
void Value::cleanup() {
    // Shared pointers will clean themselves up
    string_val.reset();
    array_val.reset();
    object_val.reset();
    device_val.reset();
    function_val.reset();
    native_function_val.reset();
}

void Value::copyFrom(const Value& other) {
    type_ = other.type_;

    switch (type_) {
        case ValueType::NIL: break;
        case ValueType::BOOLEAN: bool_val = other.bool_val; break;
        case ValueType::INTEGER: int_val = other.int_val; break;
        case ValueType::FLOAT: float_val = other.float_val; break;
        case ValueType::STRING: string_val = other.string_val; break;
        case ValueType::ARRAY: array_val = other.array_val; break;
        case ValueType::OBJECT: object_val = other.object_val; break;
        case ValueType::DEVICE: device_val = other.device_val; break;
        case ValueType::FUNCTION: function_val = other.function_val; break;
        case ValueType::NATIVE_FUNCTION: native_function_val = other.native_function_val; break;
    }
}

// Type conversions
bool Value::asBool() const {
    if (!isBool()) throw std::runtime_error("Value is not a boolean");
    return bool_val;
}

int64_t Value::asInt() const {
    if (isInt()) return int_val;
    if (isFloat()) return static_cast<int64_t>(float_val);
    throw std::runtime_error("Value is not a number");
}

double Value::asFloat() const {
    if (isFloat()) return float_val;
    if (isInt()) return static_cast<double>(int_val);
    throw std::runtime_error("Value is not a number");
}

std::string Value::asString() const {
    if (!isString()) throw std::runtime_error("Value is not a string");
    return *string_val;
}

ValueArray& Value::asArray() {
    if (!isArray()) throw std::runtime_error("Value is not an array");
    return *array_val;
}

const ValueArray& Value::asArray() const {
    if (!isArray()) throw std::runtime_error("Value is not an array");
    return *array_val;
}

ValueMap& Value::asObject() {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    return *object_val;
}

const ValueMap& Value::asObject() const {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    return *object_val;
}

DeviceRef& Value::asDevice() {
    if (!isDevice()) throw std::runtime_error("Value is not a device");
    return *device_val;
}

const DeviceRef& Value::asDevice() const {
    if (!isDevice()) throw std::runtime_error("Value is not a device");
    return *device_val;
}

FunctionObject& Value::asFunction() {
    if (!isFunction()) throw std::runtime_error("Value is not a function");
    return *function_val;
}

const FunctionObject& Value::asFunction() const {
    if (!isFunction()) throw std::runtime_error("Value is not a function");
    return *function_val;
}

NativeFunction& Value::asNativeFunction() {
    if (!isNativeFunction()) throw std::runtime_error("Value is not a native function");
    return *native_function_val;
}

const NativeFunction& Value::asNativeFunction() const {
    if (!isNativeFunction()) throw std::runtime_error("Value is not a native function");
    return *native_function_val;
}

// Factory methods
Value Value::makeNil() { return Value(); }
Value Value::makeBool(bool b) { return Value(b); }
Value Value::makeInt(int64_t i) { return Value(i); }
Value Value::makeFloat(double d) { return Value(d); }
Value Value::makeString(const std::string& s) { return Value(s); }
Value Value::makeArray(const ValueArray& arr) { return Value(arr); }
Value Value::makeObject(const ValueMap& obj) { return Value(obj); }
Value Value::makeDevice(const DeviceRef& dev) { return Value(dev); }
Value Value::makeFunction(const FunctionObject& func) { return Value(func); }
Value Value::makeNativeFunction(NativeFunction func) { return Value(func); }

// Arithmetic operators
Value Value::operator+(const Value& other) const {
    // String concatenation
    if (isString() || other.isString()) {
        return Value(toString() + other.toString());
    }

    // Numeric addition
    if (isFloat() || other.isFloat()) {
        return Value(asFloat() + other.asFloat());
    }
    if (isInt() && other.isInt()) {
        return Value(asInt() + other.asInt());
    }

    throw std::runtime_error("Invalid operands for +");
}

Value Value::operator-(const Value& other) const {
    if (isFloat() || other.isFloat()) {
        return Value(asFloat() - other.asFloat());
    }
    if (isInt() && other.isInt()) {
        return Value(asInt() - other.asInt());
    }
    throw std::runtime_error("Invalid operands for -");
}

Value Value::operator*(const Value& other) const {
    if (isFloat() || other.isFloat()) {
        return Value(asFloat() * other.asFloat());
    }
    if (isInt() && other.isInt()) {
        return Value(asInt() * other.asInt());
    }
    throw std::runtime_error("Invalid operands for *");
}

Value Value::operator/(const Value& other) const {
    if (isFloat() || other.isFloat()) {
        double divisor = other.asFloat();
        if (divisor == 0.0) throw std::runtime_error("Division by zero");
        return Value(asFloat() / divisor);
    }
    if (isInt() && other.isInt()) {
        int64_t divisor = other.asInt();
        if (divisor == 0) throw std::runtime_error("Division by zero");
        return Value(asInt() / divisor);
    }
    throw std::runtime_error("Invalid operands for /");
}

Value Value::operator%(const Value& other) const {
    if (!isInt() || !other.isInt()) {
        throw std::runtime_error("Modulo requires integer operands");
    }
    int64_t divisor = other.asInt();
    if (divisor == 0) throw std::runtime_error("Modulo by zero");
    return Value(asInt() % divisor);
}

// Comparison operators
bool Value::operator==(const Value& other) const {
    if (type_ != other.type_) return false;

    switch (type_) {
        case ValueType::NIL: return true;
        case ValueType::BOOLEAN: return bool_val == other.bool_val;
        case ValueType::INTEGER: return int_val == other.int_val;
        case ValueType::FLOAT: return std::abs(float_val - other.float_val) < 1e-10;
        case ValueType::STRING: return *string_val == *other.string_val;
        case ValueType::ARRAY: return array_val == other.array_val;  // Pointer comparison
        case ValueType::OBJECT: return object_val == other.object_val;
        case ValueType::DEVICE: return device_val->serial == other.device_val->serial;
        default: return false;
    }
}

bool Value::operator!=(const Value& other) const {
    return !(*this == other);
}

bool Value::operator<(const Value& other) const {
    if (isNumber() && other.isNumber()) {
        return asFloat() < other.asFloat();
    }
    if (isString() && other.isString()) {
        return *string_val < *other.string_val;
    }
    throw std::runtime_error("Invalid operands for <");
}

bool Value::operator<=(const Value& other) const {
    return *this < other || *this == other;
}

bool Value::operator>(const Value& other) const {
    return !(*this <= other);
}

bool Value::operator>=(const Value& other) const {
    return !(*this < other);
}

// Unary operators
Value Value::operator-() const {
    if (isInt()) return Value(-asInt());
    if (isFloat()) return Value(-asFloat());
    throw std::runtime_error("Invalid operand for unary -");
}

Value Value::operator!() const {
    return Value(!isTruthy());
}

// Array/Object access
Value& Value::operator[](size_t index) {
    if (!isArray()) throw std::runtime_error("Value is not an array");
    if (index >= array_val->size()) {
        throw std::runtime_error("Array index out of bounds");
    }
    return (*array_val)[index];
}

const Value& Value::operator[](size_t index) const {
    if (!isArray()) throw std::runtime_error("Value is not an array");
    if (index >= array_val->size()) {
        throw std::runtime_error("Array index out of bounds");
    }
    return (*array_val)[index];
}

Value& Value::operator[](const std::string& key) {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    return (*object_val)[key];
}

const Value& Value::operator[](const std::string& key) const {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    auto it = object_val->find(key);
    if (it == object_val->end()) {
        throw std::runtime_error("Key not found: " + key);
    }
    return it->second;
}

// String representation
std::string Value::toString() const {
    std::ostringstream oss;

    switch (type_) {
        case ValueType::NIL:
            return "null";
        case ValueType::BOOLEAN:
            return bool_val ? "true" : "false";
        case ValueType::INTEGER:
            return std::to_string(int_val);
        case ValueType::FLOAT:
            oss << float_val;
            return oss.str();
        case ValueType::STRING:
            return *string_val;
        case ValueType::ARRAY:
            oss << "[";
            for (size_t i = 0; i < array_val->size(); ++i) {
                if (i > 0) oss << ", ";
                oss << (*array_val)[i].toString();
            }
            oss << "]";
            return oss.str();
        case ValueType::OBJECT: {
            oss << "{";
            bool first = true;
            for (const auto& pair : *object_val) {
                if (!first) oss << ", ";
                oss << pair.first << ": " << pair.second.toString();
                first = false;
            }
            oss << "}";
            return oss.str();
        }
        case ValueType::DEVICE:
            return "Device(" + device_val->serial + ")";
        case ValueType::FUNCTION:
            return "<function>";
        case ValueType::NATIVE_FUNCTION:
            return "<native function>";
        default:
            return "<unknown>";
    }
}

std::string Value::typeString() const {
    switch (type_) {
        case ValueType::NIL: return "nil";
        case ValueType::BOOLEAN: return "boolean";
        case ValueType::INTEGER: return "integer";
        case ValueType::FLOAT: return "float";
        case ValueType::STRING: return "string";
        case ValueType::ARRAY: return "array";
        case ValueType::OBJECT: return "object";
        case ValueType::DEVICE: return "device";
        case ValueType::FUNCTION: return "function";
        case ValueType::NATIVE_FUNCTION: return "native_function";
        default: return "unknown";
    }
}

// Truthiness
bool Value::isTruthy() const {
    switch (type_) {
        case ValueType::NIL: return false;
        case ValueType::BOOLEAN: return bool_val;
        case ValueType::INTEGER: return int_val != 0;
        case ValueType::FLOAT: return float_val != 0.0;
        case ValueType::STRING: return !string_val->empty();
        case ValueType::ARRAY: return !array_val->empty();
        case ValueType::OBJECT: return !object_val->empty();
        default: return true;
    }
}

// Array operations
void Value::push(const Value& val) {
    if (!isArray()) throw std::runtime_error("Value is not an array");
    array_val->push_back(val);
}

Value Value::pop() {
    if (!isArray()) throw std::runtime_error("Value is not an array");
    if (array_val->empty()) throw std::runtime_error("Array is empty");
    Value val = array_val->back();
    array_val->pop_back();
    return val;
}

size_t Value::length() const {
    if (isArray()) return array_val->size();
    if (isString()) return string_val->length();
    if (isObject()) return object_val->size();
    throw std::runtime_error("Value does not have a length");
}

// Object operations
bool Value::hasKey(const std::string& key) const {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    return object_val->find(key) != object_val->end();
}

void Value::set(const std::string& key, const Value& val) {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    (*object_val)[key] = val;
}

Value Value::get(const std::string& key) const {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    auto it = object_val->find(key);
    if (it == object_val->end()) return Value::makeNil();
    return it->second;
}

std::vector<std::string> Value::keys() const {
    if (!isObject()) throw std::runtime_error("Value is not an object");
    std::vector<std::string> result;
    for (const auto& pair : *object_val) {
        result.push_back(pair.first);
    }
    return result;
}

// Output operator
std::ostream& operator<<(std::ostream& os, const Value& val) {
    os << val.toString();
    return os;
}

} // namespace androidscript
