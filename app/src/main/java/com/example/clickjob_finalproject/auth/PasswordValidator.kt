package com.example.clickjob_finalproject.auth

/**
 * Single source of truth for password rules — used both by the live
 * requirements list and by the final validation on submit.
 */
object PasswordValidator {

    const val MIN_LENGTH = 8

    fun hasMinLength(password: String) = password.length >= MIN_LENGTH

    fun hasLetter(password: String) = password.any { it.isLetter() }

    fun hasDigit(password: String) = password.any { it.isDigit() }

    /** Returns a Hebrew error message, or null when the password is valid. */
    fun validate(password: String): String? = when {
        password.isEmpty() -> "יש להזין סיסמה"
        !hasMinLength(password) -> "הסיסמה חייבת להכיל לפחות $MIN_LENGTH תווים"
        !hasLetter(password) -> "הסיסמה חייבת להכיל לפחות אות אחת"
        !hasDigit(password) -> "הסיסמה חייבת להכיל לפחות ספרה אחת"
        else -> null
    }

    fun isValid(password: String) = validate(password) == null
}