package interpreter.ast

data class PrintStringNode(val lineNumber: Int, val string: String) : ASTNode.Statement(lineNumber) {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <Context, Eval> visit(visitor: ASTVisitor<Context, Eval>, context: Context): Context {
        return visitor.onPrintStringNodeVisited(this, context)
    }
}
