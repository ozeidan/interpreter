package interpreter.ast

data class BinOpNode(
    val operator : BinaryOperator,
    val left: Expression,
    val right: Expression
) : ASTNode.Expression() {
    override fun getChildren(): List<ASTNode> {
        return listOf(left, right)
    }

    override fun <Context, Eval> visitExpression(visitor : ASTVisitor<Context, Eval>, context: Context) : Pair<Context, Eval> {
        return visitor.onBinOpNodeVisited(this, context)
    }
}
