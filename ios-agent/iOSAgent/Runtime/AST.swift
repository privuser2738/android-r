import Foundation

/**
 * Abstract Syntax Tree nodes for AndroidScript
 * Swift implementation using protocols and classes
 */

// MARK: - Base Protocols

protocol ASTNode {
    func accept(visitor: ASTVisitor)
}

protocol Expression: ASTNode {}
protocol Statement: ASTNode {}

// MARK: - Expression Nodes

class BinaryExpr: Expression {
    let left: Expression
    let op: Token
    let right: Expression

    init(left: Expression, op: Token, right: Expression) {
        self.left = left
        self.op = op
        self.right = right
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

class UnaryExpr: Expression {
    let op: Token
    let operand: Expression

    init(op: Token, operand: Expression) {
        self.op = op
        self.operand = operand
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

class LiteralExpr: Expression {
    let value: Token

    init(value: Token) {
        self.value = value
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

class VariableExpr: Expression {
    let name: Token

    init(name: Token) {
        self.name = name
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

class CallExpr: Expression {
    let callee: Expression
    let arguments: [Expression]
    let namedArgs: [String: Expression]

    init(callee: Expression, arguments: [Expression], namedArgs: [String: Expression] = [:]) {
        self.callee = callee
        self.arguments = arguments
        self.namedArgs = namedArgs
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

class ArrayExpr: Expression {
    let elements: [Expression]

    init(elements: [Expression]) {
        self.elements = elements
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

class MemberExpr: Expression {
    let object: Expression
    let member: Token

    init(object: Expression, member: Token) {
        self.object = object
        self.member = member
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

class IndexExpr: Expression {
    let object: Expression
    let index: Expression

    init(object: Expression, index: Expression) {
        self.object = object
        self.index = index
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(expr: self)
    }
}

// MARK: - Statement Nodes

class ExpressionStmt: Statement {
    let expression: Expression

    init(expression: Expression) {
        self.expression = expression
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class AssignmentStmt: Statement {
    let variable: Token
    let value: Expression

    init(variable: Token, value: Expression) {
        self.variable = variable
        self.value = value
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class BlockStmt: Statement {
    let statements: [Statement]

    init(statements: [Statement]) {
        self.statements = statements
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class IfStmt: Statement {
    let condition: Expression
    let thenBranch: Statement
    let elseBranch: Statement?

    init(condition: Expression, thenBranch: Statement, elseBranch: Statement? = nil) {
        self.condition = condition
        self.thenBranch = thenBranch
        self.elseBranch = elseBranch
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class WhileStmt: Statement {
    let condition: Expression
    let body: Statement

    init(condition: Expression, body: Statement) {
        self.condition = condition
        self.body = body
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class ForStmt: Statement {
    let initializer: Statement?
    let condition: Expression?
    let increment: Statement?
    let body: Statement

    init(initializer: Statement?, condition: Expression?, increment: Statement?, body: Statement) {
        self.initializer = initializer
        self.condition = condition
        self.increment = increment
        self.body = body
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class ForEachStmt: Statement {
    let variable: Token
    let iterable: Expression
    let body: Statement

    init(variable: Token, iterable: Expression, body: Statement) {
        self.variable = variable
        self.iterable = iterable
        self.body = body
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class FunctionStmt: Statement {
    let name: Token
    let parameters: [Token]
    let body: BlockStmt

    init(name: Token, parameters: [Token], body: BlockStmt) {
        self.name = name
        self.parameters = parameters
        self.body = body
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class ReturnStmt: Statement {
    let value: Expression?

    init(value: Expression? = nil) {
        self.value = value
    }

    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class BreakStmt: Statement {
    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

class ContinueStmt: Statement {
    func accept(visitor: ASTVisitor) {
        visitor.visit(stmt: self)
    }
}

// MARK: - Visitor Protocol

protocol ASTVisitor {
    // Expression visitors
    func visit(expr: BinaryExpr)
    func visit(expr: UnaryExpr)
    func visit(expr: LiteralExpr)
    func visit(expr: VariableExpr)
    func visit(expr: CallExpr)
    func visit(expr: ArrayExpr)
    func visit(expr: MemberExpr)
    func visit(expr: IndexExpr)

    // Statement visitors
    func visit(stmt: ExpressionStmt)
    func visit(stmt: AssignmentStmt)
    func visit(stmt: BlockStmt)
    func visit(stmt: IfStmt)
    func visit(stmt: WhileStmt)
    func visit(stmt: ForStmt)
    func visit(stmt: ForEachStmt)
    func visit(stmt: FunctionStmt)
    func visit(stmt: ReturnStmt)
    func visit(stmt: BreakStmt)
    func visit(stmt: ContinueStmt)
}
