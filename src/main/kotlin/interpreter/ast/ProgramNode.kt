package interpreter.ast

data class ProgramNode(val statements: List<StatementNode>) : ASTNode() {
    override fun getChildren(): List<ASTNode> {
        return statements
    }

    override fun <T> visit(visitor: ASTVisitor<T>, context: T): T {
        return visitor.onProgramNodeVisited(this, context)
    }
}
