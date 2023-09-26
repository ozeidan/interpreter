package interpreter.ast

data class BinaryLambda(
    val leftIdentifier: String,
    val rightIdentifier: String,
    val expression: ASTNode.Expression)

data class ReducingNode(
    val sequenceExpression: Expression,
    val neutralElementExpression: Expression,
    val lambda: BinaryLambda) : ASTNode.Expression() {
    override fun getChildren(): List<ASTNode> {
        return listOf(sequenceExpression, neutralElementExpression, lambda.expression)
    }

    override fun <Context, Eval> visitExpression(visitor: ASTVisitor<Context, Eval>, context: Context): Pair<Context, Eval> {
        return visitor.onReducingNodeVisited(this, context)
    }
}
