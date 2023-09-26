package interpreter.ast

data class ProgramNode(val statements: List<Statement>) : ASTNode() {
    override fun getChildren(): List<ASTNode> {
        return statements
    }

    override fun <Context, Eval> visit(visitor: ASTVisitor<Context, Eval>, context: Context): Context {
        return visitor.onProgramNodeVisited(this, context)
    }
}
