package interpreter.ast

data class PrintExpressionNode(val lineNumber: Int, val expression: ASTNode.Expression) : ASTNode.Statement(lineNumber) {
    override fun getChildren(): List<ASTNode> {
        return listOf(expression)
    }

    override fun <Context, Eval> visit(visitor: ASTVisitor<Context, Eval>, context: Context): Context {
        return visitor.onPrintExpressionNodeVisited(this, context)
    }
}
