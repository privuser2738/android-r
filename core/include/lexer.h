#ifndef ANDROIDSCRIPT_LEXER_H
#define ANDROIDSCRIPT_LEXER_H

#include "token.h"
#include <string>
#include <vector>

namespace androidscript {

class Lexer {
public:
    explicit Lexer(const std::string& source);

    // Tokenize the entire source
    std::vector<Token> tokenize();

    // Get next token
    Token nextToken();

    // Peek at next token without consuming
    Token peekToken();

    // Check if at end
    bool isAtEnd() const;

    // Error reporting
    const std::vector<std::string>& getErrors() const { return errors_; }
    bool hasErrors() const { return !errors_.empty(); }

private:
    std::string source_;
    size_t current_;
    size_t start_;
    int line_;
    int column_;
    std::vector<std::string> errors_;

    // Helper methods
    char advance();
    char peek() const;
    char peekNext() const;
    bool match(char expected);
    bool isAtEnd(size_t pos) const;

    // Tokenization helpers
    Token makeToken(TokenType type);
    Token errorToken(const std::string& message);

    // Specific token types
    Token string();
    Token number();
    Token identifier();
    Token directive();

    // Character classification
    bool isDigit(char c) const;
    bool isAlpha(char c) const;
    bool isAlphaNumeric(char c) const;
    bool isWhitespace(char c) const;

    // Skip methods
    void skipWhitespace();
    void skipLineComment();
    void skipBlockComment();

    // Error handling
    void reportError(const std::string& message);
};

} // namespace androidscript

#endif // ANDROIDSCRIPT_LEXER_H
