import Foundation

/**
 * Lexer for AndroidScript
 * Converts source code into tokens
 */
class Lexer {
    private let source: String
    private var current = 0
    private var line = 1
    private var column = 1
    private var start = 0
    private var errors = [String]()

    init(source: String) {
        self.source = source
    }

    func tokenize() -> [Token] {
        var tokens = [Token]()

        while !isAtEnd() {
            start = current
            if let token = nextToken() {
                if token.type != .newline {
                    tokens.append(token)
                }
            }
        }

        tokens.append(Token.eof(line: line, column: column))
        return tokens
    }

    func hasErrors() -> Bool {
        return !errors.isEmpty
    }

    func getErrors() -> [String] {
        return errors
    }

    // MARK: - Tokenization

    private func nextToken() -> Token? {
        let startColumn = column

        guard let c = advance() else {
            return nil
        }

        // Skip whitespace
        if c == " " || c == "\t" || c == "\r" {
            return Token(type: .newline, lexeme: "", line: line, column: startColumn)
        }

        // Newline
        if c == "\n" {
            line += 1
            column = 1
            return Token(type: .newline, lexeme: "", line: line, column: startColumn)
        }

        // Comments
        if c == "/" {
            if peek() == "/" {
                // Line comment
                lineComment()
                return Token(type: .newline, lexeme: "", line: line, column: startColumn)
            } else if peek() == "*" {
                // Block comment
                blockComment()
                return Token(type: .newline, lexeme: "", line: line, column: startColumn)
            } else {
                return Token(type: .divide, lexeme: "/", line: line, column: startColumn)
            }
        }

        // String literals
        if c == "\"" || c == "'" {
            return string(quote: c)
        }

        // Numbers
        if isDigit(c) {
            return number()
        }

        // Identifiers and keywords
        if isAlpha(c) || c == "$" || c == "_" {
            return identifier()
        }

        // Operators and delimiters
        switch c {
        case "+": return Token(type: .plus, lexeme: "+", line: line, column: startColumn)
        case "-": return Token(type: .minus, lexeme: "-", line: line, column: startColumn)
        case "*": return Token(type: .multiply, lexeme: "*", line: line, column: startColumn)
        case "%": return Token(type: .modulo, lexeme: "%", line: line, column: startColumn)

        case "=":
            if match("=") {
                return Token(type: .equal, lexeme: "==", line: line, column: startColumn)
            }
            return Token(type: .assign, lexeme: "=", line: line, column: startColumn)

        case "!":
            if match("=") {
                return Token(type: .notEqual, lexeme: "!=", line: line, column: startColumn)
            }
            return Token(type: .logicalNot, lexeme: "!", line: line, column: startColumn)

        case "<":
            if match("=") {
                return Token(type: .lessEqual, lexeme: "<=", line: line, column: startColumn)
            }
            return Token(type: .less, lexeme: "<", line: line, column: startColumn)

        case ">":
            if match("=") {
                return Token(type: .greaterEqual, lexeme: ">=", line: line, column: startColumn)
            }
            return Token(type: .greater, lexeme: ">", line: line, column: startColumn)

        case "&":
            if match("&") {
                return Token(type: .logicalAnd, lexeme: "&&", line: line, column: startColumn)
            }
            reportError("Unexpected character: &")
            return nil

        case "|":
            if match("|") {
                return Token(type: .logicalOr, lexeme: "||", line: line, column: startColumn)
            }
            reportError("Unexpected character: |")
            return nil

        case "(": return Token(type: .lparen, lexeme: "(", line: line, column: startColumn)
        case ")": return Token(type: .rparen, lexeme: ")", line: line, column: startColumn)
        case "{": return Token(type: .lbrace, lexeme: "{", line: line, column: startColumn)
        case "}": return Token(type: .rbrace, lexeme: "}", line: line, column: startColumn)
        case "[": return Token(type: .lbracket, lexeme: "[", line: line, column: startColumn)
        case "]": return Token(type: .rbracket, lexeme: "]", line: line, column: startColumn)
        case ",": return Token(type: .comma, lexeme: ",", line: line, column: startColumn)
        case ".": return Token(type: .dot, lexeme: ".", line: line, column: startColumn)
        case ";": return Token(type: .semicolon, lexeme: ";", line: line, column: startColumn)

        default:
            reportError("Unexpected character: \(c)")
            return nil
        }
    }

    // MARK: - Specialized Tokenizers

    private func string(quote: Character) -> Token {
        var value = ""

        while !isAtEnd() && peek() != quote {
            if peek() == "\\" {
                _ = advance() // consume backslash
                if let escaped = advance() {
                    switch escaped {
                    case "n": value.append("\n")
                    case "t": value.append("\t")
                    case "r": value.append("\r")
                    case "\\": value.append("\\")
                    case "\"": value.append("\"")
                    case "'": value.append("'")
                    default: value.append(escaped)
                    }
                }
            } else {
                if let c = advance() {
                    value.append(c)
                }
            }
        }

        if isAtEnd() {
            reportError("Unterminated string")
            return Token.string(value, line: line, column: column)
        }

        _ = advance() // closing quote

        return Token.string(value, line: line, column: column)
    }

    private func number() -> Token {
        while isDigit(peek()) {
            _ = advance()
        }

        // Check for decimal point
        if peek() == "." && isDigit(peekNext()) {
            _ = advance() // consume '.'

            while isDigit(peek()) {
                _ = advance()
            }

            let lexeme = substring(from: start, to: current)
            if let value = Double(lexeme) {
                return Token.float(value, line: line, column: column)
            } else {
                reportError("Invalid float literal")
                return Token.float(0.0, line: line, column: column)
            }
        }

        let lexeme = substring(from: start, to: current)
        if let value = Int64(lexeme) {
            return Token.integer(value, line: line, column: column)
        } else {
            reportError("Invalid integer literal")
            return Token.integer(0, line: line, column: column)
        }
    }

    private func identifier() -> Token {
        while isAlphaNumeric(peek()) || peek() == "_" || peek() == "$" {
            _ = advance()
        }

        let lexeme = substring(from: start, to: current)
        let type = Keywords.lookup(lexeme)

        return Token(type: type, lexeme: lexeme, line: line, column: column)
    }

    private func lineComment() {
        while !isAtEnd() && peek() != "\n" {
            _ = advance()
        }
    }

    private func blockComment() {
        _ = advance() // consume '*'

        while !isAtEnd() {
            if peek() == "*" && peekNext() == "/" {
                _ = advance() // consume '*'
                _ = advance() // consume '/'
                break
            }

            if peek() == "\n" {
                line += 1
                column = 0
            }

            _ = advance()
        }
    }

    // MARK: - Helper Methods

    @discardableResult
    private func advance() -> Character? {
        guard !isAtEnd() else { return nil }
        let index = source.index(source.startIndex, offsetBy: current)
        current += 1
        column += 1
        return source[index]
    }

    private func peek() -> Character? {
        guard !isAtEnd() else { return nil }
        let index = source.index(source.startIndex, offsetBy: current)
        return source[index]
    }

    private func peekNext() -> Character? {
        guard current + 1 < source.count else { return nil }
        let index = source.index(source.startIndex, offsetBy: current + 1)
        return source[index]
    }

    private func match(_ expected: Character) -> Bool {
        guard peek() == expected else { return false }
        _ = advance()
        return true
    }

    private func isAtEnd() -> Bool {
        return current >= source.count
    }

    private func isDigit(_ c: Character?) -> Bool {
        guard let c = c else { return false }
        return c >= "0" && c <= "9"
    }

    private func isAlpha(_ c: Character?) -> Bool {
        guard let c = c else { return false }
        return (c >= "a" && c <= "z") || (c >= "A" && c <= "Z")
    }

    private func isAlphaNumeric(_ c: Character?) -> Bool {
        return isAlpha(c) || isDigit(c)
    }

    private func substring(from: Int, to: Int) -> String {
        let startIndex = source.index(source.startIndex, offsetBy: from)
        let endIndex = source.index(source.startIndex, offsetBy: to)
        return String(source[startIndex..<endIndex])
    }

    private func reportError(_ message: String) {
        let error = "Lexer error at line \(line), column \(column): \(message)"
        errors.append(error)
        print(error)
    }
}
