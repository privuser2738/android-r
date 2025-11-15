package com.androidscript.agent.runtime

/**
 * Abstract Syntax Tree node definitions for AndroidScript
 * Uses sealed classes for type-safe AST representation
 */

// ========================================================================
// Base AST Nodes
// ========================================================================

sealed class ASTNode {
    abstract fun accept(visitor: ASTVisitor)
}

// ========================================================================
// Expression Nodes
// ========================================================================

sealed class Expression : ASTNode()

/**
 * Binary expression: left op right
 * Examples: 1 + 2, x == y, a && b
 */
data class BinaryExpr(
    val left: Expression,
    val op: Token,
    val right: Expression
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Unary expression: op operand
 * Examples: !flag, -value
 */
data class UnaryExpr(
    val op: Token,
    val operand: Expression
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Literal expression
 * Examples: 42, 3.14, "hello", true, null
 */
data class LiteralExpr(
    val value: Token
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Variable reference
 * Examples: $counter, $name
 */
data class VariableExpr(
    val name: Token
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Function call
 * Examples: Print("hello"), Tap(100, 200)
 */
data class CallExpr(
    val callee: Expression,
    val arguments: List<Expression>,
    val namedArgs: Map<String, Expression> = emptyMap()
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Array literal
 * Examples: [1, 2, 3], ["a", "b", "c"]
 */
data class ArrayExpr(
    val elements: List<Expression>
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Member access: object.member
 * Examples: element.text, node.bounds
 */
data class MemberExpr(
    val obj: Expression,
    val member: Token
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Index access: object[index]
 * Examples: array[0], map["key"]
 */
data class IndexExpr(
    val obj: Expression,
    val index: Expression
) : Expression() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

// ========================================================================
// Statement Nodes
// ========================================================================

sealed class Statement : ASTNode()

/**
 * Expression statement
 * Examples: Print("hello"); Tap(100, 200);
 */
data class ExpressionStmt(
    val expression: Expression
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Assignment statement
 * Examples: $x = 10; $name = "test";
 */
data class AssignmentStmt(
    val variable: Token,
    val value: Expression
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Block statement
 * Examples: { statement1; statement2; }
 */
data class BlockStmt(
    val statements: List<Statement>
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * If statement
 * Examples: if (condition) { ... } else { ... }
 */
data class IfStmt(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement? = null
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * While loop
 * Examples: while (condition) { ... }
 */
data class WhileStmt(
    val condition: Expression,
    val body: Statement
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * For loop
 * Examples: for ($i = 0; $i < 10; $i = $i + 1) { ... }
 */
data class ForStmt(
    val initializer: Statement?,
    val condition: Expression?,
    val increment: Statement?,
    val body: Statement
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * ForEach loop
 * Examples: foreach ($item in $array) { ... }
 */
data class ForEachStmt(
    val variable: Token,
    val iterable: Expression,
    val body: Statement
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Function declaration
 * Examples: function greet($name) { Print("Hello " + $name); }
 */
data class FunctionStmt(
    val name: Token,
    val parameters: List<Token>,
    val body: BlockStmt
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Return statement
 * Examples: return 42; return;
 */
data class ReturnStmt(
    val value: Expression? = null
) : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)
}

/**
 * Break statement
 * Examples: break;
 */
class BreakStmt : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)

    override fun equals(other: Any?): Boolean = other is BreakStmt
    override fun hashCode(): Int = "BreakStmt".hashCode()
}

/**
 * Continue statement
 * Examples: continue;
 */
class ContinueStmt : Statement() {
    override fun accept(visitor: ASTVisitor) = visitor.visit(this)

    override fun equals(other: Any?): Boolean = other is ContinueStmt
    override fun hashCode(): Int = "ContinueStmt".hashCode()
}

// ========================================================================
// Visitor Interface
// ========================================================================

/**
 * Visitor pattern for traversing AST
 * Implement this interface to evaluate, print, or transform AST
 */
interface ASTVisitor {
    // Expression visitors
    fun visit(expr: BinaryExpr)
    fun visit(expr: UnaryExpr)
    fun visit(expr: LiteralExpr)
    fun visit(expr: VariableExpr)
    fun visit(expr: CallExpr)
    fun visit(expr: ArrayExpr)
    fun visit(expr: MemberExpr)
    fun visit(expr: IndexExpr)

    // Statement visitors
    fun visit(stmt: ExpressionStmt)
    fun visit(stmt: AssignmentStmt)
    fun visit(stmt: BlockStmt)
    fun visit(stmt: IfStmt)
    fun visit(stmt: WhileStmt)
    fun visit(stmt: ForStmt)
    fun visit(stmt: ForEachStmt)
    fun visit(stmt: FunctionStmt)
    fun visit(stmt: ReturnStmt)
    fun visit(stmt: BreakStmt)
    fun visit(stmt: ContinueStmt)
}
