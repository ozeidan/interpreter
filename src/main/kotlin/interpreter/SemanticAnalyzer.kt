package interpreter

import interpreter.ast.*
import java.util.*

/**
 * Semantic Analyzer that checks for type errors and accesses to undeclared variables.
 */
class SemanticAnalyzer {
    private val typeCheckingASTVisitor = TypeCheckingASTVisitor()

    fun analyze(ast: ASTNode, symbolTable: Map<String, Type> = mapOf()) : Map<String, Type> {
        return typeCheckingASTVisitor.visit(ast, symbolTable)
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

    fun toNumeric() : Type {
        return when (this) {
            INTEGER_SEQUENCE -> INTEGER
            FLOAT_SEQUENCE -> FLOAT
            else -> {
                throw Exception()
            }
        }
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
}

private typealias SymbolTable = Map<String, Type>
private class TypeCheckingASTVisitor() : ASTVisitor<SymbolTable, Type>() {
    override fun onVariableDeclarationNodeVisited(
        variableDeclarationNode: VariableDeclarationNode,
        context: SymbolTable
    ): SymbolTable {
        return addLineNumberToExceptions(variableDeclarationNode)
        {
            val newSymbolType = visit(variableDeclarationNode.expression, context).second
            context + (variableDeclarationNode.identifier to newSymbolType)
        }
    }

    override fun onPrintExpressionNodeVisited(
        printExpressionNode: PrintExpressionNode,
        context: SymbolTable
    ): SymbolTable {
        return addLineNumberToExceptions(printExpressionNode)
        {
            visit(printExpressionNode.expression, context).first
        }
    }

    override fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: SymbolTable): Pair<SymbolTable, Type> {
        val lowerBoundType = visit(sequenceNode.lowerBoundInclusive, context).second
        val upperBoundType = visit(sequenceNode.upperBoundInclusive, context).second

        if (lowerBoundType != Type.INTEGER) {
            throw InterpreterException("invalid usage of expression of type $lowerBoundType as lower sequence bound")
        }

        if (upperBoundType != Type.INTEGER) {
            throw InterpreterException("invalid usage of expression of type $upperBoundType as upper sequence bound")
        }

        return Pair(context, Type.INTEGER_SEQUENCE)
    }

    override fun onIntegerLiteralNodeVisited(
        integerLiteralNode: IntegerLiteralNode,
        context: SymbolTable
    ): Pair<SymbolTable, Type> {
        return Pair(context, Type.INTEGER)
    }

    override fun onFloatLiteralNodeVisited(
        floatLiteralNode: FloatLiteralNode,
        context: SymbolTable
    ): Pair<SymbolTable, Type> {
        return Pair(context, Type.FLOAT)
    }


    override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: SymbolTable): Pair<SymbolTable, Type> {
        val leftExpressionType = visit(binOpNode.left, context).second
        val rightExpressionType = visit(binOpNode.right, context).second

        if (leftExpressionType.isSequence() || rightExpressionType.isSequence()) {
            throw InterpreterException("invalid usage of operator ${binOpNode.operator} on sequence")
        }

        val newExpressionType =
            if (binOpNode.operator == BinaryOperator.DIVISION) {
                Type.FLOAT
            } else {
                if (leftExpressionType == Type.FLOAT || rightExpressionType == Type.FLOAT) {
                    Type.FLOAT
                } else {
                    Type.INTEGER
                }
            }

        return Pair(context, newExpressionType)
    }

    override fun onMappingNodeVisited(mappingNode: MappingNode, context: SymbolTable): Pair<SymbolTable, Type> {
        val sequenceExpressionType = visit(mappingNode.sequenceExpression, context).second

        if (!sequenceExpressionType.isSequence()) {
            throw InterpreterException("first parameter of map must be of sequence type")
        }

        val lambdaParameterType = sequenceExpressionType.toNumeric()

        val lambdaBodyType = visit(mappingNode.lambda.expression,
            mapOf(mappingNode.lambda.identifier to lambdaParameterType)).second

        if (!lambdaBodyType.isNumeric()) {
            throw InterpreterException("return value of lambda must be scalar")
        }

        return Pair(context, lambdaBodyType.toSequence())
    }

    override fun onReducingNodeVisited(reducingNode: ReducingNode, context: SymbolTable): Pair<SymbolTable, Type> {
        val sequenceExpressionType = visit(reducingNode.sequenceExpression, context).second

        if (!sequenceExpressionType.isSequence()) {
            throw InterpreterException("first parameter of reduce must be of sequence type")
        }

        val neutralElementExpressionType = visit(reducingNode.neutralElementExpression, context).second

        if (!neutralElementExpressionType.isNumeric()) {
            throw InterpreterException("second parameter of reduce must be of scalar type")
        }

        val lambdaExpressionType = visit(reducingNode.lambda.expression,
            mapOf((reducingNode.lambda.leftIdentifier to sequenceExpressionType.toNumeric()),
                (reducingNode.lambda.rightIdentifier to neutralElementExpressionType))).second

        if (!lambdaExpressionType.isNumeric()) {
            throw InterpreterException("return value of lambda must be scalar")
        }

        return Pair(context, lambdaExpressionType)
    }

    override fun onVariableAccessNodeVisited(
        variableAccessNode: VariableAccessNode,
        context: SymbolTable
    ): Pair<SymbolTable, Type> {
        val variableType = Optional
            .ofNullable(context[variableAccessNode.identifier])
            .orElseThrow { InterpreterException("access of undeclared variable") }

        return Pair(context, variableType)
    }
}