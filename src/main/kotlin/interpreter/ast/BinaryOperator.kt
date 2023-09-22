package interpreter.ast

enum class BinaryOperator {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    POWER;

    override fun toString(): String {
        return when (this) {
            ADDITION -> "+"
            SUBTRACTION -> "-"
            MULTIPLICATION -> "*"
            DIVISION -> "/"
            POWER -> "^"
        }
    }
}