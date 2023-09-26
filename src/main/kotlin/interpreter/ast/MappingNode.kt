package interpreter.ast

data class UnaryLambda(
    val identifier: String,
    val expression: ASTNode.Expression)

data class MappingNode(
    val sequenceExpression: Expression,
    val lambda: UnaryLambda) : ASTNode.Expression() {
    override fun getChildren(): List<ASTNode> {
        return listOf(sequenceExpression, lambda.expression)
    }

    override fun <Context, Eval> visitExpression(visitor: ASTVisitor<Context, Eval>, context: Context): Pair<Context, Eval> {
        return visitor.onMappingNodeVisited(this, context)
    }
}
