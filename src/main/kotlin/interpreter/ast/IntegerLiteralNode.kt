package interpreter.ast

data class IntegerLiteralNode(val value: Int) : ASTNode.Expression() {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <Context, Eval> visitExpression(visitor: ASTVisitor<Context, Eval>, context: Context): Pair<Context, Eval> {
        return visitor.onIntegerLiteralNodeVisited(this, context)
    }
}
