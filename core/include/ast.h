#ifndef ANDROIDSCRIPT_AST_H
#define ANDROIDSCRIPT_AST_H

#include "token.h"
#include <memory>
#include <vector>
#include <string>

namespace androidscript {

// Forward declarations
class ASTVisitor;

// Base AST Node
class ASTNode {
public:
    virtual ~ASTNode() = default;
    virtual void accept(ASTVisitor& visitor) = 0;
};

// Expression nodes
class Expression : public ASTNode {
public:
    virtual ~Expression() = default;
};

class BinaryExpr : public Expression {
public:
    std::unique_ptr<Expression> left;
    Token op;
    std::unique_ptr<Expression> right;

    BinaryExpr(std::unique_ptr<Expression> l, Token o, std::unique_ptr<Expression> r)
        : left(std::move(l)), op(o), right(std::move(r)) {}

    void accept(ASTVisitor& visitor) override;
};

class UnaryExpr : public Expression {
public:
    Token op;
    std::unique_ptr<Expression> operand;

    UnaryExpr(Token o, std::unique_ptr<Expression> expr)
        : op(o), operand(std::move(expr)) {}

    void accept(ASTVisitor& visitor) override;
};

class LiteralExpr : public Expression {
public:
    Token value;

    explicit LiteralExpr(Token val) : value(val) {}

    void accept(ASTVisitor& visitor) override;
};

class VariableExpr : public Expression {
public:
    Token name;

    explicit VariableExpr(Token n) : name(n) {}

    void accept(ASTVisitor& visitor) override;
};

class CallExpr : public Expression {
public:
    std::unique_ptr<Expression> callee;
    std::vector<std::unique_ptr<Expression>> arguments;
    std::map<std::string, std::unique_ptr<Expression>> named_args;

    CallExpr(std::unique_ptr<Expression> c,
             std::vector<std::unique_ptr<Expression>> args)
        : callee(std::move(c)), arguments(std::move(args)) {}

    void accept(ASTVisitor& visitor) override;
};

class ArrayExpr : public Expression {
public:
    std::vector<std::unique_ptr<Expression>> elements;

    explicit ArrayExpr(std::vector<std::unique_ptr<Expression>> elems)
        : elements(std::move(elems)) {}

    void accept(ASTVisitor& visitor) override;
};

class MemberExpr : public Expression {
public:
    std::unique_ptr<Expression> object;
    Token member;

    MemberExpr(std::unique_ptr<Expression> obj, Token mem)
        : object(std::move(obj)), member(mem) {}

    void accept(ASTVisitor& visitor) override;
};

class IndexExpr : public Expression {
public:
    std::unique_ptr<Expression> object;
    std::unique_ptr<Expression> index;

    IndexExpr(std::unique_ptr<Expression> obj, std::unique_ptr<Expression> idx)
        : object(std::move(obj)), index(std::move(idx)) {}

    void accept(ASTVisitor& visitor) override;
};

// Statement nodes
class Statement : public ASTNode {
public:
    virtual ~Statement() = default;
};

class ExpressionStmt : public Statement {
public:
    std::unique_ptr<Expression> expression;

    explicit ExpressionStmt(std::unique_ptr<Expression> expr)
        : expression(std::move(expr)) {}

    void accept(ASTVisitor& visitor) override;
};

class AssignmentStmt : public Statement {
public:
    Token variable;
    std::unique_ptr<Expression> value;

    AssignmentStmt(Token var, std::unique_ptr<Expression> val)
        : variable(var), value(std::move(val)) {}

    void accept(ASTVisitor& visitor) override;
};

class BlockStmt : public Statement {
public:
    std::vector<std::unique_ptr<Statement>> statements;

    explicit BlockStmt(std::vector<std::unique_ptr<Statement>> stmts)
        : statements(std::move(stmts)) {}

    void accept(ASTVisitor& visitor) override;
};

class IfStmt : public Statement {
public:
    std::unique_ptr<Expression> condition;
    std::unique_ptr<Statement> then_branch;
    std::unique_ptr<Statement> else_branch;

    IfStmt(std::unique_ptr<Expression> cond,
           std::unique_ptr<Statement> then_br,
           std::unique_ptr<Statement> else_br = nullptr)
        : condition(std::move(cond)),
          then_branch(std::move(then_br)),
          else_branch(std::move(else_br)) {}

    void accept(ASTVisitor& visitor) override;
};

class WhileStmt : public Statement {
public:
    std::unique_ptr<Expression> condition;
    std::unique_ptr<Statement> body;

    WhileStmt(std::unique_ptr<Expression> cond, std::unique_ptr<Statement> b)
        : condition(std::move(cond)), body(std::move(b)) {}

    void accept(ASTVisitor& visitor) override;
};

class ForStmt : public Statement {
public:
    std::unique_ptr<Statement> initializer;
    std::unique_ptr<Expression> condition;
    std::unique_ptr<Statement> increment;  // Changed from Expression to Statement
    std::unique_ptr<Statement> body;

    ForStmt(std::unique_ptr<Statement> init,
            std::unique_ptr<Expression> cond,
            std::unique_ptr<Statement> inc,  // Changed parameter type
            std::unique_ptr<Statement> b)
        : initializer(std::move(init)),
          condition(std::move(cond)),
          increment(std::move(inc)),
          body(std::move(b)) {}

    void accept(ASTVisitor& visitor) override;
};

class ForEachStmt : public Statement {
public:
    Token variable;
    std::unique_ptr<Expression> iterable;
    std::unique_ptr<Statement> body;

    ForEachStmt(Token var, std::unique_ptr<Expression> iter, std::unique_ptr<Statement> b)
        : variable(var), iterable(std::move(iter)), body(std::move(b)) {}

    void accept(ASTVisitor& visitor) override;
};

class FunctionStmt : public Statement {
public:
    Token name;
    std::vector<Token> parameters;
    std::unique_ptr<BlockStmt> body;

    FunctionStmt(Token n, std::vector<Token> params, std::unique_ptr<BlockStmt> b)
        : name(n), parameters(std::move(params)), body(std::move(b)) {}

    void accept(ASTVisitor& visitor) override;
};

class ReturnStmt : public Statement {
public:
    std::unique_ptr<Expression> value;

    explicit ReturnStmt(std::unique_ptr<Expression> val = nullptr)
        : value(std::move(val)) {}

    void accept(ASTVisitor& visitor) override;
};

class BreakStmt : public Statement {
public:
    void accept(ASTVisitor& visitor) override;
};

class ContinueStmt : public Statement {
public:
    void accept(ASTVisitor& visitor) override;
};

// Visitor interface
class ASTVisitor {
public:
    virtual ~ASTVisitor() = default;

    // Expressions
    virtual void visit(BinaryExpr& expr) = 0;
    virtual void visit(UnaryExpr& expr) = 0;
    virtual void visit(LiteralExpr& expr) = 0;
    virtual void visit(VariableExpr& expr) = 0;
    virtual void visit(CallExpr& expr) = 0;
    virtual void visit(ArrayExpr& expr) = 0;
    virtual void visit(MemberExpr& expr) = 0;
    virtual void visit(IndexExpr& expr) = 0;

    // Statements
    virtual void visit(ExpressionStmt& stmt) = 0;
    virtual void visit(AssignmentStmt& stmt) = 0;
    virtual void visit(BlockStmt& stmt) = 0;
    virtual void visit(IfStmt& stmt) = 0;
    virtual void visit(WhileStmt& stmt) = 0;
    virtual void visit(ForStmt& stmt) = 0;
    virtual void visit(ForEachStmt& stmt) = 0;
    virtual void visit(FunctionStmt& stmt) = 0;
    virtual void visit(ReturnStmt& stmt) = 0;
    virtual void visit(BreakStmt& stmt) = 0;
    virtual void visit(ContinueStmt& stmt) = 0;
};

} // namespace androidscript

#endif // ANDROIDSCRIPT_AST_H
