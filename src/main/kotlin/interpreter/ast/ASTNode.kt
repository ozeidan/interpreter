package interpreter.ast

sealed class ASTNode {
    abstract fun getChildren() : List<ASTNode>
    abstract fun <Context, Eval> visit(visitor : ASTVisitor<Context, Eval>, context: Context) : Context
    abstract class Statement(val linenumber: Int) : ASTNode()
    abstract class Expression : ASTNode() {
        override fun <Context, Eval> visit(visitor : ASTVisitor<Context, Eval>, context: Context) : Context {
            return this.visitExpression(visitor, context).first
        }

        abstract fun <Context, Eval> visitExpression(visitor : ASTVisitor<Context, Eval>, context: Context) : Pair<Context, Eval>
    }
}