package interpreter.ast

data class UnaryLambda(val identifier: String, val expression: ExpressionNode)

data class MappingNode(val sequenceExpression: ExpressionNode, val lambda: UnaryLambda) : ExpressionNode() {
    override fun getChildren(): List<ASTNode> {
        return listOf(sequenceExpression, lambda.expression)
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onMappingNodeVisited(this, context)
    }
}
