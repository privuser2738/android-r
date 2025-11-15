#include <iostream>
#include <fstream>
#include <sstream>
#include "lexer.h"
#include "parser.h"
#include "interpreter.h"
#include "builtins.h"

using namespace androidscript;

void printUsage(const char* program) {
    std::cout << "AndroidScript - Android Automation Framework\n\n";
    std::cout << "Usage:\n";
    std::cout << "  " << program << " <script.as>                 Run a script\n";
    std::cout << "  " << program << " --version                   Show version\n";
    std::cout << "  " << program << " --help                      Show this help\n";
    std::cout << "\nExamples:\n";
    std::cout << "  " << program << " examples/simple_login.as\n";
    std::cout << "  " << program << " my_script.as\n";
}

int main(int argc, char* argv[]) {
    if (argc < 2) {
        printUsage(argv[0]);
        return 1;
    }

    std::string arg = argv[1];

    if (arg == "--help" || arg == "-h") {
        printUsage(argv[0]);
        return 0;
    }

    if (arg == "--version" || arg == "-v") {
        std::cout << "AndroidScript v1.0.0-alpha\n";
        return 0;
    }

    // Read script file
    std::string filename = argv[1];
    std::ifstream file(filename);
    if (!file) {
        std::cerr << "Error: Cannot open file: " << filename << std::endl;
        return 1;
    }

    std::ostringstream buffer;
    buffer << file.rdbuf();
    std::string source = buffer.str();

    // Lexer
    Lexer lexer(source);
    auto tokens = lexer.tokenize();

    if (lexer.hasErrors()) {
        std::cerr << "Lexer errors:\n";
        for (const auto& error : lexer.getErrors()) {
            std::cerr << "  " << error << "\n";
        }
        return 1;
    }

    // Parser
    Parser parser(tokens);
    auto ast = parser.parse();

    if (parser.hasErrors()) {
        std::cerr << "Parser errors:\n";
        for (const auto& error : parser.getErrors()) {
            std::cerr << "  " << error << "\n";
        }
        return 1;
    }

    // Interpreter
    Interpreter interpreter;

    // Register built-in functions
    registerBuiltins(interpreter);

    // Execute
    try {
        interpreter.execute(ast);

        if (interpreter.hasErrors()) {
            std::cerr << "Runtime errors:\n";
            for (const auto& error : interpreter.getErrors()) {
                std::cerr << "  " << error << "\n";
            }
            return 1;
        }

        return 0;
    } catch (const std::exception& e) {
        std::cerr << "Fatal error: " << e.what() << std::endl;
        return 1;
    }
}
