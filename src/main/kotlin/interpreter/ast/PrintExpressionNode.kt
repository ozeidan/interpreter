package interpreter.ast

data class PrintExpressionNode(val lineNumber: Int, val expression: ExpressionNode) : StatementNode(lineNumber) {
    override fun getChildren(): List<ASTNode> {
        return listOf(expression)
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onPrintExpressionNodeVisited(this, context)
    }
}
