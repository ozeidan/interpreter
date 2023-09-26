package interpreter.ast

data class VariableAccessNode(val identifier: String) : ASTNode.Expression() {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <Context, Eval> visitExpression(visitor: ASTVisitor<Context, Eval>, context: Context): Pair<Context, Eval> {
        return visitor.onVariableAccessNodeVisited(this, context)
    }
}
