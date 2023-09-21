package interpreter.ast

data class VariableDeclarationNode(val lineNumber: Int, val identifier: String, val expression: ExpressionNode) : StatementNode(lineNumber) {
}
