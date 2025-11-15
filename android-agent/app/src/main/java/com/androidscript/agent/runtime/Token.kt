package com.androidscript.agent.runtime

/**
 * Token types for AndroidScript lexer
 */
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
}

/**
 * Token data class representing a lexical token
 */
data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val column: Int,
    val intValue: Long = 0,
    val floatValue: Double = 0.0,
    val boolValue: Boolean = false
) {
    companion object {
        fun invalid() = Token(TokenType.INVALID, "", 0, 0)
        fun eof(line: Int, column: Int) = Token(TokenType.END_OF_FILE, "", line, column)
    }
}

/**
 * Keyword mapping for identifier lookup
 */
object Keywords {
    val map = mapOf(
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "while" to TokenType.WHILE,
        "for" to TokenType.FOR,
        "foreach" to TokenType.FOREACH,
        "repeat" to TokenType.REPEAT,
        "until" to TokenType.UNTIL,
        "function" to TokenType.FUNCTION,
        "return" to TokenType.RETURN,
        "break" to TokenType.BREAK,
        "continue" to TokenType.CONTINUE,
        "try" to TokenType.TRY,
        "catch" to TokenType.CATCH,
        "finally" to TokenType.FINALLY,
        "in" to TokenType.IN,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,
        "null" to TokenType.NULLPTR
    )

    fun lookup(identifier: String): TokenType {
        return map[identifier.lowercase()] ?: TokenType.IDENTIFIER
    }
}
