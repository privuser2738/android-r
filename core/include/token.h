#ifndef ANDROIDSCRIPT_TOKEN_H
#define ANDROIDSCRIPT_TOKEN_H

#include <string>
#include <map>

namespace androidscript {

enum class TokenType {
    // Literals
    IDENTIFIER,
    STRING,
    INTEGER,
    FLOAT,
    TRUE,
    FALSE,
    NULLPTR,

    // Operators
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    MODULO,         // %
    ASSIGN,         // =
    EQUAL,          // ==
    NOT_EQUAL,      // !=
    LESS,           // <
    LESS_EQUAL,     // <=
    GREATER,        // >
    GREATER_EQUAL,  // >=
    LOGICAL_AND,    // &&
    LOGICAL_OR,     // ||
    LOGICAL_NOT,    // !

    // Delimiters
    LPAREN,         // (
    RPAREN,         // )
    LBRACE,         // {
    RBRACE,         // }
    LBRACKET,       // [
    RBRACKET,       // ]
    COMMA,          // ,
    DOT,            // .
    COLON,          // :
    SEMICOLON,      // ;

    // Keywords
    IF,
    ELSE,
    WHILE,
    FOR,
    FOREACH,
    REPEAT,
    UNTIL,
    FUNCTION,
    RETURN,
    BREAK,
    CONTINUE,
    TRY,
    CATCH,
    FINALLY,
    IN,

    // Directives
    DIRECTIVE,      // #include, #import, etc.

    // Special
    NEWLINE,
    END_OF_FILE,
    INVALID
};

struct Token {
    TokenType type;
    std::string lexeme;
    int line;
    int column;

    // For literals
    union {
        int64_t int_value;
        double float_value;
        bool bool_value;
    };

    Token() : type(TokenType::INVALID), line(0), column(0), int_value(0) {}

    Token(TokenType t, const std::string& lex, int l, int c)
        : type(t), lexeme(lex), line(l), column(c), int_value(0) {}
};

// Keyword mapping
extern const std::map<std::string, TokenType> keywords;

} // namespace androidscript

#endif // ANDROIDSCRIPT_TOKEN_H
