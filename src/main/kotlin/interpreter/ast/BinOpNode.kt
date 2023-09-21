package interpreter.ast

data class BinOpNode(
    val operator : BinaryOperator,
    val left: ExpressionNode,
    val right: ExpressionNode
) : ExpressionNode() {
}
