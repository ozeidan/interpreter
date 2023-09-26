package interpreter.ast

data class FloatLiteralNode(val value: Double) : ASTNode.Expression() {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <Context, Eval> visitExpression(visitor: ASTVisitor<Context, Eval>, context: Context): Pair<Context, Eval> {
        return visitor.onFloatLiteralNodeVisited(this, context)
    }
}
