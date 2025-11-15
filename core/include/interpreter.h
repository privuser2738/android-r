#ifndef ANDROIDSCRIPT_INTERPRETER_H
#define ANDROIDSCRIPT_INTERPRETER_H

#include "ast.h"
#include "value.h"
#include "environment.h"
#include <memory>
#include <vector>
#include <string>

namespace androidscript {

// Control flow exceptions
class ReturnException : public std::exception {
public:
    explicit ReturnException(const Value& val) : value_(val) {}
    const Value& value() const { return value_; }
private:
    Value value_;
};

class BreakException : public std::exception {};
class ContinueException : public std::exception {};

// Interpreter - executes AST
class Interpreter : public ASTVisitor {
public:
    Interpreter();
    ~Interpreter() override = default;

    // Execute a list of statements
    void execute(const std::vector<std::unique_ptr<Statement>>& statements);

    // Execute single statement
    void execute(Statement* stmt);

    // Evaluate expression
    Value evaluate(Expression* expr);

    // Get global environment (for registering built-ins)
    std::shared_ptr<Environment> getGlobalEnvironment() { return global_; }

    // Error handling
    const std::vector<std::string>& getErrors() const { return errors_; }
    bool hasErrors() const { return !errors_.empty(); }

    // Expression visitors
    void visit(BinaryExpr& expr) override;
    void visit(UnaryExpr& expr) override;
    void visit(LiteralExpr& expr) override;
    void visit(VariableExpr& expr) override;
    void visit(CallExpr& expr) override;
    void visit(ArrayExpr& expr) override;
    void visit(MemberExpr& expr) override;
    void visit(IndexExpr& expr) override;

    // Statement visitors
    void visit(ExpressionStmt& stmt) override;
    void visit(AssignmentStmt& stmt) override;
    void visit(BlockStmt& stmt) override;
    void visit(IfStmt& stmt) override;
    void visit(WhileStmt& stmt) override;
    void visit(ForStmt& stmt) override;
    void visit(ForEachStmt& stmt) override;
    void visit(FunctionStmt& stmt) override;
    void visit(ReturnStmt& stmt) override;
    void visit(BreakStmt& stmt) override;
    void visit(ContinueStmt& stmt) override;

private:
    std::shared_ptr<Environment> global_;
    std::shared_ptr<Environment> environment_;
    Value last_value_;  // Last evaluated expression value
    std::vector<std::string> errors_;

    // Helpers
    void executeBlock(const std::vector<std::unique_ptr<Statement>>& statements,
                     std::shared_ptr<Environment> env);
    Value callFunction(const Value& callee, const std::vector<Value>& args);
    void reportError(const std::string& message);
};

} // namespace androidscript

#endif // ANDROIDSCRIPT_INTERPRETER_H
