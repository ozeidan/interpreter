package interpreter

import interpreter.ast.*
import java.io.Writer
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.math.pow

class Executor(private val writer: Writer) {
    private val executingASTVisitor = ExecutingASTVisitor()

    fun execute(ast: ASTNode, scope: Map<String, Eval> = mapOf()) : Map<String, Eval> {
        return executingASTVisitor.visit(ast, ExecutionContext(scope, null, writer)).scope
    }
}

sealed class Eval {
    abstract fun binaryOperation(operator : BinaryOperator, other: Eval) : Eval
    abstract fun print() : String

    data class Float(val value : Double) : Eval() {
        override fun binaryOperation(operator: BinaryOperator, other: Eval): Eval {
            val otherValue = when (other) {
                is Float -> other.value
                is Integer -> other.value.toDouble()
                else -> {
                    throw Exception()
                }
            }

            return Float(when (operator) {
                BinaryOperator.ADDITION -> value + otherValue
                BinaryOperator.SUBTRACTION -> value - otherValue
                BinaryOperator.MULTIPLICATION -> value * otherValue
                BinaryOperator.DIVISION -> {
                    if (otherValue == 0.0) {
                        throw InterpreterException("attempted division by 0")
                    }
                    value / otherValue
                }
                BinaryOperator.POWER -> value.pow(otherValue)
            })
        }

        override fun print(): String {
            return value.toString()
        }

    }
    data class Integer(val value : Int) : Eval() {
        override fun binaryOperation(operator: BinaryOperator, other: Eval): Eval {
            if (other is Float) {
                return Float(value.toDouble()).binaryOperation(operator, other)
            }

            require(other is Integer)

            val otherValue = other.value
            return when (operator) {
                BinaryOperator.ADDITION -> Integer(value + otherValue)
                BinaryOperator.SUBTRACTION -> Integer(value - otherValue)
                BinaryOperator.MULTIPLICATION -> Integer(value * otherValue)
                BinaryOperator.DIVISION -> {
                    if (otherValue == 0) {
                        throw InterpreterException("attempted division by 0")
                    }
                    Float(value / otherValue.toDouble())
                }
                BinaryOperator.POWER -> Integer(value.toDouble().pow(other.value).toInt()) // TODO: way to avoid conversion?
            }
        }

        override fun print(): String {
            return value.toString()
        }
    }
    data class Sequence(
        val lowerInclusive: Int,
        val upperInclusive: Int,
        val mappedFunctions : List<(Eval) -> Eval>)
    : Eval() {
        override fun binaryOperation(operator: BinaryOperator, other: Eval): Eval {
            throw Exception()
        }

        override fun print(): String {
            val middlePart = buildStream().map { "${it.print()}, " }.reduce("") { a, b -> a + b }
            val trimmed = middlePart.substring(0, middlePart.length - 2)
            return "{ $trimmed }"
        }

        fun buildStream(): Stream<Eval> {
            val baseStream : Stream<Eval> =
                IntStream.rangeClosed(lowerInclusive, upperInclusive).boxed().map { Integer(it) }

            val mappedStream = mappedFunctions.fold(baseStream) { stream, mappedFunction ->
                stream.map { mappedFunction.invoke(it) }
            }

            return mappedStream.parallel()
        }
    }
}

private data class ExecutionContext(
    val scope : Map<String, Eval>,
    val evaluationResult : Eval?,
    val writer: Writer
)

private class ExecutingASTVisitor : ASTVisitor<ExecutionContext> {
    override fun onVariableDeclarationNodeVisited(
        variableDeclarationNode: VariableDeclarationNode,
        context: ExecutionContext
    ): ExecutionContext {
        return addLineNumberToExceptions(variableDeclarationNode) {
            val (_, evaluationResult) = visit(variableDeclarationNode.expression, context)
            context.copy(scope = context.scope + (variableDeclarationNode.identifier to evaluationResult!!))
        }
    }

    override fun onVariableAccessNodeVisited(
        variableAccessNode: VariableAccessNode,
        context: ExecutionContext
    ): ExecutionContext {
        return context.copy(evaluationResult = context.scope[variableAccessNode.identifier])
    }

    override fun onPrintExpressionNodeVisited(
        printExpressionNode: PrintExpressionNode,
        context: ExecutionContext
    ): ExecutionContext {
        return addLineNumberToExceptions(printExpressionNode) {
            context.writer.write(visit(printExpressionNode.expression, context).evaluationResult!!.print())
            context.writer.flush()
            context
        }
    }

    override fun onPrintStringNodeVisited(
        printStringNode: PrintStringNode,
        context: ExecutionContext
    ): ExecutionContext {
        context.writer.write(printStringNode.string)
        context.writer.flush()
        return context
    }

    override fun onBinOpNodeVisited(binOpNode: BinOpNode, context: ExecutionContext): ExecutionContext {
        val leftEvaluationResult = visit(binOpNode.left, context).evaluationResult!!
        val rightEvaluationResult = visit(binOpNode.right, context).evaluationResult!!

        require(leftEvaluationResult !is Eval.Sequence && rightEvaluationResult !is Eval.Sequence)

        val evaluationResult = leftEvaluationResult.binaryOperation(binOpNode.operator, rightEvaluationResult)
        return context.copy(evaluationResult = evaluationResult)
    }

    override fun onMappingNodeVisited(mappingNode: MappingNode, context: ExecutionContext): ExecutionContext {
        val sequenceEvaluationResult = visit(mappingNode.sequenceExpression, context).evaluationResult

        require(sequenceEvaluationResult is Eval.Sequence)

        val evaluationFun = {
            sequenceValue : Eval ->
            visit(
                mappingNode.lambda.expression,
                context.copy(scope = mapOf(mappingNode.lambda.identifier to sequenceValue))
            ).evaluationResult!!
        }

        return context.copy(evaluationResult = sequenceEvaluationResult.copy(
            mappedFunctions = sequenceEvaluationResult.mappedFunctions + evaluationFun))
    }

    override fun onReducingNodeVisited(reducingNode: ReducingNode, context: ExecutionContext): ExecutionContext {
        val sequenceEvaluationResult = visit(reducingNode.sequenceExpression, context).evaluationResult
        val neutralElementEvaluationResult = visit(reducingNode.neutralElementExpression, context).evaluationResult

        require(sequenceEvaluationResult is Eval.Sequence)
        require(neutralElementEvaluationResult !is Eval.Sequence)

        val evaluationResult = sequenceEvaluationResult.buildStream().reduce(neutralElementEvaluationResult)
        { x, y ->
            visit(
                reducingNode.lambda.expression,
                context.copy(
                    scope = mapOf(
                        (reducingNode.lambda.leftIdentifier to x),
                        (reducingNode.lambda.rightIdentifier to y),
                    )
                )
            ).evaluationResult!!
        }

        return context.copy(evaluationResult = evaluationResult)
    }

    override fun onSequenceNodeVisited(sequenceNode: SequenceNode, context: ExecutionContext): ExecutionContext {
        val lowerEvaluationResult = visit(sequenceNode.lowerBoundInclusive, context).evaluationResult
        val upperEvaluationResult = visit(sequenceNode.upperBoundInclusive, context).evaluationResult

        require(lowerEvaluationResult is Eval.Integer && upperEvaluationResult is Eval.Integer)

        if (upperEvaluationResult.value < lowerEvaluationResult.value) {
            throw InterpreterException("lower sequence boundary is higher than upper boundary")
        }

        return context.copy(evaluationResult =
            Eval.Sequence(
                lowerEvaluationResult.value,
                upperEvaluationResult.value,
                listOf()
        ))
    }

    override fun onIntegerLiteralNodeVisited(
        integerLiteralNode: IntegerLiteralNode,
        context: ExecutionContext
    ): ExecutionContext {
        return context.copy(evaluationResult = Eval.Integer(integerLiteralNode.value))
    }

    override fun onFloatLiteralNodeVisited(
        floatLiteralNode: FloatLiteralNode,
        context: ExecutionContext
    ): ExecutionContext {
        return context.copy(evaluationResult = Eval.Float(floatLiteralNode.value))
    }
}