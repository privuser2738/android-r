#ifndef ANDROIDSCRIPT_PARSER_H
#define ANDROIDSCRIPT_PARSER_H

#include "ast.h"
#include "token.h"
#include <vector>
#include <memory>

namespace androidscript {

class Parser {
public:
    explicit Parser(const std::vector<Token>& tokens);

    // Parse the entire program
    std::vector<std::unique_ptr<Statement>> parse();

    // Error reporting
    const std::vector<std::string>& getErrors() const { return errors_; }
    bool hasErrors() const { return !errors_.empty(); }

private:
    std::vector<Token> tokens_;
    size_t current_;
    std::vector<std::string> errors_;

    // Parsing methods (recursive descent)
    std::unique_ptr<Statement> declaration();
    std::unique_ptr<Statement> statement();
    std::unique_ptr<Statement> expressionStatement();
    std::unique_ptr<Statement> assignmentStatement();
    std::unique_ptr<Statement> ifStatement();
    std::unique_ptr<Statement> whileStatement();
    std::unique_ptr<Statement> forStatement();
    std::unique_ptr<Statement> forEachStatement();
    std::unique_ptr<Statement> repeatStatement();
    std::unique_ptr<Statement> functionDeclaration();
    std::unique_ptr<Statement> returnStatement();
    std::unique_ptr<Statement> breakStatement();
    std::unique_ptr<Statement> continueStatement();
    std::unique_ptr<Statement> blockStatement();
    std::unique_ptr<Statement> tryStatement();

    // Expression parsing (operator precedence)
    std::unique_ptr<Expression> expression();
    std::unique_ptr<Expression> logicalOr();
    std::unique_ptr<Expression> logicalAnd();
    std::unique_ptr<Expression> equality();
    std::unique_ptr<Expression> comparison();
    std::unique_ptr<Expression> term();
    std::unique_ptr<Expression> factor();
    std::unique_ptr<Expression> unary();
    std::unique_ptr<Expression> call();
    std::unique_ptr<Expression> primary();

    // Helpers
    Token advance();
    Token peek() const;
    Token previous() const;
    bool check(TokenType type) const;
    bool match(TokenType type);
    bool match(const std::vector<TokenType>& types);
    Token consume(TokenType type, const std::string& message);
    bool isAtEnd() const;

    // Error handling
    void reportError(const std::string& message);
    void reportError(const Token& token, const std::string& message);
    void synchronize();
};

} // namespace androidscript

#endif // ANDROIDSCRIPT_PARSER_H
