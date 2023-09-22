package interpreter.ast

/**
 * Interface of a visitor on the SeqLang AST which, by default, does DFS while also maintaining a context that is passed
 * through.
 *
 * Implementers of this interface implement the corresponding functions to determine what happens when an AST node of
 * a certain type is visited. The concrete implementations only need to supply implementations for node types where
 * the visitation should actually mutate the context.
 */
interface ASTVisitor<T> {
    fun visit(node: ASTNode, context: T) : T {
        return node.visit(this, context)
    }

    fun visitChildren(node: ASTNode, context: T) : T {
        return node.getChildren().fold(context) { c, n -> n.visit(this, c) }
    }

    fun onProgramNodeVisited(programNode: ProgramNode, context: T) : T {
        return visitChildren(programNode, context)
    }

    fun onVariableDeclarationNodeVisited(variableDeclarationNode: VariableDeclarationNode, context: T) : T {
        return visitChildren(variableDeclarationNode, context)
    }

    fun onPrintExpressionNodeVisited(printExpressionNode: PrintExpressionNode, context: T) : T {
        return visitChildren(printExpressionNode, context)
    }

    fun onPrintStringNodeVisited(printStringNode: PrintStringNode, context: T) : T {
        return visitChildren(printStringNode, context)
    }

    fun onNumberLiteralNodeVisited(numberLiteralNode: NumberLiteralNode, context: T) : T {
        return visitChildren(numberLiteralNode, context)
    }

    fun onVariableAccessNodeVisited(variableAccessNode: VariableAccessNode, context: T) : T {
        return visitChildren(variableAccessNode, context)
    }

    fun onBinOpNodeVisited(binOpNode: BinOpNode, context: T) : T {
        return visitChildren(binOpNode, context)
    }

    fun onMappingNodeVisited(mappingNode: MappingNode, context: T) : T {
        return visitChildren(mappingNode, context)
    }

    fun onReducingNodeVisited(reducingNode: ReducingNode, context: T) : T {
        return visitChildren(reducingNode, context)
    }

    fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: T) : T {
        return visitChildren(sequenceNode, context)
    }
}
