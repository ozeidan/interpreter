package interpreter.ast

data class SequenceNode(
    val lowerBoundInclusive : Expression,
    val upperBoundInclusive: Expression) : ASTNode.Expression() {
    override fun getChildren(): List<ASTNode> {
        return listOf(lowerBoundInclusive, upperBoundInclusive)
    }

    override fun <Context, Eval> visitExpression(visitor: ASTVisitor<Context, Eval>, context: Context): Pair<Context, Eval> {
        return visitor.onSequenceNodeVisited(this, context)
    }
}
