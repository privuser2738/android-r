package com.androidscript.agent.runtime

import android.util.Log

/**
 * Lexer for AndroidScript
 * Converts source code into a stream of tokens
 */
class Lexer(private val source: String) {
    private var current = 0
    private var start = 0
    private var line = 1
    private var column = 1
    private val errors = mutableListOf<String>()

    companion object {
        private const val TAG = "Lexer"
    }

    /**
     * Tokenize the entire source code
     */
    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()

        while (!isAtEnd()) {
            start = current
            val token = nextToken()
            if (token.type != TokenType.NEWLINE) {  // Skip newlines
                tokens.add(token)
            }
        }

        tokens.add(Token.eof(line, column))
        return tokens
    }

    /**
     * Get next token
     */
    fun nextToken(): Token {
        skipWhitespace()
        start = current

        if (isAtEnd()) {
            return makeToken(TokenType.END_OF_FILE)
        }

        val c = advance()

        // Numbers
        if (isDigit(c)) {
            return number()
        }

        // Identifiers and keywords
        if (isAlpha(c) || c == '$') {
            return identifier()
        }

        // String literals
        if (c == '"' || c == '\'') {
            return string()
        }

        // Single character tokens
        return when (c) {
            '(' -> makeToken(TokenType.LPAREN)
            ')' -> makeToken(TokenType.RPAREN)
            '{' -> makeToken(TokenType.LBRACE)
            '}' -> makeToken(TokenType.RBRACE)
            '[' -> makeToken(TokenType.LBRACKET)
            ']' -> makeToken(TokenType.RBRACKET)
            ',' -> makeToken(TokenType.COMMA)
            '.' -> makeToken(TokenType.DOT)
            ':' -> makeToken(TokenType.COLON)
            ';' -> makeToken(TokenType.SEMICOLON)
            '+' -> makeToken(TokenType.PLUS)
            '-' -> makeToken(TokenType.MINUS)
            '*' -> makeToken(TokenType.MULTIPLY)
            '%' -> makeToken(TokenType.MODULO)
            '!' -> makeToken(if (match('=')) TokenType.NOT_EQUAL else TokenType.LOGICAL_NOT)
            '=' -> makeToken(if (match('=')) TokenType.EQUAL else TokenType.ASSIGN)
            '<' -> makeToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> makeToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '&' -> if (match('&')) makeToken(TokenType.LOGICAL_AND) else errorToken("Expected '&' after '&'")
            '|' -> if (match('|')) makeToken(TokenType.LOGICAL_OR) else errorToken("Expected '|' after '|'")
            '/' -> {
                when {
                    match('/') -> {
                        skipLineComment()
                        nextToken()
                    }
                    match('*') -> {
                        skipBlockComment()
                        nextToken()
                    }
                    else -> makeToken(TokenType.DIVIDE)
                }
            }
            '#' -> directive()
            '\n' -> {
                line++
                column = 1
                makeToken(TokenType.NEWLINE)
            }
            else -> errorToken("Unexpected character: $c")
        }
    }

    /**
     * Peek at next token without consuming
     */
    fun peekToken(): Token {
        val savedCurrent = current
        val savedStart = start
        val savedLine = line
        val savedColumn = column

        val token = nextToken()

        current = savedCurrent
        start = savedStart
        line = savedLine
        column = savedColumn

        return token
    }

    /**
     * Check if at end of source
     */
    fun isAtEnd(): Boolean = current >= source.length

    /**
     * Get errors
     */
    fun getErrors(): List<String> = errors
    fun hasErrors(): Boolean = errors.isNotEmpty()

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private fun advance(): Char {
        column++
        return source[current++]
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }

    private fun peekNext(): Char {
        return if (current + 1 >= source.length) '\u0000' else source[current + 1]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        column++
        return true
    }

    private fun makeToken(type: TokenType): Token {
        val lexeme = source.substring(start, current)
        return Token(type, lexeme, line, column - (current - start))
    }

    private fun errorToken(message: String): Token {
        reportError(message)
        return Token(TokenType.INVALID, "", line, column)
    }

    // ========================================================================
    // Token Scanners
    // ========================================================================

    private fun string(): Token {
        val quote = source[start]
        val value = StringBuilder()

        while (!isAtEnd() && peek() != quote) {
            if (peek() == '\n') {
                line++
                column = 0
            }

            if (peek() == '\\') {
                advance()
                if (!isAtEnd()) {
                    val escaped = advance()
                    value.append(when (escaped) {
                        'n' -> '\n'
                        't' -> '\t'
                        'r' -> '\r'
                        '\\' -> '\\'
                        '"' -> '"'
                        '\'' -> '\''
                        else -> escaped
                    })
                }
            } else {
                value.append(advance())
            }
        }

        if (isAtEnd()) {
            return errorToken("Unterminated string")
        }

        advance() // Closing quote

        return Token(TokenType.STRING, value.toString(), line, column - value.length)
    }

    private fun number(): Token {
        while (isDigit(peek())) {
            advance()
        }

        // Look for decimal point
        if (peek() == '.' && isDigit(peekNext())) {
            advance() // Consume '.'
            while (isDigit(peek())) {
                advance()
            }

            val lexeme = source.substring(start, current)
            val floatValue = lexeme.toDoubleOrNull() ?: 0.0
            return Token(TokenType.FLOAT, lexeme, line, column - lexeme.length, floatValue = floatValue)
        }

        val lexeme = source.substring(start, current)
        val intValue = lexeme.toLongOrNull() ?: 0L
        return Token(TokenType.INTEGER, lexeme, line, column - lexeme.length, intValue = intValue)
    }

    private fun identifier(): Token {
        while (isAlphaNumeric(peek()) || peek() == '_') {
            advance()
        }

        val text = source.substring(start, current)
        val type = Keywords.lookup(text)

        val token = makeToken(type)
        return when (type) {
            TokenType.TRUE -> token.copy(boolValue = true)
            TokenType.FALSE -> token.copy(boolValue = false)
            else -> token
        }
    }

    private fun directive(): Token {
        // Skip '#'
        while (isAlpha(peek())) {
            advance()
        }
        return makeToken(TokenType.DIRECTIVE)
    }

    // ========================================================================
    // Character Classification
    // ========================================================================

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z'

    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    private fun isWhitespace(c: Char): Boolean = c == ' ' || c == '\t' || c == '\r'

    // ========================================================================
    // Skip Methods
    // ========================================================================

    private fun skipWhitespace() {
        while (!isAtEnd()) {
            val c = peek()
            if (isWhitespace(c)) {
                if (c == '\n') {
                    break // Don't skip newlines
                }
                advance()
            } else {
                break
            }
        }
    }

    private fun skipLineComment() {
        while (!isAtEnd() && peek() != '\n') {
            advance()
        }
    }

    private fun skipBlockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance() // '*'
                advance() // '/'
                return
            }
            if (peek() == '\n') {
                line++
                column = 0
            }
            advance()
        }
    }

    // ========================================================================
    // Error Reporting
    // ========================================================================

    private fun reportError(message: String) {
        val error = "Lexer error at line $line, column $column: $message"
        errors.add(error)
        Log.e(TAG, error)
    }
}
