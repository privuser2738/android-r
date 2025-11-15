#include "lexer.h"
#include <cctype>
#include <sstream>

namespace androidscript {

// Keyword mapping
const std::map<std::string, TokenType> keywords = {
    {"if", TokenType::IF},
    {"else", TokenType::ELSE},
    {"while", TokenType::WHILE},
    {"for", TokenType::FOR},
    {"foreach", TokenType::FOREACH},
    {"ForEach", TokenType::FOREACH},
    {"repeat", TokenType::REPEAT},
    {"until", TokenType::UNTIL},
    {"function", TokenType::FUNCTION},
    {"return", TokenType::RETURN},
    {"break", TokenType::BREAK},
    {"continue", TokenType::CONTINUE},
    {"try", TokenType::TRY},
    {"catch", TokenType::CATCH},
    {"finally", TokenType::FINALLY},
    {"in", TokenType::IN},
    {"true", TokenType::TRUE},
    {"false", TokenType::FALSE},
    {"null", TokenType::NULLPTR},
};

Lexer::Lexer(const std::string& source)
    : source_(source), current_(0), start_(0), line_(1), column_(1) {}

std::vector<Token> Lexer::tokenize() {
    std::vector<Token> tokens;

    while (!isAtEnd()) {
        start_ = current_;
        Token token = nextToken();
        if (token.type != TokenType::NEWLINE) {  // Skip newlines for now
            tokens.push_back(token);
        }
    }

    tokens.push_back(Token(TokenType::END_OF_FILE, "", line_, column_));
    return tokens;
}

Token Lexer::nextToken() {
    skipWhitespace();

    start_ = current_;

    if (isAtEnd()) {
        return makeToken(TokenType::END_OF_FILE);
    }

    char c = advance();

    // Numbers
    if (isDigit(c)) {
        return number();
    }

    // Identifiers and keywords
    if (isAlpha(c) || c == '$') {
        return identifier();
    }

    // String literals
    if (c == '"' || c == '\'') {
        return string();
    }

    // Single character tokens
    switch (c) {
        case '(': return makeToken(TokenType::LPAREN);
        case ')': return makeToken(TokenType::RPAREN);
        case '{': return makeToken(TokenType::LBRACE);
        case '}': return makeToken(TokenType::RBRACE);
        case '[': return makeToken(TokenType::LBRACKET);
        case ']': return makeToken(TokenType::RBRACKET);
        case ',': return makeToken(TokenType::COMMA);
        case '.': return makeToken(TokenType::DOT);
        case ':': return makeToken(TokenType::COLON);
        case ';': return makeToken(TokenType::SEMICOLON);
        case '+': return makeToken(TokenType::PLUS);
        case '-': return makeToken(TokenType::MINUS);
        case '*': return makeToken(TokenType::MULTIPLY);
        case '%': return makeToken(TokenType::MODULO);
        case '!':
            return makeToken(match('=') ? TokenType::NOT_EQUAL : TokenType::LOGICAL_NOT);
        case '=':
            return makeToken(match('=') ? TokenType::EQUAL : TokenType::ASSIGN);
        case '<':
            return makeToken(match('=') ? TokenType::LESS_EQUAL : TokenType::LESS);
        case '>':
            return makeToken(match('=') ? TokenType::GREATER_EQUAL : TokenType::GREATER);
        case '&':
            if (match('&')) return makeToken(TokenType::LOGICAL_AND);
            break;
        case '|':
            if (match('|')) return makeToken(TokenType::LOGICAL_OR);
            break;
        case '/':
            if (match('/')) {
                skipLineComment();
                return nextToken();
            } else if (match('*')) {
                skipBlockComment();
                return nextToken();
            }
            return makeToken(TokenType::DIVIDE);
        case '#':
            return directive();
        case '\n':
            line_++;
            column_ = 1;
            return makeToken(TokenType::NEWLINE);
    }

    return errorToken("Unexpected character");
}

Token Lexer::peekToken() {
    size_t saved_current = current_;
    size_t saved_start = start_;
    int saved_line = line_;
    int saved_column = column_;

    Token token = nextToken();

    current_ = saved_current;
    start_ = saved_start;
    line_ = saved_line;
    column_ = saved_column;

    return token;
}

bool Lexer::isAtEnd() const {
    return current_ >= source_.length();
}

char Lexer::advance() {
    column_++;
    return source_[current_++];
}

char Lexer::peek() const {
    if (isAtEnd()) return '\0';
    return source_[current_];
}

char Lexer::peekNext() const {
    if (current_ + 1 >= source_.length()) return '\0';
    return source_[current_ + 1];
}

bool Lexer::match(char expected) {
    if (isAtEnd()) return false;
    if (source_[current_] != expected) return false;
    current_++;
    column_++;
    return true;
}

bool Lexer::isAtEnd(size_t pos) const {
    return pos >= source_.length();
}

Token Lexer::makeToken(TokenType type) {
    std::string lexeme = source_.substr(start_, current_ - start_);
    return Token(type, lexeme, line_, column_ - (current_ - start_));
}

Token Lexer::errorToken(const std::string& message) {
    reportError(message);
    return Token(TokenType::INVALID, "", line_, column_);
}

Token Lexer::string() {
    char quote = source_[start_];
    std::string value;

    while (!isAtEnd() && peek() != quote) {
        if (peek() == '\n') {
            line_++;
            column_ = 0;
        }
        if (peek() == '\\') {
            advance();
            if (!isAtEnd()) {
                char escaped = advance();
                switch (escaped) {
                    case 'n': value += '\n'; break;
                    case 't': value += '\t'; break;
                    case 'r': value += '\r'; break;
                    case '\\': value += '\\'; break;
                    case '"': value += '"'; break;
                    case '\'': value += '\''; break;
                    default: value += escaped;
                }
            }
        } else {
            value += advance();
        }
    }

    if (isAtEnd()) {
        return errorToken("Unterminated string");
    }

    advance(); // Closing quote

    Token token = makeToken(TokenType::STRING);
    token.lexeme = value;
    return token;
}

Token Lexer::number() {
    while (isDigit(peek())) {
        advance();
    }

    // Look for decimal point
    if (peek() == '.' && isDigit(peekNext())) {
        advance(); // Consume '.'
        while (isDigit(peek())) {
            advance();
        }

        Token token = makeToken(TokenType::FLOAT);
        token.float_value = std::stod(token.lexeme);
        return token;
    }

    Token token = makeToken(TokenType::INTEGER);
    token.int_value = std::stoll(token.lexeme);
    return token;
}

Token Lexer::identifier() {
    while (isAlphaNumeric(peek()) || peek() == '_') {
        advance();
    }

    std::string text = source_.substr(start_, current_ - start_);

    // Check if it's a keyword
    auto it = keywords.find(text);
    if (it != keywords.end()) {
        Token token = makeToken(it->second);
        if (it->second == TokenType::TRUE) {
            token.bool_value = true;
        } else if (it->second == TokenType::FALSE) {
            token.bool_value = false;
        }
        return token;
    }

    return makeToken(TokenType::IDENTIFIER);
}

Token Lexer::directive() {
    // Skip '#'
    while (isAlpha(peek())) {
        advance();
    }
    return makeToken(TokenType::DIRECTIVE);
}

bool Lexer::isDigit(char c) const {
    return c >= '0' && c <= '9';
}

bool Lexer::isAlpha(char c) const {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
}

bool Lexer::isAlphaNumeric(char c) const {
    return isAlpha(c) || isDigit(c);
}

bool Lexer::isWhitespace(char c) const {
    return c == ' ' || c == '\t' || c == '\r';
}

void Lexer::skipWhitespace() {
    while (!isAtEnd()) {
        char c = peek();
        if (isWhitespace(c)) {
            if (c == '\n') {
                break; // Don't skip newlines here
            }
            advance();
        } else {
            break;
        }
    }
}

void Lexer::skipLineComment() {
    while (!isAtEnd() && peek() != '\n') {
        advance();
    }
}

void Lexer::skipBlockComment() {
    while (!isAtEnd()) {
        if (peek() == '*' && peekNext() == '/') {
            advance(); // '*'
            advance(); // '/'
            return;
        }
        if (peek() == '\n') {
            line_++;
            column_ = 0;
        }
        advance();
    }
}

void Lexer::reportError(const std::string& message) {
    std::ostringstream oss;
    oss << "Lexer error at line " << line_ << ", column " << column_ << ": " << message;
    errors_.push_back(oss.str());
}

} // namespace androidscript
