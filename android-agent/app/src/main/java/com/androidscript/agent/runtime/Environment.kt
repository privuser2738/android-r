package com.androidscript.agent.runtime

/**
 * Runtime environment for variable storage and scoping
 * Supports nested scopes with parent chain lookup
 */
class Environment(private val parent: Environment? = null) {

    private val values = mutableMapOf<String, Value>()

    /**
     * Define a new variable in this environment
     */
    fun define(name: String, value: Value) {
        values[name] = value
    }

    /**
     * Get a variable value (searches parent chain if not found locally)
     */
    fun get(name: String): Value {
        if (values.containsKey(name)) {
            return values[name]!!
        }

        if (parent != null) {
            return parent.get(name)
        }

        throw UndefinedVariableException(name)
    }

    /**
     * Assign to an existing variable (searches parent chain)
     */
    fun assign(name: String, value: Value) {
        if (values.containsKey(name)) {
            values[name] = value
            return
        }

        if (parent != null) {
            parent.assign(name, value)
            return
        }

        throw UndefinedVariableException(name)
    }

    /**
     * Check if variable exists in this environment or parent chain
     */
    fun exists(name: String): Boolean {
        if (values.containsKey(name)) {
            return true
        }

        return parent?.exists(name) ?: false
    }

    /**
     * Get the parent environment
     */
    fun getParent(): Environment? = parent

    /**
     * Check if this is the global environment
     */
    fun isGlobal(): Boolean = parent == null

    /**
     * Clear all variables in this environment
     */
    fun clear() {
        values.clear()
    }

    /**
     * Get all variables in this environment (for debugging)
     */
    fun getVariables(): Map<String, Value> = values.toMap()
}

/**
 * Exception thrown when accessing undefined variables
 */
class UndefinedVariableException(val variableName: String) :
    RuntimeException("Undefined variable: $variableName")
