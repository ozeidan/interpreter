package interpreter.ast

data class IntegerLiteralNode(val value: Int) : ExpressionNode() {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onIntegerLiteralNodeVisited(this, context)
    }
}
