package interpreter.ast

/**
 * Base class of a visitor on the SeqLang AST. Default behavior to pass the context (of type Context) through
 * the child nodes in oder is in place for nodes that are not expressions. Visitor methods, which additionally
 * return an evaluation result of type Eval, have to be implemented for all ExpressionNode types of the AST.
 */
abstract class ASTVisitor<Context, Eval> {
    fun visit(node: ASTNode, context: Context) : Context {
        return node.visit(this, context)
    }

    fun visit(node: ASTNode.Expression, context: Context) : Pair<Context, Eval> {
        return node.visitExpression(this, context)
    }

    protected fun visitChildren(node: ASTNode, context: Context) : Context {
        return node.getChildren().fold(context) { c, n ->
            n.visit(this, c)
        }
    }

    open fun onProgramNodeVisited(programNode: ProgramNode, context: Context) : Context {
        return visitChildren(programNode, context)
    }

    open fun onVariableDeclarationNodeVisited(variableDeclarationNode: VariableDeclarationNode, context: Context) : Context {
        return visitChildren(variableDeclarationNode, context)
    }

    open fun onPrintExpressionNodeVisited(printExpressionNode: PrintExpressionNode, context: Context) : Context {
        return visitChildren(printExpressionNode, context)
    }

    open fun onPrintStringNodeVisited(printStringNode: PrintStringNode, context: Context) : Context {
        return visitChildren(printStringNode, context)
    }

    abstract fun onIntegerLiteralNodeVisited(integerLiteralNode: IntegerLiteralNode, context: Context) : Pair<Context, Eval>
    abstract fun onFloatLiteralNodeVisited(floatLiteralNode: FloatLiteralNode, context: Context) : Pair<Context, Eval>

    abstract fun onVariableAccessNodeVisited(variableAccessNode: VariableAccessNode, context: Context) : Pair<Context, Eval>

    abstract fun onBinOpNodeVisited(binOpNode: BinOpNode, context: Context) : Pair<Context, Eval>

    abstract fun onMappingNodeVisited(mappingNode: MappingNode, context: Context) : Pair<Context, Eval>

    abstract fun onReducingNodeVisited(reducingNode: ReducingNode, context: Context) : Pair<Context, Eval>

    abstract fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: Context) : Pair<Context, Eval>
}
