import Foundation

/**
 * Value system for AndroidScript runtime
 * Swift implementation using enums with associated values
 */

// Type aliases
typealias NativeFunction = ([Value]) -> Value
typealias ValueArray = [Value]
typealias ValueMap = [String: Value]

// MARK: - Device Reference

struct DeviceRef {
    var serial: String = ""
    var model: String = ""
    var screenWidth: Int = 0
    var screenHeight: Int = 0
    var androidVersion: String = ""
    var nativeHandle: Any?
}

// MARK: - Function Object

class FunctionObject {
    let parameters: [String]
    let body: BlockStmt
    let closure: Environment?

    init(parameters: [String], body: BlockStmt, closure: Environment?) {
        self.parameters = parameters
        self.body = body
        self.closure = closure
    }
}

// MARK: - Environment (for closures)

class Environment {
    private var values: [String: Value] = [:]
    private let parent: Environment?

    init(parent: Environment? = nil) {
        self.parent = parent
    }

    func define(name: String, value: Value) {
        values[name] = value
    }

    func get(name: String) throws -> Value {
        if let value = values[name] {
            return value
        }

        if let parent = parent {
            return try parent.get(name: name)
        }

        throw RuntimeError.undefinedVariable(name)
    }

    func assign(name: String, value: Value) throws {
        if values[name] != nil {
            values[name] = value
            return
        }

        if let parent = parent {
            try parent.assign(name: name, value: value)
            return
        }

        throw RuntimeError.undefinedVariable(name)
    }

    func exists(name: String) -> Bool {
        if values[name] != nil {
            return true
        }
        return parent?.exists(name: name) ?? false
    }

    func isGlobal() -> Bool {
        return parent == nil
    }

    func clear() {
        values.removeAll()
    }
}

// MARK: - Value Enum

enum Value {
    case `nil`
    case bool(Bool)
    case int(Int64)
    case float(Double)
    case string(String)
    case array(ValueArray)
    case object(ValueMap)
    case function(FunctionObject)
    case nativeFunction(NativeFunction)
    case device(DeviceRef)

    // MARK: - Type Checking

    func isNil() -> Bool {
        if case .nil = self { return true }
        return false
    }

    func isBool() -> Bool {
        if case .bool = self { return true }
        return false
    }

    func isInt() -> Bool {
        if case .int = self { return true }
        return false
    }

    func isFloat() -> Bool {
        if case .float = self { return true }
        return false
    }

    func isNumber() -> Bool {
        return isInt() || isFloat()
    }

    func isString() -> Bool {
        if case .string = self { return true }
        return false
    }

    func isArray() -> Bool {
        if case .array = self { return true }
        return false
    }

    func isObject() -> Bool {
        if case .object = self { return true }
        return false
    }

    func isFunction() -> Bool {
        if case .function = self { return true }
        return false
    }

    func isNativeFunction() -> Bool {
        if case .nativeFunction = self { return true }
        return false
    }

    func isCallable() -> Bool {
        return isFunction() || isNativeFunction()
    }

    // MARK: - Type Conversion

    func asBool() throws -> Bool {
        guard case .bool(let value) = self else {
            throw RuntimeError.typeMismatch("Value is not a boolean")
        }
        return value
    }

    func asInt() throws -> Int64 {
        switch self {
        case .int(let value):
            return value
        case .float(let value):
            return Int64(value)
        default:
            throw RuntimeError.typeMismatch("Value is not a number")
        }
    }

    func asFloat() throws -> Double {
        switch self {
        case .float(let value):
            return value
        case .int(let value):
            return Double(value)
        default:
            throw RuntimeError.typeMismatch("Value is not a number")
        }
    }

    func asString() throws -> String {
        guard case .string(let value) = self else {
            throw RuntimeError.typeMismatch("Value is not a string")
        }
        return value
    }

    func asArray() throws -> ValueArray {
        guard case .array(let value) = self else {
            throw RuntimeError.typeMismatch("Value is not an array")
        }
        return value
    }

    func asObject() throws -> ValueMap {
        guard case .object(let value) = self else {
            throw RuntimeError.typeMismatch("Value is not an object")
        }
        return value
    }

    func asFunction() throws -> FunctionObject {
        guard case .function(let value) = self else {
            throw RuntimeError.typeMismatch("Value is not a function")
        }
        return value
    }

    func asNativeFunction() throws -> NativeFunction {
        guard case .nativeFunction(let value) = self else {
            throw RuntimeError.typeMismatch("Value is not a native function")
        }
        return value
    }

    func asDevice() throws -> DeviceRef {
        guard case .device(let value) = self else {
            throw RuntimeError.typeMismatch("Value is not a device")
        }
        return value
    }

    // MARK: - Truthiness

    func isTruthy() -> Bool {
        switch self {
        case .nil:
            return false
        case .bool(let value):
            return value
        case .int(let value):
            return value != 0
        case .float(let value):
            return value != 0.0
        case .string(let value):
            return !value.isEmpty
        case .array(let value):
            return !value.isEmpty
        case .object(let value):
            return !value.isEmpty
        default:
            return true
        }
    }

    // MARK: - String Representation

    func toString() -> String {
        switch self {
        case .nil:
            return "null"
        case .bool(let value):
            return value ? "true" : "false"
        case .int(let value):
            return String(value)
        case .float(let value):
            return String(value)
        case .string(let value):
            return value
        case .array(let elements):
            let items = elements.map { $0.toString() }.joined(separator: ", ")
            return "[\(items)]"
        case .object(let map):
            let items = map.map { "\($0.key): \($0.value.toString())" }.joined(separator: ", ")
            return "{\(items)}"
        case .device(let device):
            return "Device(\(device.serial))"
        case .function:
            return "<function>"
        case .nativeFunction:
            return "<native function>"
        }
    }

    func typeString() -> String {
        switch self {
        case .nil: return "nil"
        case .bool: return "boolean"
        case .int: return "integer"
        case .float: return "float"
        case .string: return "string"
        case .array: return "array"
        case .object: return "object"
        case .function: return "function"
        case .nativeFunction: return "native_function"
        case .device: return "device"
        }
    }

    // MARK: - Arithmetic Operators

    static func +(lhs: Value, rhs: Value) throws -> Value {
        // String concatenation
        if lhs.isString() || rhs.isString() {
            return .string(lhs.toString() + rhs.toString())
        }

        // Numeric addition
        if lhs.isFloat() || rhs.isFloat() {
            return .float(try lhs.asFloat() + try rhs.asFloat())
        }

        if lhs.isInt() && rhs.isInt() {
            return .int(try lhs.asInt() + try rhs.asInt())
        }

        throw RuntimeError.invalidOperands("Invalid operands for +")
    }

    static func -(lhs: Value, rhs: Value) throws -> Value {
        if lhs.isFloat() || rhs.isFloat() {
            return .float(try lhs.asFloat() - try rhs.asFloat())
        }

        if lhs.isInt() && rhs.isInt() {
            return .int(try lhs.asInt() - try rhs.asInt())
        }

        throw RuntimeError.invalidOperands("Invalid operands for -")
    }

    static func *(lhs: Value, rhs: Value) throws -> Value {
        if lhs.isFloat() || rhs.isFloat() {
            return .float(try lhs.asFloat() * try rhs.asFloat())
        }

        if lhs.isInt() && rhs.isInt() {
            return .int(try lhs.asInt() * try rhs.asInt())
        }

        throw RuntimeError.invalidOperands("Invalid operands for *")
    }

    static func /(lhs: Value, rhs: Value) throws -> Value {
        if lhs.isFloat() || rhs.isFloat() {
            let divisor = try rhs.asFloat()
            if divisor == 0.0 {
                throw RuntimeError.divisionByZero
            }
            return .float(try lhs.asFloat() / divisor)
        }

        if lhs.isInt() && rhs.isInt() {
            let divisor = try rhs.asInt()
            if divisor == 0 {
                throw RuntimeError.divisionByZero
            }
            return .int(try lhs.asInt() / divisor)
        }

        throw RuntimeError.invalidOperands("Invalid operands for /")
    }

    static func %(lhs: Value, rhs: Value) throws -> Value {
        guard lhs.isInt() && rhs.isInt() else {
            throw RuntimeError.invalidOperands("Modulo requires integer operands")
        }

        let divisor = try rhs.asInt()
        if divisor == 0 {
            throw RuntimeError.divisionByZero
        }

        return .int(try lhs.asInt() % divisor)
    }

    // MARK: - Unary Operators

    static prefix func -(value: Value) throws -> Value {
        if value.isInt() {
            return .int(-(try value.asInt()))
        }
        if value.isFloat() {
            return .float(-(try value.asFloat()))
        }
        throw RuntimeError.invalidOperands("Invalid operand for unary -")
    }

    static prefix func !(value: Value) -> Value {
        return .bool(!value.isTruthy())
    }

    // MARK: - Comparison Operators

    func equals(_ other: Value) -> Bool {
        switch (self, other) {
        case (.nil, .nil):
            return true
        case (.bool(let a), .bool(let b)):
            return a == b
        case (.int(let a), .int(let b)):
            return a == b
        case (.float(let a), .float(let b)):
            return abs(a - b) < 1e-10
        case (.string(let a), .string(let b)):
            return a == b
        default:
            return false
        }
    }

    func lessThan(_ other: Value) throws -> Bool {
        if self.isNumber() && other.isNumber() {
            return try self.asFloat() < other.asFloat()
        }
        if self.isString() && other.isString() {
            return try self.asString() < other.asString()
        }
        throw RuntimeError.invalidOperands("Invalid operands for <")
    }

    func lessEqual(_ other: Value) throws -> Bool {
        return try lessThan(other) || equals(other)
    }

    func greaterThan(_ other: Value) throws -> Bool {
        return try !lessEqual(other)
    }

    func greaterEqual(_ other: Value) throws -> Bool {
        return try !lessThan(other)
    }

    // MARK: - Array/Object Access

    func length() throws -> Int {
        switch self {
        case .array(let elements):
            return elements.count
        case .string(let value):
            return value.count
        case .object(let map):
            return map.count
        default:
            throw RuntimeError.typeMismatch("Value does not have a length")
        }
    }
}

// MARK: - Runtime Errors

enum RuntimeError: Error {
    case undefinedVariable(String)
    case typeMismatch(String)
    case invalidOperands(String)
    case divisionByZero
    case arrayIndexOutOfBounds(Int)
    case objectKeyNotFound(String)
    case custom(String)

    var localizedDescription: String {
        switch self {
        case .undefinedVariable(let name):
            return "Undefined variable: \(name)"
        case .typeMismatch(let message):
            return message
        case .invalidOperands(let message):
            return message
        case .divisionByZero:
            return "Division by zero"
        case .arrayIndexOutOfBounds(let index):
            return "Array index out of bounds: \(index)"
        case .objectKeyNotFound(let key):
            return "Object key not found: \(key)"
        case .custom(let message):
            return message
        }
    }
}

// MARK: - Control Flow Exceptions

enum ControlFlow: Error {
    case `return`(Value)
    case `break`
    case `continue`
}
