package com.androidscript.agent.runtime

import android.util.Log

/**
 * Parser for AndroidScript
 * Converts token stream into Abstract Syntax Tree (AST)
 * Uses recursive descent parsing with operator precedence
 */
class Parser(private val tokens: List<Token>) {
    private var current = 0
    private val errors = mutableListOf<String>()

    companion object {
        private const val TAG = "Parser"
    }

    /**
     * Parse the entire program into a list of statements
     */
    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!isAtEnd()) {
            try {
                val stmt = declaration()
                if (stmt != null) {
                    statements.add(stmt)
                }
            } catch (e: ParseException) {
                reportError(e.message ?: "Unknown parse error")
                synchronize()
            } catch (e: Exception) {
                reportError(e.message ?: "Unknown error")
                synchronize()
            }
        }

        return statements
    }

    /**
     * Get parsing errors
     */
    fun getErrors(): List<String> = errors
    fun hasErrors(): Boolean = errors.isNotEmpty()

    // ========================================================================
    // Statement Parsing
    // ========================================================================

    private fun declaration(): Statement? {
        return when {
            match(TokenType.FUNCTION) -> functionDeclaration()
            else -> statement()
        }
    }

    private fun statement(): Statement {
        return when {
            match(TokenType.IF) -> ifStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.FOR) -> forStatement()
            match(TokenType.FOREACH) -> forEachStatement()
            match(TokenType.REPEAT) -> repeatStatement()
            match(TokenType.RETURN) -> returnStatement()
            match(TokenType.BREAK) -> breakStatement()
            match(TokenType.CONTINUE) -> continueStatement()
            match(TokenType.LBRACE) -> blockStatement()
            match(TokenType.TRY) -> tryStatement()

            // Check for assignment (identifier followed by =)
            check(TokenType.IDENTIFIER) && peekNext().type == TokenType.ASSIGN -> assignmentStatement()

            else -> expressionStatement()
        }
    }

    private fun expressionStatement(): Statement {
        val expr = expression()
        return ExpressionStmt(expr)
    }

    private fun assignmentStatement(): Statement {
        val name = advance()
        consume(TokenType.ASSIGN, "Expected '=' in assignment")
        val value = expression()
        return AssignmentStmt(name, value)
    }

    private fun blockStatement(): BlockStmt {
        val statements = mutableListOf<Statement>()

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            val stmt = declaration()
            if (stmt != null) {
                statements.add(stmt)
            }
        }

        consume(TokenType.RBRACE, "Expected '}' after block")
        return BlockStmt(statements)
    }

    private fun ifStatement(): IfStmt {
        consume(TokenType.LPAREN, "Expected '(' after 'if'")
        val condition = expression()
        consume(TokenType.RPAREN, "Expected ')' after condition")

        val thenBranch = statement()
        var elseBranch: Statement? = null

        if (match(TokenType.ELSE)) {
            elseBranch = statement()
        }

        return IfStmt(condition, thenBranch, elseBranch)
    }

    private fun whileStatement(): WhileStmt {
        consume(TokenType.LPAREN, "Expected '(' after 'while'")
        val condition = expression()
        consume(TokenType.RPAREN, "Expected ')' after condition")
        val body = statement()

        return WhileStmt(condition, body)
    }

    private fun forStatement(): ForStmt {
        consume(TokenType.LPAREN, "Expected '(' after 'for'")

        // Parse initializer
        val initializer: Statement? = when {
            check(TokenType.SEMICOLON) -> null
            check(TokenType.IDENTIFIER) && peekNext().type == TokenType.ASSIGN -> assignmentStatement()
            else -> expressionStatement()
        }
        consume(TokenType.SEMICOLON, "Expected ';' after for loop initializer")

        // Parse condition
        val condition: Expression? = if (!check(TokenType.SEMICOLON)) {
            expression()
        } else {
            null
        }
        consume(TokenType.SEMICOLON, "Expected ';' after for loop condition")

        // Parse increment
        val increment: Statement? = when {
            check(TokenType.RPAREN) -> null
            check(TokenType.IDENTIFIER) && peekNext().type == TokenType.ASSIGN -> assignmentStatement()
            else -> expressionStatement()
        }
        consume(TokenType.RPAREN, "Expected ')' after for clauses")

        // Parse body
        val body = statement()

        return ForStmt(initializer, condition, increment, body)
    }

    private fun forEachStatement(): ForEachStmt {
        consume(TokenType.LPAREN, "Expected '(' after 'foreach'")
        val variable = consume(TokenType.IDENTIFIER, "Expected variable name")
        consume(TokenType.IN, "Expected 'in' in foreach loop")
        val iterable = expression()
        consume(TokenType.RPAREN, "Expected ')' after foreach clauses")
        val body = statement()

        return ForEachStmt(variable, iterable, body)
    }

    private fun repeatStatement(): Statement {
        // repeat { ... } until (condition)
        val body = statement()
        consume(TokenType.UNTIL, "Expected 'until' after repeat block")
        consume(TokenType.LPAREN, "Expected '(' after 'until'")
        val condition = expression()
        consume(TokenType.RPAREN, "Expected ')' after condition")

        // Convert repeat-until to do-while equivalent
        // repeat { body } until (condition) => do { body } while (!condition)
        val notCondition = UnaryExpr(
            Token(TokenType.LOGICAL_NOT, "!", condition.hashCode(), 0),
            condition
        )
        return WhileStmt(notCondition, body)
    }

    private fun functionDeclaration(): FunctionStmt {
        val name = consume(TokenType.IDENTIFIER, "Expected function name")
        consume(TokenType.LPAREN, "Expected '(' after function name")

        val parameters = mutableListOf<Token>()
        if (!check(TokenType.RPAREN)) {
            do {
                parameters.add(consume(TokenType.IDENTIFIER, "Expected parameter name"))
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RPAREN, "Expected ')' after parameters")
        consume(TokenType.LBRACE, "Expected '{' before function body")
        val body = blockStatement()

        return FunctionStmt(name, parameters, body)
    }

    private fun returnStatement(): ReturnStmt {
        val value = if (!check(TokenType.SEMICOLON) && !isAtEnd()) {
            expression()
        } else {
            null
        }
        return ReturnStmt(value)
    }

    private fun breakStatement(): BreakStmt {
        return BreakStmt()
    }

    private fun continueStatement(): ContinueStmt {
        return ContinueStmt()
    }

    private fun tryStatement(): Statement {
        // TODO: Implement try-catch-finally parsing
        throw ParseException("try-catch-finally not yet implemented")
    }

    // ========================================================================
    // Expression Parsing (Operator Precedence)
    // ========================================================================

    private fun expression(): Expression {
        return logicalOr()
    }

    private fun logicalOr(): Expression {
        var expr = logicalAnd()

        while (match(TokenType.LOGICAL_OR)) {
            val op = previous()
            val right = logicalAnd()
            expr = BinaryExpr(expr, op, right)
        }

        return expr
    }

    private fun logicalAnd(): Expression {
        var expr = equality()

        while (match(TokenType.LOGICAL_AND)) {
            val op = previous()
            val right = equality()
            expr = BinaryExpr(expr, op, right)
        }

        return expr
    }

    private fun equality(): Expression {
        var expr = comparison()

        while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = BinaryExpr(expr, op, right)
        }

        return expr
    }

    private fun comparison(): Expression {
        var expr = term()

        while (match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
            val op = previous()
            val right = term()
            expr = BinaryExpr(expr, op, right)
        }

        return expr
    }

    private fun term(): Expression {
        var expr = factor()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous()
            val right = factor()
            expr = BinaryExpr(expr, op, right)
        }

        return expr
    }

    private fun factor(): Expression {
        var expr = unary()

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            val op = previous()
            val right = unary()
            expr = BinaryExpr(expr, op, right)
        }

        return expr
    }

    private fun unary(): Expression {
        if (match(TokenType.LOGICAL_NOT, TokenType.MINUS)) {
            val op = previous()
            val operand = unary()
            return UnaryExpr(op, operand)
        }

        return call()
    }

    private fun call(): Expression {
        var expr = primary()

        while (true) {
            when {
                match(TokenType.LPAREN) -> {
                    val args = mutableListOf<Expression>()
                    if (!check(TokenType.RPAREN)) {
                        do {
                            args.add(expression())
                        } while (match(TokenType.COMMA))
                    }
                    consume(TokenType.RPAREN, "Expected ')' after arguments")
                    expr = CallExpr(expr, args)
                }
                match(TokenType.DOT) -> {
                    val member = consume(TokenType.IDENTIFIER, "Expected property name after '.'")
                    expr = MemberExpr(expr, member)
                }
                match(TokenType.LBRACKET) -> {
                    val index = expression()
                    consume(TokenType.RBRACKET, "Expected ']' after index")
                    expr = IndexExpr(expr, index)
                }
                else -> break
            }
        }

        return expr
    }

    private fun primary(): Expression {
        return when {
            match(TokenType.TRUE) -> LiteralExpr(previous())
            match(TokenType.FALSE) -> LiteralExpr(previous())
            match(TokenType.NULLPTR) -> LiteralExpr(previous())
            match(TokenType.INTEGER) -> LiteralExpr(previous())
            match(TokenType.FLOAT) -> LiteralExpr(previous())
            match(TokenType.STRING) -> LiteralExpr(previous())
            match(TokenType.IDENTIFIER) -> VariableExpr(previous())

            match(TokenType.LPAREN) -> {
                val expr = expression()
                consume(TokenType.RPAREN, "Expected ')' after expression")
                expr
            }

            match(TokenType.LBRACKET) -> {
                val elements = mutableListOf<Expression>()
                if (!check(TokenType.RBRACKET)) {
                    do {
                        elements.add(expression())
                    } while (match(TokenType.COMMA))
                }
                consume(TokenType.RBRACKET, "Expected ']' after array elements")
                ArrayExpr(elements)
            }

            else -> throw ParseException("Expected expression")
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun peekNext(): Token {
        return if (current + 1 < tokens.size) {
            tokens[current + 1]
        } else {
            Token.eof(0, 0)
        }
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw ParseException(message)
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.END_OF_FILE
    }

    // ========================================================================
    // Error Handling
    // ========================================================================

    private fun reportError(message: String) {
        val error = "Parser error: $message"
        errors.add(error)
        Log.e(TAG, error)
    }

    private fun reportError(token: Token, message: String) {
        val error = "Parser error at line ${token.line}, column ${token.column}: $message"
        errors.add(error)
        Log.e(TAG, error)
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.IF,
                TokenType.WHILE,
                TokenType.FOR,
                TokenType.FOREACH,
                TokenType.FUNCTION,
                TokenType.RETURN -> return
                else -> advance()
            }
        }
    }

    // ========================================================================
    // Exception Classes
    // ========================================================================

    private class ParseException(message: String) : Exception(message)
}
