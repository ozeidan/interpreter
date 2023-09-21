package interpreter.ast

data class PrintExpressionNode(val lineNumber: Int, val expression: ExpressionNode) : StatementNode(lineNumber) {
}
