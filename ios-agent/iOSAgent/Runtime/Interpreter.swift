import Foundation

/**
 * Interpreter for AndroidScript
 * Executes Abstract Syntax Tree using visitor pattern
 */
class Interpreter: ASTVisitor {
    private let global: Environment
    private var environment: Environment
    private var lastValue: Value = .nil
    private var errors = [String]()

    init() {
        self.global = Environment()
        self.environment = global
    }

    // MARK: - Public Interface

    func getGlobalEnvironment() -> Environment {
        return global
    }

    func execute(statements: [Statement]) {
        for stmt in statements {
            do {
                try execute(stmt: stmt)
            } catch ControlFlow.return {
                reportError("Return statement outside of function")
            } catch ControlFlow.break {
                reportError("Break statement outside of loop")
            } catch ControlFlow.continue {
                reportError("Continue statement outside of loop")
            } catch {
                reportError("Runtime error: \(error.localizedDescription)")
            }
        }
    }

    func hasErrors() -> Bool {
        return !errors.isEmpty
    }

    func getErrors() -> [String] {
        return errors
    }

    // MARK: - Execution

    private func execute(stmt: Statement) throws {
        stmt.accept(visitor: self)
    }

    private func evaluate(expr: Expression) throws -> Value {
        expr.accept(visitor: self)
        return lastValue
    }

    private func executeBlock(statements: [Statement], environment: Environment) throws {
        let previous = self.environment
        defer { self.environment = previous }

        self.environment = environment

        for stmt in statements {
            try execute(stmt: stmt)
        }
    }

    private func callFunction(callee: Value, args: [Value]) throws -> Value {
        // Native function
        if case .nativeFunction(let function) = callee {
            return function(args)
        }

        // User-defined function
        if case .function(let function) = callee {
            // Check argument count
            guard args.count == function.parameters.count else {
                throw RuntimeError.custom("Expected \(function.parameters.count) arguments but got \(args.count)")
            }

            // Create new environment for function execution
            let funcEnv = Environment(parent: function.closure)

            // Bind parameters
            for (i, param) in function.parameters.enumerated() {
                funcEnv.define(name: param, value: args[i])
            }

            // Execute function body
            do {
                try executeBlock(statements: function.body.statements, environment: funcEnv)
                return .nil  // No explicit return
            } catch ControlFlow.return(let value) {
                return value
            }
        }

        throw RuntimeError.custom("Value is not callable")
    }

    // MARK: - Expression Visitors

    func visit(expr: BinaryExpr) {
        do {
            let left = try evaluate(expr: expr.left)
            let right = try evaluate(expr: expr.right)

            switch expr.op.type {
            case .plus:
                lastValue = try left + right
            case .minus:
                lastValue = try left - right
            case .multiply:
                lastValue = try left * right
            case .divide:
                lastValue = try left / right
            case .modulo:
                lastValue = try left % right
            case .equal:
                lastValue = .bool(left.equals(right))
            case .notEqual:
                lastValue = .bool(!left.equals(right))
            case .less:
                lastValue = .bool(try left.lessThan(right))
            case .lessEqual:
                lastValue = .bool(try left.lessEqual(right))
            case .greater:
                lastValue = .bool(try left.greaterThan(right))
            case .greaterEqual:
                lastValue = .bool(try left.greaterEqual(right))
            case .logicalAnd:
                lastValue = .bool(left.isTruthy() && right.isTruthy())
            case .logicalOr:
                lastValue = .bool(left.isTruthy() || right.isTruthy())
            default:
                throw RuntimeError.custom("Unknown binary operator: \(expr.op.type)")
            }
        } catch {
            reportError("Binary operation error: \(error.localizedDescription)")
            lastValue = .nil
        }
    }

    func visit(expr: UnaryExpr) {
        do {
            let operand = try evaluate(expr: expr.operand)

            switch expr.op.type {
            case .minus:
                lastValue = try -operand
            case .logicalNot:
                lastValue = !operand
            default:
                throw RuntimeError.custom("Unknown unary operator: \(expr.op.type)")
            }
        } catch {
            reportError("Unary operation error: \(error.localizedDescription)")
            lastValue = .nil
        }
    }

    func visit(expr: LiteralExpr) {
        switch expr.value.type {
        case .true:
            lastValue = .bool(true)
        case .false:
            lastValue = .bool(false)
        case .nullptr:
            lastValue = .nil
        case .integer:
            lastValue = .int(expr.value.intValue)
        case .float:
            lastValue = .float(expr.value.floatValue)
        case .string:
            lastValue = .string(expr.value.lexeme)
        default:
            lastValue = .nil
        }
    }

    func visit(expr: VariableExpr) {
        do {
            lastValue = try environment.get(name: expr.name.lexeme)
        } catch {
            reportError("Undefined variable: \(expr.name.lexeme)")
            lastValue = .nil
        }
    }

    func visit(expr: CallExpr) {
        do {
            let callee = try evaluate(expr: expr.callee)

            var args = [Value]()
            for arg in expr.arguments {
                args.append(try evaluate(expr: arg))
            }

            lastValue = try callFunction(callee: callee, args: args)
        } catch {
            reportError("Function call error: \(error.localizedDescription)")
            lastValue = .nil
        }
    }

    func visit(expr: ArrayExpr) {
        do {
            var elements = [Value]()
            for elem in expr.elements {
                elements.append(try evaluate(expr: elem))
            }
            lastValue = .array(elements)
        } catch {
            reportError("Array creation error: \(error.localizedDescription)")
            lastValue = .nil
        }
    }

    func visit(expr: MemberExpr) {
        do {
            let object = try evaluate(expr: expr.object)

            if case .object(let map) = object {
                lastValue = map[expr.member.lexeme] ?? .nil
            } else if case .device(let device) = object {
                // Handle device member access
                switch expr.member.lexeme {
                case "serial":
                    lastValue = .string(device.serial)
                case "model":
                    lastValue = .string(device.model)
                case "screenWidth":
                    lastValue = .int(Int64(device.screenWidth))
                case "screenHeight":
                    lastValue = .int(Int64(device.screenHeight))
                case "androidVersion":
                    lastValue = .string(device.androidVersion)
                default:
                    throw RuntimeError.custom("Unknown device member: \(expr.member.lexeme)")
                }
            } else {
                throw RuntimeError.custom("Cannot access member of non-object")
            }
        } catch {
            reportError("Member access error: \(error.localizedDescription)")
            lastValue = .nil
        }
    }

    func visit(expr: IndexExpr) {
        do {
            let object = try evaluate(expr: expr.object)
            let index = try evaluate(expr: expr.index)

            if case .array(let elements) = object {
                guard case .int(let idx) = index else {
                    throw RuntimeError.custom("Array index must be an integer")
                }
                guard idx >= 0 && idx < elements.count else {
                    throw RuntimeError.arrayIndexOutOfBounds(Int(idx))
                }
                lastValue = elements[Int(idx)]
            } else if case .object(let map) = object {
                guard case .string(let key) = index else {
                    throw RuntimeError.custom("Object key must be a string")
                }
                lastValue = map[key] ?? .nil
            } else {
                throw RuntimeError.custom("Cannot index non-array/object")
            }
        } catch {
            reportError("Index access error: \(error.localizedDescription)")
            lastValue = .nil
        }
    }

    // MARK: - Statement Visitors

    func visit(stmt: ExpressionStmt) {
        do {
            _ = try evaluate(expr: stmt.expression)
        } catch {
            reportError("Expression error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: AssignmentStmt) {
        do {
            let value = try evaluate(expr: stmt.value)

            // Try to assign to existing variable
            do {
                try environment.assign(name: stmt.variable.lexeme, value: value)
            } catch RuntimeError.undefinedVariable {
                // If variable doesn't exist, define it
                environment.define(name: stmt.variable.lexeme, value: value)
            }
        } catch {
            reportError("Assignment error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: BlockStmt) {
        do {
            try executeBlock(statements: stmt.statements, environment: Environment(parent: environment))
        } catch {
            reportError("Block execution error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: IfStmt) {
        do {
            let condition = try evaluate(expr: stmt.condition)

            if condition.isTruthy() {
                try execute(stmt: stmt.thenBranch)
            } else if let elseBranch = stmt.elseBranch {
                try execute(stmt: elseBranch)
            }
        } catch {
            reportError("If statement error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: WhileStmt) {
        do {
            while try evaluate(expr: stmt.condition).isTruthy() {
                do {
                    try execute(stmt: stmt.body)
                } catch ControlFlow.break {
                    break
                } catch ControlFlow.continue {
                    continue
                }
            }
        } catch {
            reportError("While loop error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: ForStmt) {
        do {
            // Create new scope for loop
            let loopEnv = Environment(parent: environment)
            let previous = environment
            environment = loopEnv

            defer { environment = previous }

            // Execute initializer
            if let initializer = stmt.initializer {
                try execute(stmt: initializer)
            }

            // Loop
            while stmt.condition == nil || try evaluate(expr: stmt.condition!).isTruthy() {
                do {
                    try execute(stmt: stmt.body)
                } catch ControlFlow.break {
                    break
                } catch ControlFlow.continue {
                    // Continue to increment
                }

                // Execute increment
                if let increment = stmt.increment {
                    try execute(stmt: increment)
                }
            }
        } catch {
            reportError("For loop error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: ForEachStmt) {
        do {
            let iterable = try evaluate(expr: stmt.iterable)

            guard case .array(let elements) = iterable else {
                throw RuntimeError.custom("ForEach requires an array")
            }

            for item in elements {
                // Create new scope for each iteration
                let loopEnv = Environment(parent: environment)
                loopEnv.define(name: stmt.variable.lexeme, value: item)

                do {
                    try executeBlock(statements: [stmt.body], environment: loopEnv)
                } catch ControlFlow.break {
                    break
                } catch ControlFlow.continue {
                    continue
                }
            }
        } catch {
            reportError("ForEach loop error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: FunctionStmt) {
        let parameters = stmt.parameters.map { $0.lexeme }
        let function = FunctionObject(parameters: parameters, body: stmt.body, closure: environment)

        environment.define(name: stmt.name.lexeme, value: .function(function))
    }

    func visit(stmt: ReturnStmt) {
        do {
            let value: Value
            if let returnValue = stmt.value {
                value = try evaluate(expr: returnValue)
            } else {
                value = .nil
            }
            throw ControlFlow.return(value)
        } catch ControlFlow.return {
            throw ControlFlow.return(lastValue)
        } catch {
            reportError("Return statement error: \(error.localizedDescription)")
        }
    }

    func visit(stmt: BreakStmt) {
        throw ControlFlow.break
    }

    func visit(stmt: ContinueStmt) {
        throw ControlFlow.continue
    }

    // MARK: - Error Reporting

    private func reportError(_ message: String) {
        errors.append(message)
        print("Interpreter error: \(message)")
    }
}
