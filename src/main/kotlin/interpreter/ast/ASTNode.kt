package interpreter.ast

abstract class ASTNode {
    abstract fun getChildren() : List<ASTNode>

    /**
     * This function is supposed to delegate to the method of the visitor that corresponds to the node's type.
     */
    abstract fun <T> visit(visitor : ASTVisitor<T>, context: T) : T
}