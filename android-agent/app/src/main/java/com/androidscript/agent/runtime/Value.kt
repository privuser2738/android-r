package com.androidscript.agent.runtime

import kotlin.math.abs

/**
 * Value system for AndroidScript runtime
 * Represents all possible value types in the language
 */

// Type aliases for clarity
typealias NativeFunction = (List<Value>) -> Value
typealias ValueArray = MutableList<Value>
typealias ValueMap = MutableMap<String, Value>

// ========================================================================
// Value Type Enum
// ========================================================================

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
}

// ========================================================================
// Device Reference (for multi-device support)
// ========================================================================

data class DeviceRef(
    val serial: String = "",
    val model: String = "",
    var screenWidth: Int = 0,
    var screenHeight: Int = 0,
    val androidVersion: String = "",
    var nativeHandle: Any? = null  // Platform-specific handle
)

// ========================================================================
// Function Object (user-defined functions)
// ========================================================================

data class FunctionObject(
    val parameters: List<String>,
    val body: BlockStmt,
    val closure: Environment?
)

// ========================================================================
// Main Value Class (sealed class hierarchy)
// ========================================================================

sealed class Value {
    abstract val type: ValueType

    // Type checking methods
    fun isNil() = this is NilValue
    fun isBool() = this is BoolValue
    fun isInt() = this is IntValue
    fun isFloat() = this is FloatValue
    fun isNumber() = isInt() || isFloat()
    fun isString() = this is StringValue
    fun isArray() = this is ArrayValue
    fun isObject() = this is ObjectValue
    fun isFunction() = this is FunctionValue
    fun isNativeFunction() = this is NativeFunctionValue
    fun isDevice() = this is DeviceValue
    fun isCallable() = isFunction() || isNativeFunction()

    // Type conversion methods (with runtime checks)
    fun asBool(): Boolean = when (this) {
        is BoolValue -> value
        else -> throw RuntimeException("Value is not a boolean")
    }

    fun asInt(): Long = when (this) {
        is IntValue -> value
        is FloatValue -> value.toLong()
        else -> throw RuntimeException("Value is not a number")
    }

    fun asFloat(): Double = when (this) {
        is FloatValue -> value
        is IntValue -> value.toDouble()
        else -> throw RuntimeException("Value is not a number")
    }

    fun asString(): String = when (this) {
        is StringValue -> value
        else -> throw RuntimeException("Value is not a string")
    }

    fun asArray(): ValueArray = when (this) {
        is ArrayValue -> elements
        else -> throw RuntimeException("Value is not an array")
    }

    fun asObject(): ValueMap = when (this) {
        is ObjectValue -> properties
        else -> throw RuntimeException("Value is not an object")
    }

    fun asDevice(): DeviceRef = when (this) {
        is DeviceValue -> device
        else -> throw RuntimeException("Value is not a device")
    }

    fun asFunction(): FunctionObject = when (this) {
        is FunctionValue -> function
        else -> throw RuntimeException("Value is not a function")
    }

    fun asNativeFunction(): NativeFunction = when (this) {
        is NativeFunctionValue -> function
        else -> throw RuntimeException("Value is not a native function")
    }

    // Truthiness (for conditionals)
    fun isTruthy(): Boolean = when (this) {
        is NilValue -> false
        is BoolValue -> value
        is IntValue -> value != 0L
        is FloatValue -> value != 0.0
        is StringValue -> value.isNotEmpty()
        is ArrayValue -> elements.isNotEmpty()
        is ObjectValue -> properties.isNotEmpty()
        else -> true
    }

    // String representation
    fun typeString(): String = when (this) {
        is NilValue -> "nil"
        is BoolValue -> "boolean"
        is IntValue -> "integer"
        is FloatValue -> "float"
        is StringValue -> "string"
        is ArrayValue -> "array"
        is ObjectValue -> "object"
        is FunctionValue -> "function"
        is NativeFunctionValue -> "native_function"
        is DeviceValue -> "device"
    }

    override fun toString(): String = when (this) {
        is NilValue -> "null"
        is BoolValue -> if (value) "true" else "false"
        is IntValue -> value.toString()
        is FloatValue -> value.toString()
        is StringValue -> value
        is ArrayValue -> "[${elements.joinToString(", ") { it.toString() }}]"
        is ObjectValue -> "{${properties.entries.joinToString(", ") { "${it.key}: ${it.value}" }}}"
        is DeviceValue -> "Device(${device.serial})"
        is FunctionValue -> "<function>"
        is NativeFunctionValue -> "<native function>"
    }

    // ========================================================================
    // Arithmetic Operators
    // ========================================================================

    operator fun plus(other: Value): Value {
        // String concatenation
        if (this is StringValue || other is StringValue) {
            return StringValue(this.toString() + other.toString())
        }

        // Numeric addition
        if (this.isFloat() || other.isFloat()) {
            return FloatValue(this.asFloat() + other.asFloat())
        }
        if (this.isInt() && other.isInt()) {
            return IntValue(this.asInt() + other.asInt())
        }

        throw RuntimeException("Invalid operands for +")
    }

    operator fun minus(other: Value): Value {
        if (this.isFloat() || other.isFloat()) {
            return FloatValue(this.asFloat() - other.asFloat())
        }
        if (this.isInt() && other.isInt()) {
            return IntValue(this.asInt() - other.asInt())
        }
        throw RuntimeException("Invalid operands for -")
    }

    operator fun times(other: Value): Value {
        if (this.isFloat() || other.isFloat()) {
            return FloatValue(this.asFloat() * other.asFloat())
        }
        if (this.isInt() && other.isInt()) {
            return IntValue(this.asInt() * other.asInt())
        }
        throw RuntimeException("Invalid operands for *")
    }

    operator fun div(other: Value): Value {
        if (this.isFloat() || other.isFloat()) {
            val divisor = other.asFloat()
            if (divisor == 0.0) throw RuntimeException("Division by zero")
            return FloatValue(this.asFloat() / divisor)
        }
        if (this.isInt() && other.isInt()) {
            val divisor = other.asInt()
            if (divisor == 0L) throw RuntimeException("Division by zero")
            return IntValue(this.asInt() / divisor)
        }
        throw RuntimeException("Invalid operands for /")
    }

    operator fun rem(other: Value): Value {
        if (!this.isInt() || !other.isInt()) {
            throw RuntimeException("Modulo requires integer operands")
        }
        val divisor = other.asInt()
        if (divisor == 0L) throw RuntimeException("Modulo by zero")
        return IntValue(this.asInt() % divisor)
    }

    // ========================================================================
    // Comparison Operators
    // ========================================================================

    fun equals(other: Value): Boolean {
        if (this.type != other.type) return false

        return when (this) {
            is NilValue -> true
            is BoolValue -> value == (other as BoolValue).value
            is IntValue -> value == (other as IntValue).value
            is FloatValue -> abs(value - (other as FloatValue).value) < 1e-10
            is StringValue -> value == (other as StringValue).value
            is ArrayValue -> elements === (other as ArrayValue).elements  // Reference equality
            is ObjectValue -> properties === (other as ObjectValue).properties
            is DeviceValue -> device.serial == (other as DeviceValue).device.serial
            else -> false
        }
    }

    fun notEquals(other: Value): Boolean = !equals(other)

    fun lessThan(other: Value): Boolean {
        if (this.isNumber() && other.isNumber()) {
            return this.asFloat() < other.asFloat()
        }
        if (this is StringValue && other is StringValue) {
            return value < other.value
        }
        throw RuntimeException("Invalid operands for <")
    }

    fun lessEqual(other: Value): Boolean = lessThan(other) || equals(other)
    fun greaterThan(other: Value): Boolean = !lessEqual(other)
    fun greaterEqual(other: Value): Boolean = !lessThan(other)

    // ========================================================================
    // Unary Operators
    // ========================================================================

    operator fun unaryMinus(): Value {
        return when (this) {
            is IntValue -> IntValue(-value)
            is FloatValue -> FloatValue(-value)
            else -> throw RuntimeException("Invalid operand for unary -")
        }
    }

    operator fun not(): Value = BoolValue(!isTruthy())

    // ========================================================================
    // Array/Object Access
    // ========================================================================

    operator fun get(index: Int): Value {
        if (this !is ArrayValue) throw RuntimeException("Value is not an array")
        if (index < 0 || index >= elements.size) {
            throw RuntimeException("Array index out of bounds: $index")
        }
        return elements[index]
    }

    operator fun get(key: String): Value {
        if (this !is ObjectValue) throw RuntimeException("Value is not an object")
        return properties[key] ?: NilValue
    }

    operator fun set(index: Int, value: Value) {
        if (this !is ArrayValue) throw RuntimeException("Value is not an array")
        if (index < 0 || index >= elements.size) {
            throw RuntimeException("Array index out of bounds: $index")
        }
        elements[index] = value
    }

    operator fun set(key: String, value: Value) {
        if (this !is ObjectValue) throw RuntimeException("Value is not an object")
        properties[key] = value
    }

    // ========================================================================
    // Length Operation
    // ========================================================================

    fun length(): Int = when (this) {
        is ArrayValue -> elements.size
        is StringValue -> value.length
        is ObjectValue -> properties.size
        else -> throw RuntimeException("Value does not have a length")
    }
}

// ========================================================================
// Concrete Value Types
// ========================================================================

object NilValue : Value() {
    override val type = ValueType.NIL
}

data class BoolValue(val value: Boolean) : Value() {
    override val type = ValueType.BOOLEAN
}

data class IntValue(val value: Long) : Value() {
    override val type = ValueType.INTEGER
}

data class FloatValue(val value: Double) : Value() {
    override val type = ValueType.FLOAT
}

data class StringValue(val value: String) : Value() {
    override val type = ValueType.STRING
}

data class ArrayValue(val elements: ValueArray = mutableListOf()) : Value() {
    override val type = ValueType.ARRAY

    fun push(value: Value) = elements.add(value)

    fun pop(): Value {
        if (elements.isEmpty()) throw RuntimeException("Array is empty")
        return elements.removeAt(elements.size - 1)
    }
}

data class ObjectValue(val properties: ValueMap = mutableMapOf()) : Value() {
    override val type = ValueType.OBJECT

    fun hasKey(key: String): Boolean = properties.containsKey(key)

    fun keys(): List<String> = properties.keys.toList()
}

data class FunctionValue(val function: FunctionObject) : Value() {
    override val type = ValueType.FUNCTION
}

data class NativeFunctionValue(val function: NativeFunction) : Value() {
    override val type = ValueType.NATIVE_FUNCTION
}

data class DeviceValue(val device: DeviceRef) : Value() {
    override val type = ValueType.DEVICE
}

// ========================================================================
// Factory Methods (companion object pattern)
// ========================================================================

object Values {
    fun makeNil() = NilValue
    fun makeBool(b: Boolean) = BoolValue(b)
    fun makeInt(i: Long) = IntValue(i)
    fun makeInt(i: Int) = IntValue(i.toLong())
    fun makeFloat(d: Double) = FloatValue(d)
    fun makeString(s: String) = StringValue(s)
    fun makeArray(elements: ValueArray = mutableListOf()) = ArrayValue(elements)
    fun makeObject(properties: ValueMap = mutableMapOf()) = ObjectValue(properties)
    fun makeFunction(function: FunctionObject) = FunctionValue(function)
    fun makeNativeFunction(function: NativeFunction) = NativeFunctionValue(function)
    fun makeDevice(device: DeviceRef) = DeviceValue(device)
}
