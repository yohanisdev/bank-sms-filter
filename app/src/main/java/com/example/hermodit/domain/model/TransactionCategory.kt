package com.example.hermodit.domain.model

/**
 * High-level categorization of financial transactions.
 */
enum class TransactionCategory(val displayName: String) {
    GROCERIES("Groceries"),
    SHOPPING("Shopping"),
    FOOD_AND_DINING("Food & Dining"),
    BILLS_AND_UTILITIES("Bills & Utilities"),
    SALARY("Salary"),
    INVESTMENT("Investment"),
    HEALTHCARE("Healthcare"),
    TRANSPORTATION("Transportation"),
    ENTERTAINMENT("Entertainment"),
    TRANSFER("Transfer"),
    OTHER("Other")
}
