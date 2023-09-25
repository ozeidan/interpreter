package interpreter.ast

data class FloatLiteralNode(val value: Double) : ExpressionNode() {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onFloatLiteralNodeVisited(this, context)
    }
}
