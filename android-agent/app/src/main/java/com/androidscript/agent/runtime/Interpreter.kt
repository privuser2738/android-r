package com.androidscript.agent.runtime

import android.util.Log

/**
 * Interpreter for AndroidScript
 * Executes Abstract Syntax Tree (AST) using visitor pattern
 */
class Interpreter : ASTVisitor {

    private val global: Environment = Environment()
    private var environment: Environment = global
    private var lastValue: Value = NilValue
    private val errors = mutableListOf<String>()

    companion object {
        private const val TAG = "Interpreter"
    }

    /**
     * Get the global environment (for registering built-in functions)
     */
    fun getGlobalEnvironment(): Environment = global

    /**
     * Get execution errors
     */
    fun getErrors(): List<String> = errors
    fun hasErrors(): Boolean = errors.isNotEmpty()

    /**
     * Execute a list of statements
     */
    fun execute(statements: List<Statement>) {
        for (stmt in statements) {
            try {
                execute(stmt)
            } catch (e: ReturnException) {
                reportError("Return statement outside of function")
            } catch (e: BreakException) {
                reportError("Break statement outside of loop")
            } catch (e: ContinueException) {
                reportError("Continue statement outside of loop")
            } catch (e: Exception) {
                reportError("Runtime error: ${e.message}")
                Log.e(TAG, "Runtime error", e)
            }
        }
    }

    /**
     * Execute a single statement
     */
    fun execute(stmt: Statement) {
        stmt.accept(this)
    }

    /**
     * Evaluate an expression
     */
    fun evaluate(expr: Expression): Value {
        expr.accept(this)
        return lastValue
    }

    /**
     * Execute a block of statements with a new environment
     */
    private fun executeBlock(statements: List<Statement>, env: Environment) {
        val previous = environment
        try {
            environment = env
            for (stmt in statements) {
                execute(stmt)
            }
            environment = previous
        } catch (e: Exception) {
            environment = previous
            throw e
        }
    }

    /**
     * Call a function (native or user-defined)
     */
    private fun callFunction(callee: Value, args: List<Value>): Value {
        // Native function
        if (callee is NativeFunctionValue) {
            return callee.function(args)
        }

        // User-defined function
        if (callee is FunctionValue) {
            val func = callee.function

            // Check argument count
            if (args.size != func.parameters.size) {
                throw RuntimeException(
                    "Expected ${func.parameters.size} arguments but got ${args.size}"
                )
            }

            // Create new environment for function execution
            val funcEnv = Environment(func.closure)

            // Bind parameters
            for (i in args.indices) {
                funcEnv.define(func.parameters[i], args[i])
            }

            // Execute function body
            return try {
                val previous = environment
                environment = funcEnv
                execute(func.body)
                environment = previous
                NilValue  // No explicit return
            } catch (e: ReturnException) {
                e.value
            }
        }

        throw RuntimeException("Value is not callable")
    }

    /**
     * Report runtime error
     */
    private fun reportError(message: String) {
        errors.add(message)
        Log.e(TAG, message)
    }

    // ========================================================================
    // Expression Visitors
    // ========================================================================

    override fun visit(expr: BinaryExpr) {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        lastValue = when (expr.op.type) {
            TokenType.PLUS -> left + right
            TokenType.MINUS -> left - right
            TokenType.MULTIPLY -> left * right
            TokenType.DIVIDE -> left / right
            TokenType.MODULO -> left % right
            TokenType.EQUAL -> BoolValue(left.equals(right))
            TokenType.NOT_EQUAL -> BoolValue(left.notEquals(right))
            TokenType.LESS -> BoolValue(left.lessThan(right))
            TokenType.LESS_EQUAL -> BoolValue(left.lessEqual(right))
            TokenType.GREATER -> BoolValue(left.greaterThan(right))
            TokenType.GREATER_EQUAL -> BoolValue(left.greaterEqual(right))
            TokenType.LOGICAL_AND -> BoolValue(left.isTruthy() && right.isTruthy())
            TokenType.LOGICAL_OR -> BoolValue(left.isTruthy() || right.isTruthy())
            else -> throw RuntimeException("Unknown binary operator: ${expr.op.type}")
        }
    }

    override fun visit(expr: UnaryExpr) {
        val operand = evaluate(expr.operand)

        lastValue = when (expr.op.type) {
            TokenType.MINUS -> -operand
            TokenType.LOGICAL_NOT -> !operand
            else -> throw RuntimeException("Unknown unary operator: ${expr.op.type}")
        }
    }

    override fun visit(expr: LiteralExpr) {
        lastValue = when (expr.value.type) {
            TokenType.TRUE -> BoolValue(true)
            TokenType.FALSE -> BoolValue(false)
            TokenType.NULLPTR -> NilValue
            TokenType.INTEGER -> IntValue(expr.value.intValue)
            TokenType.FLOAT -> FloatValue(expr.value.floatValue)
            TokenType.STRING -> StringValue(expr.value.lexeme)
            else -> NilValue
        }
    }

    override fun visit(expr: VariableExpr) {
        try {
            lastValue = environment.get(expr.name.lexeme)
        } catch (e: UndefinedVariableException) {
            throw RuntimeException("Undefined variable: ${expr.name.lexeme}")
        }
    }

    override fun visit(expr: CallExpr) {
        val callee = evaluate(expr.callee)

        val args = mutableListOf<Value>()
        for (arg in expr.arguments) {
            args.add(evaluate(arg))
        }

        lastValue = callFunction(callee, args)
    }

    override fun visit(expr: ArrayExpr) {
        val elements = mutableListOf<Value>()
        for (elem in expr.elements) {
            elements.add(evaluate(elem))
        }
        lastValue = ArrayValue(elements)
    }

    override fun visit(expr: MemberExpr) {
        val obj = evaluate(expr.obj)

        lastValue = when (obj) {
            is ObjectValue -> obj[expr.member.lexeme]
            is DeviceValue -> {
                val dev = obj.device
                when (expr.member.lexeme) {
                    "serial" -> StringValue(dev.serial)
                    "model" -> StringValue(dev.model)
                    "screenWidth" -> IntValue(dev.screenWidth.toLong())
                    "screenHeight" -> IntValue(dev.screenHeight.toLong())
                    "androidVersion" -> StringValue(dev.androidVersion)
                    else -> throw RuntimeException("Unknown device member: ${expr.member.lexeme}")
                }
            }
            else -> throw RuntimeException("Cannot access member of non-object")
        }
    }

    override fun visit(expr: IndexExpr) {
        val obj = evaluate(expr.obj)
        val index = evaluate(expr.index)

        lastValue = when (obj) {
            is ArrayValue -> {
                if (!index.isInt()) {
                    throw RuntimeException("Array index must be an integer")
                }
                val idx = index.asInt().toInt()
                obj[idx]
            }
            is ObjectValue -> {
                if (!index.isString()) {
                    throw RuntimeException("Object key must be a string")
                }
                obj[index.asString()]
            }
            else -> throw RuntimeException("Cannot index non-array/object")
        }
    }

    // ========================================================================
    // Statement Visitors
    // ========================================================================

    override fun visit(stmt: ExpressionStmt) {
        evaluate(stmt.expression)
    }

    override fun visit(stmt: AssignmentStmt) {
        val value = evaluate(stmt.value)

        try {
            // Try to assign to existing variable
            environment.assign(stmt.variable.lexeme, value)
        } catch (e: UndefinedVariableException) {
            // If variable doesn't exist, define it
            environment.define(stmt.variable.lexeme, value)
        }
    }

    override fun visit(stmt: BlockStmt) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visit(stmt: IfStmt) {
        val condition = evaluate(stmt.condition)

        if (condition.isTruthy()) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visit(stmt: WhileStmt) {
        while (evaluate(stmt.condition).isTruthy()) {
            try {
                execute(stmt.body)
            } catch (e: BreakException) {
                break
            } catch (e: ContinueException) {
                continue
            }
        }
    }

    override fun visit(stmt: ForStmt) {
        // Create new scope for loop
        val loopEnv = Environment(environment)
        val previous = environment
        environment = loopEnv

        try {
            // Execute initializer
            if (stmt.initializer != null) {
                execute(stmt.initializer)
            }

            // Loop
            while (stmt.condition == null || evaluate(stmt.condition).isTruthy()) {
                try {
                    execute(stmt.body)
                } catch (e: BreakException) {
                    break
                } catch (e: ContinueException) {
                    // Continue to increment
                }

                // Execute increment
                if (stmt.increment != null) {
                    execute(stmt.increment)
                }
            }

            environment = previous
        } catch (e: Exception) {
            environment = previous
            throw e
        }
    }

    override fun visit(stmt: ForEachStmt) {
        val iterable = evaluate(stmt.iterable)

        if (iterable !is ArrayValue) {
            throw RuntimeException("ForEach requires an array")
        }

        for (item in iterable.elements) {
            // Create new scope for each iteration
            val loopEnv = Environment(environment)
            loopEnv.define(stmt.variable.lexeme, item)

            try {
                val previous = environment
                environment = loopEnv
                execute(stmt.body)
                environment = previous
            } catch (e: BreakException) {
                break
            } catch (e: ContinueException) {
                continue
            }
        }
    }

    override fun visit(stmt: FunctionStmt) {
        // Create function object
        val parameters = stmt.parameters.map { it.lexeme }
        val func = FunctionObject(parameters, stmt.body, environment)

        // Define function in environment
        environment.define(stmt.name.lexeme, FunctionValue(func))
    }

    override fun visit(stmt: ReturnStmt) {
        val value = if (stmt.value != null) {
            evaluate(stmt.value)
        } else {
            NilValue
        }
        throw ReturnException(value)
    }

    override fun visit(stmt: BreakStmt) {
        throw BreakException()
    }

    override fun visit(stmt: ContinueStmt) {
        throw ContinueException()
    }
}

// ========================================================================
// Control Flow Exceptions
// ========================================================================

class ReturnException(val value: Value) : Exception("Return")
class BreakException : Exception("Break")
class ContinueException : Exception("Continue")
