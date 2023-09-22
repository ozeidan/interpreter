package interpreter.ast

data class VariableAccessNode(val identifier: String) : ExpressionNode() {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onVariableAccessNodeVisited(this, context)
    }
}
