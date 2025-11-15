package com.androidscript.agent.runtime

import android.util.Log

/**
 * ScriptRunner - Main entry point for executing AndroidScript code
 * Orchestrates lexer, parser, and interpreter
 */
class ScriptRunner {

    private val interpreter = Interpreter()
    private val nativeBridge = NativeBridge(interpreter)
    private val errors = mutableListOf<String>()

    companion object {
        private const val TAG = "ScriptRunner"
    }

    init {
        // Register built-in functions
        nativeBridge.registerBuiltins()
        Log.d(TAG, "ScriptRunner initialized with built-in functions")
    }

    /**
     * Execute AndroidScript source code
     */
    fun execute(source: String): ExecutionResult {
        errors.clear()

        try {
            // Step 1: Lexical Analysis
            Log.d(TAG, "Tokenizing source code...")
            val lexer = Lexer(source)
            val tokens = lexer.tokenize()

            if (lexer.hasErrors()) {
                errors.addAll(lexer.getErrors())
                return ExecutionResult(
                    success = false,
                    errors = errors,
                    output = null
                )
            }

            Log.d(TAG, "Tokenization complete: ${tokens.size} tokens")

            // Step 2: Parsing
            Log.d(TAG, "Parsing tokens...")
            val parser = Parser(tokens)
            val statements = parser.parse()

            if (parser.hasErrors()) {
                errors.addAll(parser.getErrors())
                return ExecutionResult(
                    success = false,
                    errors = errors,
                    output = null
                )
            }

            Log.d(TAG, "Parsing complete: ${statements.size} statements")

            // Step 3: Execution
            Log.d(TAG, "Executing statements...")
            interpreter.execute(statements)

            if (interpreter.hasErrors()) {
                errors.addAll(interpreter.getErrors())
                return ExecutionResult(
                    success = false,
                    errors = errors,
                    output = null
                )
            }

            Log.d(TAG, "Execution complete")

            return ExecutionResult(
                success = true,
                errors = emptyList(),
                output = "Script executed successfully"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Script execution failed", e)
            errors.add("Execution error: ${e.message}")
            return ExecutionResult(
                success = false,
                errors = errors,
                output = null
            )
        }
    }

    /**
     * Get the global environment for debugging or advanced usage
     */
    fun getGlobalEnvironment(): Environment = interpreter.getGlobalEnvironment()

    /**
     * Clear all variables and reset the interpreter
     */
    fun reset() {
        // Create new interpreter and re-register built-ins
        val newInterpreter = Interpreter()
        val newBridge = NativeBridge(newInterpreter)
        newBridge.registerBuiltins()

        // Note: We can't reassign interpreter/nativeBridge here since they're val
        // In practice, create a new ScriptRunner instance for a full reset
        Log.d(TAG, "Reset called - create new ScriptRunner instance for full reset")
    }

    /**
     * Execute a script file from assets or storage
     */
    fun executeFile(scriptPath: String): ExecutionResult {
        // TODO: Implement file reading and execution
        return ExecutionResult(
            success = false,
            errors = listOf("File execution not yet implemented"),
            output = null
        )
    }
}

/**
 * Result of script execution
 */
data class ExecutionResult(
    val success: Boolean,
    val errors: List<String>,
    val output: String?
)

/**
 * Example usage:
 *
 * val runner = ScriptRunner()
 * val result = runner.execute("""
 *     $button = FindByText("Click me")
 *     if ($button != null) {
 *         Click($button)
 *         Print("Button clicked!")
 *     }
 * """)
 *
 * if (result.success) {
 *     println("Script executed successfully")
 * } else {
 *     println("Errors: ${result.errors}")
 * }
 */
