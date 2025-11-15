#include "parser.h"
#include <sstream>

namespace androidscript {

Parser::Parser(const std::vector<Token>& tokens)
    : tokens_(tokens), current_(0) {}

std::vector<std::unique_ptr<Statement>> Parser::parse() {
    std::vector<std::unique_ptr<Statement>> statements;

    while (!isAtEnd()) {
        try {
            auto stmt = declaration();
            if (stmt) {
                statements.push_back(std::move(stmt));
            }
        } catch (const std::exception& e) {
            reportError(e.what());
            synchronize();
        }
    }

    return statements;
}

std::unique_ptr<Statement> Parser::declaration() {
    if (match(TokenType::FUNCTION)) {
        return functionDeclaration();
    }
    return statement();
}

std::unique_ptr<Statement> Parser::statement() {
    if (match(TokenType::IF)) return ifStatement();
    if (match(TokenType::WHILE)) return whileStatement();
    if (match(TokenType::FOR)) return forStatement();
    if (match(TokenType::FOREACH)) return forEachStatement();
    if (match(TokenType::REPEAT)) return repeatStatement();
    if (match(TokenType::RETURN)) return returnStatement();
    if (match(TokenType::BREAK)) return breakStatement();
    if (match(TokenType::CONTINUE)) return continueStatement();
    if (match(TokenType::LBRACE)) return blockStatement();
    if (match(TokenType::TRY)) return tryStatement();

    // Check for assignment (variable followed by =)
    if (check(TokenType::IDENTIFIER) && tokens_[current_ + 1].type == TokenType::ASSIGN) {
        return assignmentStatement();
    }

    return expressionStatement();
}

std::unique_ptr<Statement> Parser::expressionStatement() {
    auto expr = expression();
    return std::make_unique<ExpressionStmt>(std::move(expr));
}

std::unique_ptr<Statement> Parser::assignmentStatement() {
    Token name = advance();
    consume(TokenType::ASSIGN, "Expected '=' in assignment");
    auto value = expression();
    return std::make_unique<AssignmentStmt>(name, std::move(value));
}

std::unique_ptr<Statement> Parser::blockStatement() {
    std::vector<std::unique_ptr<Statement>> statements;

    while (!check(TokenType::RBRACE) && !isAtEnd()) {
        statements.push_back(declaration());
    }

    consume(TokenType::RBRACE, "Expected '}' after block");
    return std::make_unique<BlockStmt>(std::move(statements));
}

std::unique_ptr<Statement> Parser::ifStatement() {
    consume(TokenType::LPAREN, "Expected '(' after 'if'");
    auto condition = expression();
    consume(TokenType::RPAREN, "Expected ')' after condition");

    auto thenBranch = statement();
    std::unique_ptr<Statement> elseBranch = nullptr;

    if (match(TokenType::ELSE)) {
        elseBranch = statement();
    }

    return std::make_unique<IfStmt>(std::move(condition), std::move(thenBranch), std::move(elseBranch));
}

std::unique_ptr<Statement> Parser::whileStatement() {
    consume(TokenType::LPAREN, "Expected '(' after 'while'");
    auto condition = expression();
    consume(TokenType::RPAREN, "Expected ')' after condition");
    auto body = statement();

    return std::make_unique<WhileStmt>(std::move(condition), std::move(body));
}

std::unique_ptr<Statement> Parser::forStatement() {
    consume(TokenType::LPAREN, "Expected '(' after 'for'");

    // Parse initializer
    std::unique_ptr<Statement> initializer = nullptr;
    if (!check(TokenType::SEMICOLON)) {
        // Check if it's a variable assignment
        if (check(TokenType::IDENTIFIER)) {
            initializer = assignmentStatement();
        } else {
            initializer = expressionStatement();
        }
    }
    consume(TokenType::SEMICOLON, "Expected ';' after for loop initializer");

    // Parse condition
    std::unique_ptr<Expression> condition = nullptr;
    if (!check(TokenType::SEMICOLON)) {
        condition = expression();
    }
    consume(TokenType::SEMICOLON, "Expected ';' after for loop condition");

    // Parse increment (can be assignment or expression statement)
    std::unique_ptr<Statement> increment = nullptr;
    if (!check(TokenType::RPAREN)) {
        // Check if it's an assignment ($i = $i + 1)
        if (check(TokenType::IDENTIFIER) && current_ + 1 < tokens_.size() &&
            tokens_[current_ + 1].type == TokenType::ASSIGN) {
            increment = assignmentStatement();
        } else {
            // Otherwise treat as expression statement
            increment = expressionStatement();
        }
    }
    consume(TokenType::RPAREN, "Expected ')' after for clauses");

    // Parse body
    auto body = statement();

    return std::make_unique<ForStmt>(
        std::move(initializer),
        std::move(condition),
        std::move(increment),
        std::move(body)
    );
}

std::unique_ptr<Statement> Parser::forEachStatement() {
    // TODO: Implement foreach parsing
    return nullptr;
}

std::unique_ptr<Statement> Parser::repeatStatement() {
    // TODO: Implement repeat-until parsing
    return nullptr;
}

std::unique_ptr<Statement> Parser::functionDeclaration() {
    // TODO: Implement function declaration parsing
    return nullptr;
}

std::unique_ptr<Statement> Parser::returnStatement() {
    std::unique_ptr<Expression> value = nullptr;
    if (!check(TokenType::SEMICOLON) && !isAtEnd()) {
        value = expression();
    }
    return std::make_unique<ReturnStmt>(std::move(value));
}

std::unique_ptr<Statement> Parser::breakStatement() {
    return std::make_unique<BreakStmt>();
}

std::unique_ptr<Statement> Parser::continueStatement() {
    return std::make_unique<ContinueStmt>();
}

std::unique_ptr<Statement> Parser::tryStatement() {
    // TODO: Implement try-catch-finally parsing
    return nullptr;
}

std::unique_ptr<Expression> Parser::expression() {
    return logicalOr();
}

std::unique_ptr<Expression> Parser::logicalOr() {
    auto expr = logicalAnd();

    while (match(TokenType::LOGICAL_OR)) {
        Token op = previous();
        auto right = logicalAnd();
        expr = std::make_unique<BinaryExpr>(std::move(expr), op, std::move(right));
    }

    return expr;
}

std::unique_ptr<Expression> Parser::logicalAnd() {
    auto expr = equality();

    while (match(TokenType::LOGICAL_AND)) {
        Token op = previous();
        auto right = equality();
        expr = std::make_unique<BinaryExpr>(std::move(expr), op, std::move(right));
    }

    return expr;
}

std::unique_ptr<Expression> Parser::equality() {
    auto expr = comparison();

    while (match({TokenType::EQUAL, TokenType::NOT_EQUAL})) {
        Token op = previous();
        auto right = comparison();
        expr = std::make_unique<BinaryExpr>(std::move(expr), op, std::move(right));
    }

    return expr;
}

std::unique_ptr<Expression> Parser::comparison() {
    auto expr = term();

    while (match({TokenType::LESS, TokenType::LESS_EQUAL, TokenType::GREATER, TokenType::GREATER_EQUAL})) {
        Token op = previous();
        auto right = term();
        expr = std::make_unique<BinaryExpr>(std::move(expr), op, std::move(right));
    }

    return expr;
}

std::unique_ptr<Expression> Parser::term() {
    auto expr = factor();

    while (match({TokenType::PLUS, TokenType::MINUS})) {
        Token op = previous();
        auto right = factor();
        expr = std::make_unique<BinaryExpr>(std::move(expr), op, std::move(right));
    }

    return expr;
}

std::unique_ptr<Expression> Parser::factor() {
    auto expr = unary();

    while (match({TokenType::MULTIPLY, TokenType::DIVIDE, TokenType::MODULO})) {
        Token op = previous();
        auto right = unary();
        expr = std::make_unique<BinaryExpr>(std::move(expr), op, std::move(right));
    }

    return expr;
}

std::unique_ptr<Expression> Parser::unary() {
    if (match({TokenType::LOGICAL_NOT, TokenType::MINUS})) {
        Token op = previous();
        auto operand = unary();
        return std::make_unique<UnaryExpr>(op, std::move(operand));
    }

    return call();
}

std::unique_ptr<Expression> Parser::call() {
    auto expr = primary();

    while (true) {
        if (match(TokenType::LPAREN)) {
            std::vector<std::unique_ptr<Expression>> args;
            if (!check(TokenType::RPAREN)) {
                do {
                    args.push_back(expression());
                } while (match(TokenType::COMMA));
            }
            consume(TokenType::RPAREN, "Expected ')' after arguments");
            expr = std::make_unique<CallExpr>(std::move(expr), std::move(args));
        } else if (match(TokenType::DOT)) {
            Token member = consume(TokenType::IDENTIFIER, "Expected property name after '.'");
            expr = std::make_unique<MemberExpr>(std::move(expr), member);
        } else if (match(TokenType::LBRACKET)) {
            auto index = expression();
            consume(TokenType::RBRACKET, "Expected ']' after index");
            expr = std::make_unique<IndexExpr>(std::move(expr), std::move(index));
        } else {
            break;
        }
    }

    return expr;
}

std::unique_ptr<Expression> Parser::primary() {
    if (match(TokenType::TRUE)) {
        return std::make_unique<LiteralExpr>(previous());
    }
    if (match(TokenType::FALSE)) {
        return std::make_unique<LiteralExpr>(previous());
    }
    if (match(TokenType::NULLPTR)) {
        return std::make_unique<LiteralExpr>(previous());
    }
    if (match(TokenType::INTEGER)) {
        return std::make_unique<LiteralExpr>(previous());
    }
    if (match(TokenType::FLOAT)) {
        return std::make_unique<LiteralExpr>(previous());
    }
    if (match(TokenType::STRING)) {
        return std::make_unique<LiteralExpr>(previous());
    }
    if (match(TokenType::IDENTIFIER)) {
        return std::make_unique<VariableExpr>(previous());
    }
    if (match(TokenType::LPAREN)) {
        auto expr = expression();
        consume(TokenType::RPAREN, "Expected ')' after expression");
        return expr;
    }
    if (match(TokenType::LBRACKET)) {
        std::vector<std::unique_ptr<Expression>> elements;
        if (!check(TokenType::RBRACKET)) {
            do {
                elements.push_back(expression());
            } while (match(TokenType::COMMA));
        }
        consume(TokenType::RBRACKET, "Expected ']' after array elements");
        return std::make_unique<ArrayExpr>(std::move(elements));
    }

    throw std::runtime_error("Expected expression");
}

Token Parser::advance() {
    if (!isAtEnd()) current_++;
    return previous();
}

Token Parser::peek() const {
    return tokens_[current_];
}

Token Parser::previous() const {
    return tokens_[current_ - 1];
}

bool Parser::check(TokenType type) const {
    if (isAtEnd()) return false;
    return peek().type == type;
}

bool Parser::match(TokenType type) {
    if (check(type)) {
        advance();
        return true;
    }
    return false;
}

bool Parser::match(const std::vector<TokenType>& types) {
    for (TokenType type : types) {
        if (check(type)) {
            advance();
            return true;
        }
    }
    return false;
}

Token Parser::consume(TokenType type, const std::string& message) {
    if (check(type)) return advance();
    throw std::runtime_error(message);
}

bool Parser::isAtEnd() const {
    return peek().type == TokenType::END_OF_FILE;
}

void Parser::reportError(const std::string& message) {
    std::ostringstream oss;
    oss << "Parser error: " << message;
    errors_.push_back(oss.str());
}

void Parser::reportError(const Token& token, const std::string& message) {
    std::ostringstream oss;
    oss << "Parser error at line " << token.line << ", column " << token.column << ": " << message;
    errors_.push_back(oss.str());
}

void Parser::synchronize() {
    advance();

    while (!isAtEnd()) {
        if (previous().type == TokenType::SEMICOLON) return;

        switch (peek().type) {
            case TokenType::IF:
            case TokenType::WHILE:
            case TokenType::FOR:
            case TokenType::FOREACH:
            case TokenType::FUNCTION:
            case TokenType::RETURN:
                return;
            default:
                advance();
        }
    }
}

} // namespace androidscript
