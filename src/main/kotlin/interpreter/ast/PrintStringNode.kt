package interpreter.ast

data class PrintStringNode(val lineNumber: Int, val string: String) : StatementNode(lineNumber) {
    override fun getChildren(): List<ASTNode> {
        return listOf()
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onPrintStringNodeVisited(this, context)
    }
}
