#include "interpreter.h"
#include "environment.h"
#include <sstream>

namespace androidscript {

Interpreter::Interpreter() {
    global_ = std::make_shared<Environment>();
    environment_ = global_;
}

void Interpreter::execute(const std::vector<std::unique_ptr<Statement>>& statements) {
    for (const auto& stmt : statements) {
        try {
            execute(stmt.get());
        } catch (const ReturnException& e) {
            reportError("Return statement outside of function");
        } catch (const BreakException& e) {
            reportError("Break statement outside of loop");
        } catch (const ContinueException& e) {
            reportError("Continue statement outside of loop");
        } catch (const std::exception& e) {
            reportError(std::string("Runtime error: ") + e.what());
        }
    }
}

void Interpreter::execute(Statement* stmt) {
    if (stmt) {
        stmt->accept(*this);
    }
}

Value Interpreter::evaluate(Expression* expr) {
    if (!expr) return Value::makeNil();
    expr->accept(*this);
    return last_value_;
}

void Interpreter::executeBlock(const std::vector<std::unique_ptr<Statement>>& statements,
                               std::shared_ptr<Environment> env) {
    auto previous = environment_;
    try {
        environment_ = env;
        for (const auto& stmt : statements) {
            execute(stmt.get());
        }
        environment_ = previous;
    } catch (...) {
        environment_ = previous;
        throw;
    }
}

Value Interpreter::callFunction(const Value& callee, const std::vector<Value>& args) {
    if (callee.isNativeFunction()) {
        // Call native function
        const NativeFunction& func = callee.asNativeFunction();
        return func(args);
    }

    if (callee.isFunction()) {
        // Call user-defined function
        const FunctionObject& func = callee.asFunction();

        // Check argument count
        if (args.size() != func.parameters.size()) {
            std::ostringstream oss;
            oss << "Expected " << func.parameters.size() << " arguments but got " << args.size();
            throw std::runtime_error(oss.str());
        }

        // Create new environment for function execution
        auto func_env = std::make_shared<Environment>(func.closure);

        // Bind parameters
        for (size_t i = 0; i < args.size(); ++i) {
            func_env->define(func.parameters[i], args[i]);
        }

        // Execute function body
        try {
            auto previous = environment_;
            environment_ = func_env;
            if (func.body) {
                func.body->accept(*this);
            }
            environment_ = previous;
            return Value::makeNil();  // No explicit return
        } catch (const ReturnException& e) {
            return e.value();
        }
    }

    throw std::runtime_error("Value is not callable");
}

void Interpreter::reportError(const std::string& message) {
    errors_.push_back(message);
}

// Expression visitors

void Interpreter::visit(BinaryExpr& expr) {
    Value left = evaluate(expr.left.get());
    Value right = evaluate(expr.right.get());

    switch (expr.op.type) {
        case TokenType::PLUS:
            last_value_ = left + right;
            break;
        case TokenType::MINUS:
            last_value_ = left - right;
            break;
        case TokenType::MULTIPLY:
            last_value_ = left * right;
            break;
        case TokenType::DIVIDE:
            last_value_ = left / right;
            break;
        case TokenType::MODULO:
            last_value_ = left % right;
            break;
        case TokenType::EQUAL:
            last_value_ = Value(left == right);
            break;
        case TokenType::NOT_EQUAL:
            last_value_ = Value(left != right);
            break;
        case TokenType::LESS:
            last_value_ = Value(left < right);
            break;
        case TokenType::LESS_EQUAL:
            last_value_ = Value(left <= right);
            break;
        case TokenType::GREATER:
            last_value_ = Value(left > right);
            break;
        case TokenType::GREATER_EQUAL:
            last_value_ = Value(left >= right);
            break;
        case TokenType::LOGICAL_AND:
            last_value_ = Value(left.isTruthy() && right.isTruthy());
            break;
        case TokenType::LOGICAL_OR:
            last_value_ = Value(left.isTruthy() || right.isTruthy());
            break;
        default:
            throw std::runtime_error("Unknown binary operator");
    }
}

void Interpreter::visit(UnaryExpr& expr) {
    Value operand = evaluate(expr.operand.get());

    switch (expr.op.type) {
        case TokenType::MINUS:
            last_value_ = -operand;
            break;
        case TokenType::LOGICAL_NOT:
            last_value_ = !operand;
            break;
        default:
            throw std::runtime_error("Unknown unary operator");
    }
}

void Interpreter::visit(LiteralExpr& expr) {
    switch (expr.value.type) {
        case TokenType::TRUE:
            last_value_ = Value(true);
            break;
        case TokenType::FALSE:
            last_value_ = Value(false);
            break;
        case TokenType::NULLPTR:
            last_value_ = Value::makeNil();
            break;
        case TokenType::INTEGER:
            last_value_ = Value(expr.value.int_value);
            break;
        case TokenType::FLOAT:
            last_value_ = Value(expr.value.float_value);
            break;
        case TokenType::STRING:
            last_value_ = Value(expr.value.lexeme);
            break;
        default:
            last_value_ = Value::makeNil();
    }
}

void Interpreter::visit(VariableExpr& expr) {
    try {
        last_value_ = environment_->get(expr.name.lexeme);
    } catch (const UndefinedVariableError& e) {
        throw std::runtime_error("Undefined variable: " + expr.name.lexeme);
    }
}

void Interpreter::visit(CallExpr& expr) {
    Value callee = evaluate(expr.callee.get());

    std::vector<Value> args;
    for (const auto& arg : expr.arguments) {
        args.push_back(evaluate(arg.get()));
    }

    last_value_ = callFunction(callee, args);
}

void Interpreter::visit(ArrayExpr& expr) {
    ValueArray elements;
    for (const auto& elem : expr.elements) {
        elements.push_back(evaluate(elem.get()));
    }
    last_value_ = Value::makeArray(elements);
}

void Interpreter::visit(MemberExpr& expr) {
    Value object = evaluate(expr.object.get());

    if (object.isObject()) {
        last_value_ = object.get(expr.member.lexeme);
    } else if (object.isDevice()) {
        // Handle device member access
        DeviceRef& dev = object.asDevice();
        const std::string& member = expr.member.lexeme;

        if (member == "serial") {
            last_value_ = Value(dev.serial);
        } else if (member == "model") {
            last_value_ = Value(dev.model);
        } else if (member == "screenWidth") {
            last_value_ = Value(dev.screen_width);
        } else if (member == "screenHeight") {
            last_value_ = Value(dev.screen_height);
        } else if (member == "androidVersion") {
            last_value_ = Value(dev.android_version);
        } else {
            throw std::runtime_error("Unknown device member: " + member);
        }
    } else {
        throw std::runtime_error("Cannot access member of non-object");
    }
}

void Interpreter::visit(IndexExpr& expr) {
    Value object = evaluate(expr.object.get());
    Value index = evaluate(expr.index.get());

    if (object.isArray()) {
        if (!index.isInt()) {
            throw std::runtime_error("Array index must be an integer");
        }
        size_t idx = static_cast<size_t>(index.asInt());
        last_value_ = object[idx];
    } else if (object.isObject()) {
        if (!index.isString()) {
            throw std::runtime_error("Object key must be a string");
        }
        last_value_ = object[index.asString()];
    } else {
        throw std::runtime_error("Cannot index non-array/object");
    }
}

// Statement visitors

void Interpreter::visit(ExpressionStmt& stmt) {
    evaluate(stmt.expression.get());
}

void Interpreter::visit(AssignmentStmt& stmt) {
    Value value = evaluate(stmt.value.get());
    environment_->assign(stmt.variable.lexeme, value);
}

void Interpreter::visit(BlockStmt& stmt) {
    executeBlock(stmt.statements, std::make_shared<Environment>(environment_));
}

void Interpreter::visit(IfStmt& stmt) {
    Value condition = evaluate(stmt.condition.get());

    if (condition.isTruthy()) {
        execute(stmt.then_branch.get());
    } else if (stmt.else_branch) {
        execute(stmt.else_branch.get());
    }
}

void Interpreter::visit(WhileStmt& stmt) {
    while (evaluate(stmt.condition.get()).isTruthy()) {
        try {
            execute(stmt.body.get());
        } catch (const BreakException&) {
            break;
        } catch (const ContinueException&) {
            continue;
        }
    }
}

void Interpreter::visit(ForStmt& stmt) {
    // Create new scope for loop
    auto loop_env = std::make_shared<Environment>(environment_);
    auto previous = environment_;
    environment_ = loop_env;

    try {
        // Execute initializer
        if (stmt.initializer) {
            execute(stmt.initializer.get());
        }

        // Loop
        while (!stmt.condition || evaluate(stmt.condition.get()).isTruthy()) {
            try {
                execute(stmt.body.get());
            } catch (const BreakException&) {
                break;
            } catch (const ContinueException&) {
                // Continue to increment
            }

            // Execute increment
            if (stmt.increment) {
                execute(stmt.increment.get());
            }
        }

        environment_ = previous;
    } catch (...) {
        environment_ = previous;
        throw;
    }
}

void Interpreter::visit(ForEachStmt& stmt) {
    Value iterable = evaluate(stmt.iterable.get());

    if (!iterable.isArray()) {
        throw std::runtime_error("ForEach requires an array");
    }

    const ValueArray& arr = iterable.asArray();

    for (const Value& item : arr) {
        // Create new scope for each iteration
        auto loop_env = std::make_shared<Environment>(environment_);
        loop_env->define(stmt.variable.lexeme, item);

        try {
            executeBlock({}, loop_env);
            auto previous = environment_;
            environment_ = loop_env;
            execute(stmt.body.get());
            environment_ = previous;
        } catch (const BreakException&) {
            break;
        } catch (const ContinueException&) {
            continue;
        }
    }
}

void Interpreter::visit(FunctionStmt& stmt) {
    // Create function object
    FunctionObject func;
    for (const auto& param : stmt.parameters) {
        func.parameters.push_back(param.lexeme);
    }
    func.body = std::shared_ptr<Statement>(stmt.body.get(), [](Statement*){}); // Non-owning shared_ptr
    func.closure = environment_;

    // Define function in environment
    environment_->define(stmt.name.lexeme, Value::makeFunction(func));
}

void Interpreter::visit(ReturnStmt& stmt) {
    Value value = Value::makeNil();
    if (stmt.value) {
        value = evaluate(stmt.value.get());
    }
    throw ReturnException(value);
}

void Interpreter::visit(BreakStmt&) {
    throw BreakException();
}

void Interpreter::visit(ContinueStmt&) {
    throw ContinueException();
}

} // namespace androidscript
