import Foundation

/**
 * Parser for AndroidScript
 * Converts token stream into Abstract Syntax Tree
 * Uses recursive descent parsing
 */
class Parser {
    private let tokens: [Token]
    private var current = 0
    private var errors = [String]()

    init(tokens: [Token]) {
        self.tokens = tokens
    }

    // MARK: - Public Interface

    func parse() -> [Statement] {
        var statements = [Statement]()

        while !isAtEnd() {
            do {
                if let stmt = try declaration() {
                    statements.append(stmt)
                }
            } catch {
                reportError(error.localizedDescription)
                synchronize()
            }
        }

        return statements
    }

    func hasErrors() -> Bool {
        return !errors.isEmpty
    }

    func getErrors() -> [String] {
        return errors
    }

    // MARK: - Statement Parsing

    private func declaration() throws -> Statement? {
        if match(.function) {
            return try functionDeclaration()
        }
        return try statement()
    }

    private func statement() throws -> Statement {
        if match(.if) { return try ifStatement() }
        if match(.while) { return try whileStatement() }
        if match(.for) { return try forStatement() }
        if match(.foreach) { return try forEachStatement() }
        if match(.repeat) { return try repeatStatement() }
        if match(.return) { return try returnStatement() }
        if match(.break) { return try breakStatement() }
        if match(.continue) { return try continueStatement() }
        if match(.lbrace) { return try blockStatement() }
        if match(.try) { return try tryStatement() }

        // Check for assignment
        if check(.identifier) && peekNext().type == .assign {
            return try assignmentStatement()
        }

        return try expressionStatement()
    }

    private func expressionStatement() throws -> Statement {
        let expr = try expression()
        return ExpressionStmt(expression: expr)
    }

    private func assignmentStatement() throws -> Statement {
        let name = advance()
        _ = try consume(.assign, message: "Expected '=' in assignment")
        let value = try expression()
        return AssignmentStmt(variable: name, value: value)
    }

    private func blockStatement() throws -> BlockStmt {
        var statements = [Statement]()

        while !check(.rbrace) && !isAtEnd() {
            if let stmt = try declaration() {
                statements.append(stmt)
            }
        }

        _ = try consume(.rbrace, message: "Expected '}' after block")
        return BlockStmt(statements: statements)
    }

    private func ifStatement() throws -> IfStmt {
        _ = try consume(.lparen, message: "Expected '(' after 'if'")
        let condition = try expression()
        _ = try consume(.rparen, message: "Expected ')' after condition")

        let thenBranch = try statement()
        var elseBranch: Statement? = nil

        if match(.else) {
            elseBranch = try statement()
        }

        return IfStmt(condition: condition, thenBranch: thenBranch, elseBranch: elseBranch)
    }

    private func whileStatement() throws -> WhileStmt {
        _ = try consume(.lparen, message: "Expected '(' after 'while'")
        let condition = try expression()
        _ = try consume(.rparen, message: "Expected ')' after condition")
        let body = try statement()

        return WhileStmt(condition: condition, body: body)
    }

    private func forStatement() throws -> ForStmt {
        _ = try consume(.lparen, message: "Expected '(' after 'for'")

        // Initializer
        var initializer: Statement? = nil
        if !check(.semicolon) {
            if check(.identifier) && peekNext().type == .assign {
                initializer = try assignmentStatement()
            } else {
                initializer = try expressionStatement()
            }
        }
        _ = try consume(.semicolon, message: "Expected ';' after for loop initializer")

        // Condition
        var condition: Expression? = nil
        if !check(.semicolon) {
            condition = try expression()
        }
        _ = try consume(.semicolon, message: "Expected ';' after for loop condition")

        // Increment
        var increment: Statement? = nil
        if !check(.rparen) {
            if check(.identifier) && peekNext().type == .assign {
                increment = try assignmentStatement()
            } else {
                increment = try expressionStatement()
            }
        }
        _ = try consume(.rparen, message: "Expected ')' after for clauses")

        // Body
        let body = try statement()

        return ForStmt(initializer: initializer, condition: condition, increment: increment, body: body)
    }

    private func forEachStatement() throws -> ForEachStmt {
        _ = try consume(.lparen, message: "Expected '(' after 'foreach'")
        let variable = try consume(.identifier, message: "Expected variable name")
        _ = try consume(.in, message: "Expected 'in' in foreach loop")
        let iterable = try expression()
        _ = try consume(.rparen, message: "Expected ')' after foreach clauses")
        let body = try statement()

        return ForEachStmt(variable: variable, iterable: iterable, body: body)
    }

    private func repeatStatement() throws -> Statement {
        // repeat { ... } until (condition)
        let body = try statement()
        _ = try consume(.until, message: "Expected 'until' after repeat block")
        _ = try consume(.lparen, message: "Expected '(' after 'until'")
        let condition = try expression()
        _ = try consume(.rparen, message: "Expected ')' after condition")

        // Convert to do-while: repeat { body } until (condition) => while (!condition) { body }
        let notToken = Token(type: .logicalNot, lexeme: "!", line: 0, column: 0)
        let notCondition = UnaryExpr(op: notToken, operand: condition)

        return WhileStmt(condition: notCondition, body: body)
    }

    private func functionDeclaration() throws -> FunctionStmt {
        let name = try consume(.identifier, message: "Expected function name")
        _ = try consume(.lparen, message: "Expected '(' after function name")

        var parameters = [Token]()
        if !check(.rparen) {
            repeat {
                parameters.append(try consume(.identifier, message: "Expected parameter name"))
            } while match(.comma)
        }

        _ = try consume(.rparen, message: "Expected ')' after parameters")
        _ = try consume(.lbrace, message: "Expected '{' before function body")
        let body = try blockStatement()

        return FunctionStmt(name: name, parameters: parameters, body: body)
    }

    private func returnStatement() throws -> ReturnStmt {
        var value: Expression? = nil
        if !check(.semicolon) && !isAtEnd() {
            value = try expression()
        }
        return ReturnStmt(value: value)
    }

    private func breakStatement() throws -> BreakStmt {
        return BreakStmt()
    }

    private func continueStatement() throws -> ContinueStmt {
        return ContinueStmt()
    }

    private func tryStatement() throws -> Statement {
        throw ParseError.notImplemented("try-catch-finally not yet implemented")
    }

    // MARK: - Expression Parsing (Operator Precedence)

    private func expression() throws -> Expression {
        return try logicalOr()
    }

    private func logicalOr() throws -> Expression {
        var expr = try logicalAnd()

        while match(.logicalOr) {
            let op = previous()
            let right = try logicalAnd()
            expr = BinaryExpr(left: expr, op: op, right: right)
        }

        return expr
    }

    private func logicalAnd() throws -> Expression {
        var expr = try equality()

        while match(.logicalAnd) {
            let op = previous()
            let right = try equality()
            expr = BinaryExpr(left: expr, op: op, right: right)
        }

        return expr
    }

    private func equality() throws -> Expression {
        var expr = try comparison()

        while match(.equal, .notEqual) {
            let op = previous()
            let right = try comparison()
            expr = BinaryExpr(left: expr, op: op, right: right)
        }

        return expr
    }

    private func comparison() throws -> Expression {
        var expr = try term()

        while match(.less, .lessEqual, .greater, .greaterEqual) {
            let op = previous()
            let right = try term()
            expr = BinaryExpr(left: expr, op: op, right: right)
        }

        return expr
    }

    private func term() throws -> Expression {
        var expr = try factor()

        while match(.plus, .minus) {
            let op = previous()
            let right = try factor()
            expr = BinaryExpr(left: expr, op: op, right: right)
        }

        return expr
    }

    private func factor() throws -> Expression {
        var expr = try unary()

        while match(.multiply, .divide, .modulo) {
            let op = previous()
            let right = try unary()
            expr = BinaryExpr(left: expr, op: op, right: right)
        }

        return expr
    }

    private func unary() throws -> Expression {
        if match(.logicalNot, .minus) {
            let op = previous()
            let operand = try unary()
            return UnaryExpr(op: op, operand: operand)
        }

        return try call()
    }

    private func call() throws -> Expression {
        var expr = try primary()

        while true {
            if match(.lparen) {
                var args = [Expression]()
                if !check(.rparen) {
                    repeat {
                        args.append(try expression())
                    } while match(.comma)
                }
                _ = try consume(.rparen, message: "Expected ')' after arguments")
                expr = CallExpr(callee: expr, arguments: args)
            } else if match(.dot) {
                let member = try consume(.identifier, message: "Expected property name after '.'")
                expr = MemberExpr(object: expr, member: member)
            } else if match(.lbracket) {
                let index = try expression()
                _ = try consume(.rbracket, message: "Expected ']' after index")
                expr = IndexExpr(object: expr, index: index)
            } else {
                break
            }
        }

        return expr
    }

    private func primary() throws -> Expression {
        if match(.true) { return LiteralExpr(value: previous()) }
        if match(.false) { return LiteralExpr(value: previous()) }
        if match(.nullptr) { return LiteralExpr(value: previous()) }
        if match(.integer) { return LiteralExpr(value: previous()) }
        if match(.float) { return LiteralExpr(value: previous()) }
        if match(.string) { return LiteralExpr(value: previous()) }
        if match(.identifier) { return VariableExpr(name: previous()) }

        if match(.lparen) {
            let expr = try expression()
            _ = try consume(.rparen, message: "Expected ')' after expression")
            return expr
        }

        if match(.lbracket) {
            var elements = [Expression]()
            if !check(.rbracket) {
                repeat {
                    elements.append(try expression())
                } while match(.comma)
            }
            _ = try consume(.rbracket, message: "Expected ']' after array elements")
            return ArrayExpr(elements: elements)
        }

        throw ParseError.expectedExpression
    }

    // MARK: - Helper Methods

    @discardableResult
    private func advance() -> Token {
        if !isAtEnd() { current += 1 }
        return previous()
    }

    private func peek() -> Token {
        return tokens[current]
    }

    private func peekNext() -> Token {
        if current + 1 < tokens.count {
            return tokens[current + 1]
        }
        return Token.eof(line: 0, column: 0)
    }

    private func previous() -> Token {
        return tokens[current - 1]
    }

    private func check(_ type: TokenType) -> Bool {
        if isAtEnd() { return false }
        return peek().type == type
    }

    private func match(_ types: TokenType...) -> Bool {
        for type in types {
            if check(type) {
                advance()
                return true
            }
        }
        return false
    }

    private func consume(_ type: TokenType, message: String) throws -> Token {
        if check(type) { return advance() }
        throw ParseError.expectedToken(message)
    }

    private func isAtEnd() -> Bool {
        return peek().type == .endOfFile
    }

    // MARK: - Error Handling

    private func reportError(_ message: String) {
        errors.append("Parser error: \(message)")
        print("Parser error: \(message)")
    }

    private func synchronize() {
        advance()

        while !isAtEnd() {
            if previous().type == .semicolon { return }

            switch peek().type {
            case .if, .while, .for, .foreach, .function, .return:
                return
            default:
                advance()
            }
        }
    }
}

// MARK: - Parse Errors

enum ParseError: Error {
    case expectedToken(String)
    case expectedExpression
    case notImplemented(String)

    var localizedDescription: String {
        switch self {
        case .expectedToken(let message):
            return message
        case .expectedExpression:
            return "Expected expression"
        case .notImplemented(let message):
            return message
        }
    }
}
