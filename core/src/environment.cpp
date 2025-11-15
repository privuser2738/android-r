#include "environment.h"

namespace androidscript {

Environment::Environment() : parent_(nullptr) {}

Environment::Environment(std::shared_ptr<Environment> parent)
    : parent_(parent) {}

void Environment::define(const std::string& name, const Value& value) {
    values_[name] = value;
}

Value Environment::get(const std::string& name) const {
    // Check local scope
    auto it = values_.find(name);
    if (it != values_.end()) {
        return it->second;
    }

    // Check parent scopes
    if (parent_) {
        return parent_->get(name);
    }

    // Not found
    throw UndefinedVariableError(name);
}

void Environment::assign(const std::string& name, const Value& value) {
    // Check local scope
    auto it = values_.find(name);
    if (it != values_.end()) {
        it->second = value;
        return;
    }

    // Check parent scopes
    if (parent_) {
        parent_->assign(name, value);
        return;
    }

    // Variable doesn't exist - create it in local scope
    // (This allows implicit declaration, similar to Python)
    values_[name] = value;
}

bool Environment::exists(const std::string& name) const {
    // Check local scope
    if (values_.find(name) != values_.end()) {
        return true;
    }

    // Check parent scopes
    if (parent_) {
        return parent_->exists(name);
    }

    return false;
}

void Environment::clear() {
    values_.clear();
}

} // namespace androidscript
