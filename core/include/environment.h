#ifndef ANDROIDSCRIPT_ENVIRONMENT_H
#define ANDROIDSCRIPT_ENVIRONMENT_H

#include "value.h"
#include <string>
#include <map>
#include <memory>
#include <stdexcept>

namespace androidscript {

// Runtime environment for variable storage and scoping
class Environment {
public:
    // Create global environment
    Environment();

    // Create nested environment with parent
    explicit Environment(std::shared_ptr<Environment> parent);

    // Variable operations
    void define(const std::string& name, const Value& value);
    Value get(const std::string& name) const;
    void assign(const std::string& name, const Value& value);
    bool exists(const std::string& name) const;

    // Scope management
    std::shared_ptr<Environment> getParent() const { return parent_; }
    bool isGlobal() const { return parent_ == nullptr; }

    // Clear all variables (for reset/cleanup)
    void clear();

private:
    std::map<std::string, Value> values_;
    std::shared_ptr<Environment> parent_;
};

// Exception for undefined variables
class UndefinedVariableError : public std::runtime_error {
public:
    explicit UndefinedVariableError(const std::string& name)
        : std::runtime_error("Undefined variable: " + name), name_(name) {}

    const std::string& variableName() const { return name_; }

private:
    std::string name_;
};

} // namespace androidscript

#endif // ANDROIDSCRIPT_ENVIRONMENT_H
