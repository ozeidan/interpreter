package interpreter.ast

data class BinaryLambda(val leftIdentifier: String, val rightIdentifier: String, val expression: ExpressionNode)

data class ReducingNode(val sequenceExpression: ExpressionNode,
                   val neutralElementExpression: ExpressionNode,
                   val lambda: BinaryLambda) : ExpressionNode() {
    override fun getChildren(): List<ASTNode> {
        return listOf(sequenceExpression, neutralElementExpression, lambda.expression)
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onReducingNodeVisited(this, context)
    }
}
