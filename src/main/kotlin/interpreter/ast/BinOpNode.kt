package interpreter.ast

data class BinOpNode(
    val operator : BinaryOperator,
    val left: ExpressionNode,
    val right: ExpressionNode
) : ExpressionNode() {
    override fun getChildren(): List<ASTNode> {
        return listOf(left, right)
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T) : T {
        return visitor.onBinOpNodeVisited(this, context)
    }
}
