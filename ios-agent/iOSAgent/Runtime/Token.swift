import Foundation

/**
 * Token types for AndroidScript
 */
enum TokenType {
    // Literals
    case identifier
    case string
    case integer
    case float
    case `true`
    case `false`
    case nullptr

    // Operators
    case plus, minus, multiply, divide, modulo
    case assign
    case equal, notEqual
    case less, lessEqual, greater, greaterEqual
    case logicalAnd, logicalOr, logicalNot

    // Delimiters
    case lparen, rparen
    case lbrace, rbrace
    case lbracket, rbracket
    case comma, dot, semicolon

    // Keywords
    case `if`, `else`
    case `while`, `for`, foreach, `repeat`, until
    case function, `return`
    case `break`, `continue`
    case `try`, `catch`, finally
    case `in`
    case `var`, `let`, const

    // Special
    case newline
    case endOfFile
}

/**
 * Token structure
 */
struct Token {
    let type: TokenType
    let lexeme: String
    let line: Int
    let column: Int

    // Value storage for literals
    let intValue: Int64
    let floatValue: Double
    let boolValue: Bool

    init(type: TokenType, lexeme: String, line: Int, column: Int,
         intValue: Int64 = 0, floatValue: Double = 0.0, boolValue: Bool = false) {
        self.type = type
        self.lexeme = lexeme
        self.line = line
        self.column = column
        self.intValue = intValue
        self.floatValue = floatValue
        self.boolValue = boolValue
    }

    static func eof(line: Int, column: Int) -> Token {
        return Token(type: .endOfFile, lexeme: "", line: line, column: column)
    }

    static func identifier(_ name: String, line: Int, column: Int) -> Token {
        return Token(type: .identifier, lexeme: name, line: line, column: column)
    }

    static func string(_ value: String, line: Int, column: Int) -> Token {
        return Token(type: .string, lexeme: value, line: line, column: column)
    }

    static func integer(_ value: Int64, line: Int, column: Int) -> Token {
        return Token(type: .integer, lexeme: String(value), line: line, column: column, intValue: value)
    }

    static func float(_ value: Double, line: Int, column: Int) -> Token {
        return Token(type: .float, lexeme: String(value), line: line, column: column, floatValue: value)
    }
}

/**
 * Keyword lookup table
 */
struct Keywords {
    private static let map: [String: TokenType] = [
        "if": .if,
        "else": .else,
        "while": .while,
        "for": .for,
        "foreach": .foreach,
        "repeat": .repeat,
        "until": .until,
        "function": .function,
        "return": .return,
        "break": .break,
        "continue": .continue,
        "try": .try,
        "catch": .catch,
        "finally": .finally,
        "in": .in,
        "var": .var,
        "let": .let,
        "const": .const,
        "true": .true,
        "false": .false,
        "null": .nullptr,
        "nil": .nullptr
    ]

    static func lookup(_ identifier: String) -> TokenType {
        return map[identifier] ?? .identifier
    }

    static func isKeyword(_ identifier: String) -> Bool {
        return map[identifier] != nil
    }
}
