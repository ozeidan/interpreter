package interpreter.ast

data class SequenceNode(val lowerBoundInclusive : ExpressionNode, val upperBoundInclusive: ExpressionNode) : ExpressionNode() {
    override fun getChildren(): List<ASTNode> {
        return listOf(lowerBoundInclusive, upperBoundInclusive)
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onSequenceNodeVisited(this, context)
    }
}
