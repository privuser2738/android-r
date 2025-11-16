import Foundation

/**
 * ScriptRunner - Main entry point for executing AndroidScript on iOS
 * Orchestrates lexer, parser, and interpreter
 */
class ScriptRunner {
    private let interpreter: Interpreter
    private let nativeBridge: iOSNativeBridge

    init() {
        self.interpreter = Interpreter()
        self.nativeBridge = iOSNativeBridge(interpreter: interpreter)

        // Register built-in functions
        nativeBridge.registerBuiltins()
        print("ScriptRunner initialized with built-in functions")
    }

    /**
     * Execute AndroidScript source code
     */
    func execute(source: String) -> ExecutionResult {
        print("Tokenizing source code...")

        // Step 1: Lexical Analysis
        let lexer = Lexer(source: source)
        let tokens = lexer.tokenize()

        if lexer.hasErrors() {
            return ExecutionResult(
                success: false,
                errors: lexer.getErrors(),
                output: nil
            )
        }

        print("Tokenization complete: \(tokens.count) tokens")

        // Step 2: Parsing
        print("Parsing tokens...")
        let parser = Parser(tokens: tokens)
        let statements = parser.parse()

        if parser.hasErrors() {
            return ExecutionResult(
                success: false,
                errors: parser.getErrors(),
                output: nil
            )
        }

        print("Parsing complete: \(statements.count) statements")

        // Step 3: Execution
        print("Executing statements...")
        interpreter.execute(statements: statements)

        if interpreter.hasErrors() {
            return ExecutionResult(
                success: false,
                errors: interpreter.getErrors(),
                output: nil
            )
        }

        print("Execution complete")

        return ExecutionResult(
            success: true,
            errors: [],
            output: "Script executed successfully"
        )
    }

    /**
     * Get the global environment for debugging
     */
    func getGlobalEnvironment() -> Environment {
        return interpreter.getGlobalEnvironment()
    }
}

/**
 * Result of script execution
 */
struct ExecutionResult {
    let success: Bool
    let errors: [String]
    let output: String?
}

/**
 * Example usage:
 *
 * let runner = ScriptRunner()
 * let result = runner.execute(source: """
 *     $device = GetDeviceInfo()
 *     Print("Running on: " + $device.platform)
 *
 *     $button = FindByText("Submit")
 *     if ($button != null) {
 *         Click($button)
 *         Print("Button clicked!")
 *     }
 * """)
 *
 * if result.success {
 *     print("Script executed successfully")
 * } else {
 *     print("Errors: \(result.errors)")
 * }
 */
