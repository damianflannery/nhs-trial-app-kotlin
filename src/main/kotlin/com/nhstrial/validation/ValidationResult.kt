package com.nhstrial.validation

data class ValidationResult(val errors: Map<String, String> = emptyMap()) {
    val hasErrors: Boolean
        get() = errors.isNotEmpty()

    operator fun plus(other: ValidationResult) = ValidationResult(errors + other.errors)
}

class ValidationResultBuilder {
    private val errors = mutableMapOf<String, String>()

    fun error(field: String, message: String) {
        errors.putIfAbsent(field, message)
    }

    fun build() = ValidationResult(errors.toMap())
}

inline fun buildValidation(block: ValidationResultBuilder.() -> Unit): ValidationResult =
    ValidationResultBuilder().apply(block).build()
