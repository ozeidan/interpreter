package interpreter.ast

data class VariableDeclarationNode(val lineNumber: Int,
                                   val identifier: String,
                                   val expression: Expression) : ASTNode.Statement(lineNumber) {
    override fun getChildren(): List<ASTNode> {
        return listOf(expression)
    }

    override fun <Context, Eval> visit(visitor : ASTVisitor<Context, Eval>, context: Context) : Context {
        return visitor.onVariableDeclarationNodeVisited(this, context)
    }
}
