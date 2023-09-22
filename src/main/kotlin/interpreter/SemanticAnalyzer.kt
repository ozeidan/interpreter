package interpreter

import interpreter.ast.*

/**
 * Semantic Analyzer that checks for type errors and accesses to undeclared variables.
 */
class SemanticAnalyzer {
    private val typeCheckingASTVisitor = TypeCheckingASTVisitor()

    fun analyze(ast: ASTNode) {
        typeCheckingASTVisitor.visit(ast, TypeCheckingContext(mapOf(), null))
    }
}

private fun <T> addLineNumberToExceptions(statementNode: StatementNode, function: () -> T) : T {
    // TODO: better abstraction for this
    try {
        return function()
    } catch (e: Exception) {
        throw SeqLangException(e.message ?: "", statementNode.linenumber)
    }
}

private enum class Type {
    NUMBER,
    SEQUENCE
}

private data class TypeCheckingContext(
    val symbolTable : Map<String, Type>,
    val evaluatedType : Type?
)

private class TypeCheckingASTVisitor() : ASTVisitor<TypeCheckingContext> {
    override fun onVariableDeclarationNodeVisited(
        variableDeclarationNode: VariableDeclarationNode,
        context: TypeCheckingContext
    ): TypeCheckingContext {
        return addLineNumberToExceptions(variableDeclarationNode)
        {
            val newContext = visit(variableDeclarationNode.expression, context)
            context.copy(symbolTable = context.symbolTable +
                    (variableDeclarationNode.identifier to newContext.evaluatedType!!))
        }
    }

    override fun onPrintExpressionNodeVisited(
        printExpressionNode: PrintExpressionNode,
        context: TypeCheckingContext
    ): TypeCheckingContext {
        return addLineNumberToExceptions(printExpressionNode)
        {
            visit(printExpressionNode.expression, context)
        }
    }

    override fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: TypeCheckingContext): TypeCheckingContext {
        visit(sequenceNode.upperBoundInclusive, context)
        visit(sequenceNode.upperBoundInclusive, context)
        return context.copy(evaluatedType = Type.SEQUENCE)
    }

    override fun onNumberLiteralNodeVisited(
        numberLiteralNode: NumberLiteralNode,
        context: TypeCheckingContext
    ): TypeCheckingContext {
        return context.copy(evaluatedType = Type.NUMBER)
    }

    override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: TypeCheckingContext): TypeCheckingContext {
        val (_, leftExpressionType) = visit(binOpNode.left, context)
        val (_, rightExpressionType) = visit(binOpNode.right, context)

        if (leftExpressionType != Type.NUMBER || rightExpressionType != Type.NUMBER) {
            throw InterpreterException("invalid usage of operator ${binOpNode.operator} on sequence")
        }

        return context.copy(evaluatedType = Type.NUMBER)
    }

    override fun onMappingNodeVisited(mappingNode: MappingNode, context: TypeCheckingContext): TypeCheckingContext {
        val (_, sequenceExpressionType) = visit(mappingNode.sequenceExpression, context)

        if (sequenceExpressionType != Type.SEQUENCE) {
            throw InterpreterException("first parameter of map must be of sequence type")
        }

        val (_, lambdaExpressionType) = visit(mappingNode.lambda.expression,
            context.copy(symbolTable = context.symbolTable + (mappingNode.lambda.identifier to Type.NUMBER)))

        if (lambdaExpressionType != Type.NUMBER) {
            throw InterpreterException("return value of lambda must be scalar")
        }

        return context.copy(evaluatedType = Type.SEQUENCE)
    }

    override fun onReducingNodeVisited(reducingNode: ReducingNode, context: TypeCheckingContext): TypeCheckingContext {
        val (_, sequenceExpressionType) = visit(reducingNode.sequenceExpression, context)

        if (sequenceExpressionType != Type.SEQUENCE) {
            throw InterpreterException("first parameter of reduce must be of sequence type")
        }

        val (_, neutralElementExpressionType) = visit(reducingNode.neutralElementExpression, context)

        if (neutralElementExpressionType != Type.NUMBER) {
            throw InterpreterException("second parameter of reduce must be of scalar type")
        }


        val (_, lambdaExpressionType) = visit(reducingNode.lambda.expression,
            context.copy(symbolTable = context.symbolTable +
                    (reducingNode.lambda.leftIdentifier to Type.NUMBER) +
                    (reducingNode.lambda.rightIdentifier to Type.NUMBER)))

        if (lambdaExpressionType != Type.NUMBER) {
            throw InterpreterException("return value of lambda must be scalar")
        }

        return context.copy(evaluatedType = Type.NUMBER)
    }

    override fun onVariableAccessNodeVisited(
        variableAccessNode: VariableAccessNode,
        context: TypeCheckingContext
    ): TypeCheckingContext {
        if (!context.symbolTable.containsKey(variableAccessNode.identifier)) {
            throw InterpreterException("access of undeclared variable")
        }
        return context.copy(
            evaluatedType = context.symbolTable.getOrDefault(variableAccessNode.identifier, Type.NUMBER))
    }
}