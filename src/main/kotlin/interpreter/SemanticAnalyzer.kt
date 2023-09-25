package interpreter

import interpreter.ast.*

/**
 * Semantic Analyzer that checks for type errors and accesses to undeclared variables.
 */
class SemanticAnalyzer {
    private val typeCheckingASTVisitor = TypeCheckingASTVisitor()

    fun analyze(ast: ASTNode, symbolTable: Map<String, Type> = mapOf()) : Map<String, Type> {
        return typeCheckingASTVisitor.visit(ast, TypeCheckingContext(symbolTable, null)).symbolTable
    }
}

enum class Type {
    INTEGER,
    FLOAT,
    INTEGER_SEQUENCE,
    FLOAT_SEQUENCE;

    fun isNumeric() : Boolean {
        return this == INTEGER || this == FLOAT
    }
    fun isSequence() : Boolean {
        return this == INTEGER_SEQUENCE || this == FLOAT_SEQUENCE
    }

    fun toSequence() : Type {
        return when (this) {
            INTEGER -> INTEGER_SEQUENCE
            FLOAT -> FLOAT_SEQUENCE
            else -> {
                throw Exception()
            }
        }
    }

    fun toNumeric() : Type {
        return when (this) {
            INTEGER_SEQUENCE -> INTEGER
            FLOAT_SEQUENCE -> FLOAT
            else -> {
                throw Exception()
            }
        }
    }
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
        val lowerBoundType = visit(sequenceNode.lowerBoundInclusive, context).evaluatedType
        val upperBoundType = visit(sequenceNode.upperBoundInclusive, context).evaluatedType

        if (lowerBoundType != Type.INTEGER) {
            throw InterpreterException("invalid usage of expression of type $lowerBoundType as lower sequence bound")
        }

        if (upperBoundType != Type.INTEGER) {
            throw InterpreterException("invalid usage of expression of type $upperBoundType as upper sequence bound")
        }

        return context.copy(evaluatedType = Type.INTEGER_SEQUENCE)
    }

    override fun onIntegerLiteralNodeVisited(
        integerLiteralNode: IntegerLiteralNode,
        context: TypeCheckingContext
    ): TypeCheckingContext {
        return context.copy(evaluatedType = Type.INTEGER)
    }

    override fun onFloatLiteralNodeVisited(
        floatLiteralNode: FloatLiteralNode,
        context: TypeCheckingContext
    ): TypeCheckingContext {
        return context.copy(evaluatedType = Type.FLOAT)
    }


    override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: TypeCheckingContext): TypeCheckingContext {
        val leftExpressionType = visit(binOpNode.left, context).evaluatedType!!
        val rightExpressionType = visit(binOpNode.right, context).evaluatedType!!

        if (leftExpressionType.isSequence() || rightExpressionType.isSequence()) {
            throw InterpreterException("invalid usage of operator ${binOpNode.operator} on sequence")
        }

        val newExpressionType =
            if (leftExpressionType == Type.FLOAT || rightExpressionType == Type.FLOAT) { Type.FLOAT }
            else { Type.INTEGER }

        return context.copy(evaluatedType = newExpressionType)
    }

    override fun onMappingNodeVisited(mappingNode: MappingNode, context: TypeCheckingContext): TypeCheckingContext {
        val sequenceExpressionType = visit(mappingNode.sequenceExpression, context).evaluatedType!!

        if (!sequenceExpressionType.isSequence()) {
            throw InterpreterException("first parameter of map must be of sequence type")
        }

        val lambdaParameterType = sequenceExpressionType.toNumeric()

        val lambdaExpressionType = visit(mappingNode.lambda.expression,
            context.copy(symbolTable = mapOf(mappingNode.lambda.identifier to lambdaParameterType))).evaluatedType!!

        if (!lambdaExpressionType.isNumeric()) {
            throw InterpreterException("return value of lambda must be scalar")
        }

        return context.copy(evaluatedType = lambdaParameterType.toSequence())
    }

    override fun onReducingNodeVisited(reducingNode: ReducingNode, context: TypeCheckingContext): TypeCheckingContext {
        val sequenceExpressionType = visit(reducingNode.sequenceExpression, context).evaluatedType!!

        if (!sequenceExpressionType.isSequence()) {
            throw InterpreterException("first parameter of reduce must be of sequence type")
        }

        val neutralElementExpressionType = visit(reducingNode.neutralElementExpression, context).evaluatedType!!

        if (!neutralElementExpressionType.isNumeric()) {
            throw InterpreterException("second parameter of reduce must be of scalar type")
        }

        val lambdaExpressionType = visit(reducingNode.lambda.expression,
            context.copy(symbolTable = mapOf(
                (reducingNode.lambda.leftIdentifier to sequenceExpressionType.toNumeric()),
                    (reducingNode.lambda.rightIdentifier to neutralElementExpressionType)))).evaluatedType!!

        if (!lambdaExpressionType.isNumeric()) {
            throw InterpreterException("return value of lambda must be scalar")
        }

        return context.copy(evaluatedType = lambdaExpressionType)
    }

    override fun onVariableAccessNodeVisited(
        variableAccessNode: VariableAccessNode,
        context: TypeCheckingContext
    ): TypeCheckingContext {
        if (!context.symbolTable.containsKey(variableAccessNode.identifier)) {
            throw InterpreterException("access of undeclared variable")
        }

        return context.copy(evaluatedType = context.symbolTable[variableAccessNode.identifier])
    }
}