package interpreter.ast

data class VariableDeclarationNode(val lineNumber: Int, val identifier: String, val expression: ExpressionNode) : StatementNode(lineNumber) {
    override fun getChildren(): List<ASTNode> {
        return listOf(expression)
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onVariableDeclarationNodeVisited(this, context)
    }
}
